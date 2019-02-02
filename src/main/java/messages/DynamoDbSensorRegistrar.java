package messages;

import db.DatabaseManager;
import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;

public class DynamoDbSensorRegistrar implements SensorRegistrar{

    private DatabaseManager manager;

    public DynamoDbSensorRegistrar(DatabaseManager manager){
        this.manager = manager;
    }

    @Override
    public void checkIn(XbeeDao sensor, DateTime updateTime, Integer messageId) {

        this.manager.registerSensor(sensor.getAddress64bit(), DateTime.now(), messageId);

    }
}
