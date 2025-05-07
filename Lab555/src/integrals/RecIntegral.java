package integrals;

import java.io.Serializable;

public class RecIntegral implements Serializable {
    private double a, b, h;
    private double result;

    public RecIntegral(double a, double b, double h) throws InvalidDataException {
        if (a < 0.000001 || b > 1000000 || h <= 0 || h > (b - a)) {
            throw new InvalidDataException("Недопустимые значения: от 0.000001 до 1 000 000");
        }
        this.a = a;
        this.b = b;
        this.h = h;
    }

    public double getA() { return a; }
    public double getB() { return b; }
    public double getH() { return h; }
    public double getResult() { return result; }

    public void setResult(double result) {
        this.result = result;
    }
}
