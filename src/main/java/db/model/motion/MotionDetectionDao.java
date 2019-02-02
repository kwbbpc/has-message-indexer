package db.model.motion;

import db.sensor.xbee.XbeeDao;
import org.joda.time.DateTime;

public class MotionDetectionDao {

    private final String nodeId;
    private final String sensorType;
    private final DateTime createdDate;
    private final DateTime indexedDate;
    private final XbeeDao nodeInfo;
    private final int expectedDistanceCm;
    private final int triggeredDistanceCm;


    public MotionDetectionDao(DateTime createdDate, XbeeDao sensor, int expectedDistanceCm, int triggeredDistanceCm) {
        this.nodeId = sensor.getAddress64bit();
        this.sensorType = MotionDetectionConstants.MotionSensor;
        this.createdDate = createdDate;
        this.indexedDate = DateTime.now().toDateTimeISO();
        this.nodeInfo = sensor;
        this.expectedDistanceCm = expectedDistanceCm;
        this.triggeredDistanceCm = triggeredDistanceCm;
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

    public int getExpectedDistanceCm() {
        return expectedDistanceCm;
    }

    public int getTriggeredDistanceCm() {
        return triggeredDistanceCm;
    }
}
