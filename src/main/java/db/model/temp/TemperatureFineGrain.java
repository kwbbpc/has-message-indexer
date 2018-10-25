package db.model.temp;

public class TemperatureFineGrain {

    private String unit;
    private float temperature;

    public TemperatureFineGrain(String unit, float temperature) {
        this.unit = unit;
        this.temperature = temperature;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
}
