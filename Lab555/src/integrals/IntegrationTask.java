package integrals;

public class IntegrationTask extends Thread {
    private RecIntegral rec;

    public IntegrationTask(RecIntegral rec) {
        this.rec = rec;
    }

    @Override
    public void run() {
        double result = IntegrationCalculator.integrateSimpson(rec.getA(), rec.getB(), rec.getH());
        rec.setResult(result);
    }
}
