package db.sensor.xbee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import util.JsonUtils;

import java.io.IOException;

public class XbeeDao {

    private String address64bit;
    private String address16bit;
    private String firmwareVersion;
    private String hardwareVersion;
    private int hardwareVersionId;
    private String nodeId;
    private String panId;
    private String xbeeProtocol;

    public XbeeDao() {
    }

    public static XbeeDao makeSensorDao(JsonNode device) throws JsonProcessingException, IOException{
        return JsonUtils.MAPPER.readValue(JsonUtils.MAPPER.writeValueAsString(device), XbeeDao.class);
    }


    public Integer getHardwareVersionId() {
        return hardwareVersionId;
    }

    public void setHardwareVersionId(Integer hardwareVersionId) {
        this.hardwareVersionId = hardwareVersionId;
    }

    public String getAddress64bit() {
        return address64bit;
    }

    public void setAddress64bit(String address64bit) {
        this.address64bit = address64bit;
    }

    public String getAddress16bit() {
        return address16bit;
    }

    public void setAddress16bit(String address16bit) {
        this.address16bit = address16bit;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getPanId() {
        return panId;
    }

    public void setPanId(String panId) {
        this.panId = panId;
    }

    public String getXbeeProtocol() {
        return xbeeProtocol;
    }

    public void setXbeeProtocol(String xbeeProtocol) {
        this.xbeeProtocol = xbeeProtocol;
    }
}
