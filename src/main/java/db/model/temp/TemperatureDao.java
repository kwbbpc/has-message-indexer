package db.model.temp;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;
import weather.messages.Weather;



public class TemperatureDao {

    private final String nodeId;
    private final String sensorType;
    private final DateTime createdDate;
    private final DateTime indexedDate;
    private final XbeeDao nodeInfo;
    private final TemperatureFineGrain temperature;

    /* Data Model as of 2018-9-2
    {
        nodeId: "XXXX",
        sensorType: "temperature",
        createdDate: "2018-08-24T00:00:00+00:00",   //timestamp the measurement was created and published
        indexDate: "2018-0825T00:00:00+00:00",  //timestamp the measurement was indexed
        nodeInfo: {
        custom per node type
    },
        temperature: {
            unit: F
            degree: 87.1
        }
    }
    */

    public TemperatureDao(DateTime createdDate, Weather.WeatherMessage message, XbeeDao sensor){

        this.indexedDate = DateTime.now().toDateTimeISO();

        this.createdDate = createdDate;

        this.nodeId = sensor.getAddress64bit();

        this.temperature = new TemperatureFineGrain(TemperatureConstants.UNITS.FARENHEIGHT, message.getTemperatureF());

        this.nodeInfo = sensor;
        this.sensorType = TemperatureConstants.TemperatureSensorType;
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

    public TemperatureFineGrain getTemperature() {
        return temperature;
    }
}
