package fr.ramiere;

import java.util.Collections;
import java.util.Map;

public class Measurement {
    final String name;
    final double time;
    final double value;
    final Map<String, String> tags;

    public Measurement(String name, double time, double value, Map<String, String> tags) {
        this.name = name;
        this.time = time;
        this.value = value;
        this.tags = tags;
    }

    public Measurement(String name, double time, double value) {
        this.name = name;
        this.time = time;
        this.value = value;
        this.tags = Collections.EMPTY_MAP;
    }

    @Override
    public String toString() {
        return name + " " + time + " " + value + " " + tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Measurement)) return false;

        Measurement that = (Measurement) o;

        if (Double.compare(that.time, time) != 0) return false;
        if (Double.compare(that.value, value) != 0) return false;
        return name.equals(that.name) && tags.equals(that.tags);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(time);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + tags.hashCode();
        return result;
    }
}
