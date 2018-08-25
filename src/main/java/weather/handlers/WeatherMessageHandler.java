package weather.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import core.MessageHandler;
import db.DataPipe;
import db.HumidityDao;
import db.TemperatureDao;
import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import weather.messages.Weather;

public class WeatherMessageHandler implements MessageHandler {

    private DataPipe manager;
    private DateTime lastUpdated;

    public WeatherMessageHandler(DataPipe pipe) {
        this.manager = pipe;
        this.lastUpdated = new DateTime();
    }

    public void processMessage(XbeeDao device, byte[] payload) {

        try {

            //parse the weather message
            Weather.WeatherMessage msg = Weather.WeatherMessage.parseFrom(ByteString.copyFrom(payload));

            if(msg.hasTemperatureF()) {
                TemperatureDao temp = new TemperatureDao(msg, device);
                this.manager.saveTemperature(temp);
            }

            if(msg.hasHumidity()){
                HumidityDao humidityDao = new HumidityDao(msg, device);
                this.manager.saveHumidity(humidityDao);
            }


        }
        catch(InvalidProtocolBufferException e){

            System.err.println("Error processing temperature message: " + e.getMessage());

        }catch(JsonProcessingException e){

            System.err.println("Error saving data: " + e.getMessage());
            System.err.println("weather message data: " + payload);

        }

    }


}
