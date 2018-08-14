package ibsp.metaserver.bean;

import java.util.ArrayList;
import java.util.List;

public class Histogram {
    private double sum;
    private double count;
    private List<Double> keys;
    private List<Double> values;

    public Histogram() {
        keys = new ArrayList<>();
        values = new ArrayList<>();
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public List<Double> getKeys() {
        return keys;
    }

    public void setKeys(List<Double> keys) {
        this.keys = keys;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public void setValue(Double key, Double value) {
        keys.add(key);
        values.add(value);
    }

    public static double calc(Histogram h1 ,Histogram h2, double quantile) {
        int size = h2.values.size();
        double count = 0D;
        double allIntervalNumber = h2.count - h1.count;
        double prevInterval = 0D;
        double intervalNumber = 0D;

        for(int i= 0; i<size; i++) {
            if(prevInterval / allIntervalNumber > quantile) {
                break;
            }

            intervalNumber = h2.values.get(i) - h1.values.get(i);

            if(i == 0) {
                count += intervalNumber * h2.keys.get(i);
                prevInterval = intervalNumber;
            }else if(i < size - 1) {
                count += (intervalNumber - prevInterval) * h2.keys.get(i);
                prevInterval = intervalNumber;
            } else {
                count += (intervalNumber - prevInterval) * h2.keys.get(i-1);
            }

        }
        return intervalNumber == 0D ? 0D : count / intervalNumber;
    }

    @Override
    public String toString() {
        return "Histogram{" +
                "sum=" + sum +
                ", count=" + count +
                ", keys=" + keys +
                ", values=" + values +
                '}';
    }
}

