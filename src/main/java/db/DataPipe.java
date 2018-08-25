package db;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface DataPipe {

    void saveTemperature(TemperatureDao temperature) throws JsonProcessingException;

    void saveHumidity(HumidityDao humidityDao) throws JsonProcessingException;

}
