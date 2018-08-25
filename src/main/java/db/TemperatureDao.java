package db;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import weather.messages.Weather;

import java.util.Date;

public class TemperatureDao {

    private final Date timestamp;
    private final float temperature;

    private final XbeeDao sensorInfo;


    public TemperatureDao(Weather.WeatherMessage message, XbeeDao sensor){

        this.timestamp = DateTime.now().toDate();
        this.temperature = message.getTemperatureF();

        this.sensorInfo = sensor;
    }

    public Date getTimestamp() {
        return timestamp;
    }


    public float getTemperature() {
        return temperature;
    }


    public XbeeDao getSensorInfo() {
        return sensorInfo;
    }

}
