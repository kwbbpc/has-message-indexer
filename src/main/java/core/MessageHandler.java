package core;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;

public interface MessageHandler {

    void processMessage(DateTime createdDate, XbeeDao deviceInfo, byte[] payload);

}
