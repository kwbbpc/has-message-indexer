package messages;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;

public interface SensorRegistrar {

    void checkIn(XbeeDao sensor, DateTime updateTime, final Integer messageId);
}
