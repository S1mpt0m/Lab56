package distributed;

import network.IntegralRequest;
import network.IntegralResponse;

import java.io.*;
import java.net.Socket;

public class Client1 {
    private static final String HOST = "localhost";
    private static final int PORT = 6005;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("🧮 Подключено к серверу " + HOST + ":" + PORT);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            IntegralRequest request = (IntegralRequest) in.readObject();
            System.out.printf("📤 Получено задание: [%.2f, %.2f] h=%.5f\n",
                    request.a, request.b, request.h);

            double result = calculateIntegral(request.a, request.b, request.h);
            out.writeObject(new IntegralResponse(result));

            System.out.printf("✅ Результат отправлен: %.8f\n", result);

        } catch (Exception e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static double calculateIntegral(double a, double b, double h) {
        // Реализация метода Симпсона
        int n = (int) ((b - a) / h);
        if (n % 2 != 0) n++;
        double sum = 0;

        for (int i = 0; i <= n; i++) {
            double x = a + i * h;
            double fx = Math.sin(x * x);
            if (i == 0 || i == n) {
                sum += fx;
            } else if (i % 2 == 0) {
                sum += 2 * fx;
            } else {
                sum += 4 * fx;
            }
        }
        return h / 3 * sum;
    }
}