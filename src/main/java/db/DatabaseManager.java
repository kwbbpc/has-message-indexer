package db;

import com.fasterxml.jackson.core.JsonProcessingException;
import db.model.motion.MotionDetectionDao;
import db.model.temp.TemperatureDao;
import db.model.humidity.HumidityDao;
import org.joda.time.DateTime;

public interface DatabaseManager {

    void saveTemperature(TemperatureDao temperature) throws JsonProcessingException;

    void saveHumidity(HumidityDao humidityDao) throws JsonProcessingException;

    void saveMotionDetection(MotionDetectionDao motionDetectionDao) throws JsonProcessingException;

    void registerSensor(String nodeId, DateTime updateDate, final Integer messageId);

}
