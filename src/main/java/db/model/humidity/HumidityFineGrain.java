package db.model.humidity;

public class HumidityFineGrain {

    private float humidity;
    private String unit;

    public HumidityFineGrain(float humidity, String unit) {
        this.humidity = humidity;
        this.unit = unit;
    }

    public float getHumidity() {
        return humidity;
    }

    public String getUnit() {
        return unit;
    }
}
