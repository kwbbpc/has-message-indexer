package db;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import weather.messages.Weather;

import java.util.Date;

public class HumidityDao {

    private final Date timestamp;
    private  final float humidity;

    private final XbeeDao sensorInfo;


    public HumidityDao(Weather.WeatherMessage message, XbeeDao sensor){

        this.timestamp = DateTime.now().toDate();
        this.humidity = message.getHumidity();

        this.sensorInfo = sensor;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public float getHumidity() {
        return humidity;
    }

    public XbeeDao getSensorInfo() {
        return sensorInfo;
    }

}
