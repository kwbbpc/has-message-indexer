package aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import core.MessageHandler;
import db.dynamo.DynamoManager;
import db.sensor.xbee.XbeeDao;
import messages.DynamoDbSensorRegistrar;
import messages.SensorRegistrar;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSMessenger {


    public static final String MESSAGE_ID_FIELD = "messageId";

    private static final Logger logger = LoggerFactory.getLogger(SQSMessenger.class);

    private final Map<Integer, MessageHandler> messageHandlerMap;
    private final BasicAWSCredentials creds;
    private SensorRegistrar registrar;



    public SQSMessenger(BasicAWSCredentials creds){

        this.creds = creds;

        //update message ids with their parsers
        this.messageHandlerMap = new HashMap<Integer, MessageHandler>();



    }

    public void setSensorRegistrar(SensorRegistrar registrar){
        this.registrar = registrar;
    }

    public void registerMessageHandler(int messageId, MessageHandler handler){
        this.messageHandlerMap.put(messageId, handler);
    }


    public void getMessages(String queueUrl){

       AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(
                        this.creds)).build();

        logger.info("Building SQS request with queue {}", queueUrl);
        SetQueueAttributesRequest set_attrs_request = new SetQueueAttributesRequest()
                .withQueueUrl(queueUrl)
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
        sqs.setQueueAttributes(set_attrs_request);

        //pull messages from the queue
        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setMaxNumberOfMessages(10);
        request.setQueueUrl(queueUrl);
        request.withAttributeNames("All");
        ReceiveMessageResult result = sqs.receiveMessage(request);

        logger.info("Getting messages");
        List<Message> messages = result.getMessages();
        logger.info("Got {} messages", messages.size());

        for(Message m : messages) {
            //determine the parser to use
            try {
                String rawMessage = m.getBody();
                JsonNode sqsMessage = JsonUtils.MAPPER.readTree(rawMessage);
                JsonNode jsonMessage = JsonUtils.MAPPER.readTree(sqsMessage.get("Message").textValue());
                logger.trace("Message received: ", JsonUtils.MAPPER.writeValueAsString(jsonMessage));


                DateTime createdDate = new DateTime(Long.parseLong(m.getAttributes().get("SentTimestamp")));

                Integer msgId = jsonMessage.get(MESSAGE_ID_FIELD).asInt();

                if(msgId != null){

                    //TODO: this needs to be refactored sometime to be more flexible.
                    XbeeDao sensorDao = XbeeDao.makeSensorDao(jsonMessage.get("nodeInfo"));

                    byte[] payload = jsonMessage.get("payload").textValue().getBytes();

                    MessageHandler handler = this.messageHandlerMap.get(msgId);
                    if(handler != null){

                        //check in with the sensor registrar
                        if(this.registrar != null) {
                            this.registrar.checkIn(sensorDao, createdDate, msgId);
                        }else{
                            logger.warn("Sensor registrar is null, no sensors are going to be registered.");
                        }

                        handler.processMessage(createdDate, sensorDao, payload);
                        //delete the message
                        sqs.deleteMessage(queueUrl, m.getReceiptHandle());
                        continue;
                    }else{
                        logger.error("No handler found for message id {}.  Message Handlers exist for message types {}." +
                                        "Original Message: {}",
                                msgId, this.messageHandlerMap.keySet(), JsonUtils.MAPPER.writeValueAsString(jsonMessage));
                    }
                }else{
                    logger.error("Message ID not found in message: {}", JsonUtils.MAPPER.writeValueAsString(jsonMessage));
                }


                //TODO if the parser doesn't exist, publish an error to the error SNS topic and save to S3

                //handoff to that parser
            }catch (JsonParseException e){
                //hand to error handler
                logger.error("Error parsing json message: {}. Original Message: {}", e, m);
            }catch (IOException e){
                logger.error("IOException while processing message {}: {}", m, e);
            }

            //delete the message
            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
        }

    }


}
