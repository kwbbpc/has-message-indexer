package weather.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import core.MessageHandler;
import db.DatabaseManager;
import db.model.humidity.HumidityDao;
import db.model.temp.TemperatureDao;
import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsonUtils;
import weather.messages.Weather;

import java.util.Base64;

public class WeatherMessageHandler implements MessageHandler {

    private DatabaseManager manager;
    private DateTime lastUpdated;

    private static final Logger logger = LoggerFactory.getLogger(WeatherMessageHandler.class);

    public WeatherMessageHandler(DatabaseManager pipe) {
        this.manager = pipe;
        this.lastUpdated = new DateTime();
    }

    public void processMessage(DateTime createdDate, XbeeDao device, byte[] payload) {

        logger.info("Processing message payload: {}", payload);

        try {

            logger.debug("Decoding from base 64.... ");
            //all payloads are base64 encoded.
            byte[] decodedPayload = Base64.getDecoder().decode(payload);
            logger.debug("Decoded base64 payload.  Result: {}", decodedPayload);

            //parse the weather message
            Weather.WeatherMessage msg = Weather.WeatherMessage.parseFrom(ByteString.copyFrom(decodedPayload));

            logger.debug("Parsed a weather message: {}", msg.toString());

            if(msg.hasTemperatureF()) {

                logger.info("Weather message temperature data: {}", msg.getTemperatureF());

                TemperatureDao temp = new TemperatureDao(createdDate, msg, device);

                logger.debug("Weather message temperature data: {}", JsonUtils.MAPPER.writeValueAsString(temp));

                this.manager.saveTemperature(temp);
            }

            if(msg.hasHumidity()){
                logger.info("Weather message humidity data: {}", msg.getHumidity());
                HumidityDao humidityDao = new HumidityDao(createdDate, msg, device);
                logger.debug("Weather message humidity data: {}", JsonUtils.MAPPER.writeValueAsString(humidityDao));
                this.manager.saveHumidity(humidityDao);
            }


        }
        catch(InvalidProtocolBufferException e){

            logger.error("Error processing temperature message: " + e.getMessage());

        }catch(JsonProcessingException e){

            logger.error("Error saving data: " + e.getMessage());
            logger.error("weather message data: " + payload);

        }

    }


}
