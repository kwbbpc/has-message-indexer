import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.amazonaws.util.Base64;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import core.MessageHandler;
import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSMessenger {


    public static String queueUrl = "https://sqs.us-east-1.amazonaws.com/051846041120/test";
    public static final String MESSAGE_ID_FIELD = "messageId";


    private final Map<Integer, MessageHandler> messageHandlerMap;
    private final AmazonSQS sqs;



    public SQSMessenger(){

        //update message ids with their parsers
        this.messageHandlerMap = new HashMap<Integer, MessageHandler>();

        this.sqs = AmazonSQSClientBuilder.defaultClient();

        SetQueueAttributesRequest set_attrs_request = new SetQueueAttributesRequest()
                .withQueueUrl(queueUrl)
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
        sqs.setQueueAttributes(set_attrs_request);

    }

    public void registerMessageHandler(int messageId, MessageHandler handler){
        this.messageHandlerMap.put(messageId, handler);
    }


    public void getMessages(){

        //pull messages from the queue
        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setMaxNumberOfMessages(10);
        request.setQueueUrl(queueUrl);
        ReceiveMessageResult result = sqs.receiveMessage(request);
        List<Message> messages = result.getMessages();

        for(Message m : messages) {
            //determine the parser to use
            try {
                String rawMessage = m.getBody();
                JsonNode sqsMessage = JsonUtils.MAPPER.readTree(rawMessage);
                JsonNode jsonMessage = JsonUtils.MAPPER.readTree(sqsMessage.get("Message").textValue());

                DateTime createdDate = DateTime.parse(sqsMessage.get("Timestamp").asText());

                Integer msgId = jsonMessage.get(MESSAGE_ID_FIELD).asInt();

                if(msgId != null){

                    //TODO: this needs to be refactored sometime to be more flexible.
                    // well fuck you too buddy
                    XbeeDao sensorDao = XbeeDao.makeSensorDao(jsonMessage.get("nodeInfo"));

                    byte[] payload = jsonMessage.get("payload").textValue().getBytes();

                    MessageHandler handler = this.messageHandlerMap.get(msgId);
                    if(handler != null){
                        handler.processMessage(createdDate, sensorDao, payload);
                        //delete the message
                        sqs.deleteMessage(queueUrl, m.getReceiptHandle());
                        continue;
                    }
                }


                //TODO if the parser doesn't exist, publish an error to the error SNS topic and save to S3

                //handoff to that parser
            }catch (JsonParseException e){
                //hand to error handler
                System.err.println(e);
            }catch (IOException e){
                System.err.println(e);
            }

            //delete the message
            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
        }

    }


}
