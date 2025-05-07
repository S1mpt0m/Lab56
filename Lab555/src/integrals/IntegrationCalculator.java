package integrals;

public class IntegrationCalculator {
    public static double integrateSimpson(double a, double b, double h) {
        int n = (int) ((b - a) / h);
        if (n % 2 != 0) n++;
        double sum = 0;
        for (int i = 0; i <= n; i++) {
            double x = a + i * h;
            double fx = Math.sin(x * x);
            if (i == 0 || i == n)
                sum += fx;
            else if (i % 2 == 0)
                sum += 2 * fx;
            else
                sum += 4 * fx;
        }
        return h / 3 * sum;
    }

}
