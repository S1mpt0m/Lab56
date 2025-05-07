package network;

import java.io.Serializable;

public class IntegralResponse implements Serializable {
    public double result;

    public IntegralResponse(double result) {
        this.result = result;
    }
}
