

import aws.SQSMessenger;
import com.amazonaws.auth.BasicAWSCredentials;
import db.DatabaseManager;
import db.dynamo.DynamoManager;
import weather.handlers.WeatherMessageHandler;


public class ServerMain {


    public static final Integer WEATHER_MSG_ID = 0;


    private static void registerHandlers(SQSMessenger messenger, BasicAWSCredentials creds){

        DatabaseManager pipe = new DynamoManager(creds);

        messenger.registerMessageHandler(WEATHER_MSG_ID, new WeatherMessageHandler(pipe));

    }


    public static void main(String args[]){

        System.out.println("Server is starting.....");

        try{

            AwsCredentialsLoader loader = new AwsCredentialsLoader("awsAccessKeys.properties");
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(loader.getAccessKey(), loader.getSecretKey());

            SQSMessenger messenger = new SQSMessenger(awsCreds);

            registerHandlers(messenger, awsCreds);

            while(true){

                messenger.getMessages();

            }

        }catch (Exception e){
            System.out.println("Error: " + e);
        }
    }
}
