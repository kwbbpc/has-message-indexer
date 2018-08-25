package core;

import db.sensor.xbee.XbeeDao;

public interface MessageHandler {

    void processMessage(XbeeDao deviceInfo, byte[] payload);

}
