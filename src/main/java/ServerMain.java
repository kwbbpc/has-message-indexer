

import aws.SQSMessenger;
import com.amazonaws.auth.BasicAWSCredentials;
import db.DatabaseManager;
import db.dynamo.DynamoManager;
import messages.DynamoDbSensorRegistrar;
import messages.handlers.MotionDetectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messages.handlers.WeatherMessageHandler;


public class ServerMain {

    public static String queueUrl = "https://sqs.us-east-1.amazonaws.com/051846041120/weather";
    public static String queueUrl2 = "https://sqs.us-east-1.amazonaws.com/051846041120/motion-detection";

    public static final Integer WEATHER_MSG_ID = 0;
    public static final Integer MOTION_DETECT_MSG_ID = 1;
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);


    private static void registerHandlers(SQSMessenger messenger, BasicAWSCredentials creds){

        DatabaseManager pipe = new DynamoManager(creds);

        messenger.registerMessageHandler(WEATHER_MSG_ID, new WeatherMessageHandler(pipe));
        messenger.registerMessageHandler(MOTION_DETECT_MSG_ID, new MotionDetectionHandler(pipe));
        messenger.setSensorRegistrar(new DynamoDbSensorRegistrar(pipe));

    }


    public static void main(String args[]){

        logger.info("Server is starting.....");

        while(true) {
            try {

                logger.trace("Loading AWS credentials.");
                AwsCredentialsLoader loader = new AwsCredentialsLoader("awsAccessKeys.properties");
                BasicAWSCredentials awsCreds = new BasicAWSCredentials(loader.getAccessKey(), loader.getSecretKey());

                logger.trace("Starting the SQS messenger.");
                SQSMessenger messenger = new SQSMessenger(awsCreds);

                logger.trace("Registering the SQS messenger.");
                registerHandlers(messenger, awsCreds);

                while (true) {

                    logger.trace("Looping for SQS messages.");
                    messenger.getMessages(queueUrl);
                    messenger.getMessages(queueUrl2);

                }

            } catch (Exception e) {
                logger.error("Error: " + e);
            }

            try {
                Thread.sleep(1000);
            }catch (Exception e){
                logger.error("Error sleeping: {}.  aborting.", e);
            }
        }
    }
}
