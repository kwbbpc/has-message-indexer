package db.model.humidity;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import weather.messages.Weather;

import java.util.Date;

public class HumidityDao {


    private final String nodeId;
    private final String sensorType;
    private final DateTime createdDate;
    private final DateTime indexedDate;
    private final XbeeDao nodeInfo;
    private final HumidityFineGrain humidity;


    public HumidityDao(DateTime createdDate, Weather.WeatherMessage message, XbeeDao sensor){
        this.nodeId = sensor.getAddress64bit();
        this.sensorType = HumidityConstants.HumiditySensorType;
        this.createdDate = createdDate;
        this.indexedDate = DateTime.now().toDateTimeISO();
        this.nodeInfo = sensor;
        this.humidity = new HumidityFineGrain(message.getHumidity(), HumidityConstants.UNITS.UNITS);
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getSensorType() {
        return sensorType;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public DateTime getIndexedDate() {
        return indexedDate;
    }

    public XbeeDao getNodeInfo() {
        return nodeInfo;
    }

    public HumidityFineGrain getHumidity() {
        return humidity;
    }
}
