package db;

import com.fasterxml.jackson.core.JsonProcessingException;
import db.model.temp.TemperatureDao;
import db.model.humidity.HumidityDao;

public interface DatabaseManager {

    void saveTemperature(TemperatureDao temperature) throws JsonProcessingException;

    void saveHumidity(HumidityDao humidityDao) throws JsonProcessingException;

}
