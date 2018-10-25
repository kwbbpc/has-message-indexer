
import db.DatabaseManager;
import db.dynamo.DynamoManager;
import weather.handlers.WeatherMessageHandler;


public class ServerMain {


    public static final Integer WEATHER_MSG_ID = 0;


    private static void registerHandlers(SQSMessenger messenger){

        DatabaseManager pipe = new DynamoManager();

        messenger.registerMessageHandler(WEATHER_MSG_ID, new WeatherMessageHandler(pipe));

    }


    public static void main(String args[]){

        System.out.println("Server is starting.....");

        try{

            SQSMessenger messenger = new SQSMessenger();

            registerHandlers(messenger);

            while(true){

                messenger.getMessages();

            }

        }catch (Exception e){
            System.out.println("Error: " + e);
        }
    }
}
