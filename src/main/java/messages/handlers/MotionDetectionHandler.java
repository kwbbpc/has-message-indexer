package messages.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import core.MessageHandler;
import db.DatabaseManager;
import db.model.motion.MotionDetectionDao;
import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.proto.MotionDetect;

import java.util.Base64;

public class MotionDetectionHandler implements MessageHandler {

    private DatabaseManager manager;


    private static final Logger logger = LoggerFactory.getLogger(MotionDetectionHandler.class);

    public MotionDetectionHandler(DatabaseManager pipe) {
        this.manager = pipe;
    }

    public void processMessage(DateTime createdDate, XbeeDao deviceInfo, byte[] payload) {

        logger.info("Processing message payload: {}", payload);

        try {

            logger.debug("Decoding from base 64.... ");
            //all payloads are base64 encoded.
            byte[] decodedPayload = Base64.getDecoder().decode(payload);
            logger.debug("Decoded base64 payload.  Result: {}", decodedPayload);

            //parse the weather message
            MotionDetect.MotionDetectMessage msg = MotionDetect.MotionDetectMessage.parseFrom(ByteString.copyFrom(decodedPayload));

            logger.debug("Parsed a motion detection message: {}", msg.toString());


            MotionDetectionDao md = new MotionDetectionDao(createdDate, deviceInfo, msg.getExpectedDistanceCm(),
                    msg.getTriggeredDistanceCm());

            this.manager.saveMotionDetection(md);


        }catch(InvalidProtocolBufferException e){

            logger.error("Error processing temperature message: " + e.getMessage());

        }catch(JsonProcessingException e){

            logger.error("Error saving data: " + e.getMessage());
            logger.error("Motion detection message data: " + payload);

        }catch (Exception e){
            logger.error("Unknown exception occurred: {}", e);
        }

    }
}
