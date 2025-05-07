package network;

import java.io.Serializable;

public class IntegralRequest implements Serializable {
    public double a, b, h;

    public IntegralRequest(double a, double b, double h) {
        this.a = a;
        this.b = b;
        this.h = h;
    }
}
