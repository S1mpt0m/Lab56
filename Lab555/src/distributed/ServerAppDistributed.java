package distributed;

import network.IntegralRequest;
import network.IntegralResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAppDistributed {
    private static final int PORT = 6005;
    private static final int MAX_RETRY = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        int retryCount = 0;

        while (retryCount < MAX_RETRY) {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PORT), 50);
                System.out.println("🟢 Сервер успешно запущен на порту " + PORT);
                break;
            } catch (IOException e) {
                retryCount++;
                System.err.printf("⚠️ Попытка %d/%d: порт %d занят\n",
                        retryCount, MAX_RETRY, PORT);

                if (retryCount < MAX_RETRY) {
                    try {
                        killProcessUsingPort(PORT);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (Exception ex) {
                        System.err.println("Ошибка при освобождении порта: " + ex.getMessage());
                    }
                } else {
                    System.err.println("❌ Не удалось запустить сервер: " + e.getMessage());
                    System.exit(1);
                }
            }
        }

        try {
            System.out.println("⏳ Ожидание подключений клиентов...");

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("🔗 Новый клиент подключился");

                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                    // Получаем параметры от клиента
                    IntegralRequest request = (IntegralRequest) in.readObject();
                    System.out.printf("📩 Получен запрос: a=%.2f, b=%.2f, h=%.5f\n",
                            request.a, request.b, request.h);

                    // Вычисляем интеграл
                    double result = calculateIntegral(request.a, request.b, request.h);

                    // Отправляем результат
                    out.writeObject(new IntegralResponse(result));
                    System.out.printf("📤 Отправлен результат: %.8f\n", result);
                } catch (Exception e) {
                    System.err.println("Ошибка обработки клиента: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Фатальная ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии сервера: " + e.getMessage());
            }
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

    private static void killProcessUsingPort(int port) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            Process process = Runtime.getRuntime().exec(
                    new String[]{"cmd", "/c", "netstat -ano | findstr " + port});

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("LISTENING")) {
                        String[] parts = line.trim().split("\\s+");
                        String pid = parts[parts.length - 1];
                        Runtime.getRuntime().exec("taskkill /F /PID " + pid);
                        System.out.println("✔️ Процесс " + pid + " завершен");
                    }
                }
            }
        } else {
            Process process = Runtime.getRuntime().exec(
                    new String[]{"sh", "-c", "lsof -t -i:" + port + " | xargs kill -9"});
            process.waitFor();
            System.out.println("✔️ Процесс на порту " + port + " завершен");
        }
    }
}