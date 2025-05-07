import network.IntegralRequest;
import network.IntegralResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static integrals.IntegrationCalculator.integrateSimpson;

public class ServerApp {
    public static void main(String[] args) {
        final int PORT = 6005;
        System.out.println("🔵 Сервер запущен...");

        try (ServerSocket serverSocket = new ServerSocket()) {
            // Настройка возможности повторного использования адреса
            serverSocket.setReuseAddress(true);
            // Привязка к порту с таймаутом
            serverSocket.bind(new InetSocketAddress(PORT), 100);

            System.out.println("🟢 Сервер слушает порт " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🟢 Клиент подключился");

                try {
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                    IntegralRequest request = (IntegralRequest) in.readObject();
                    double result = integrateSinX2Parallel(request.a, request.b, request.h);

                    out.writeObject(new IntegralResponse(result));
                } catch (Exception e) {
                    System.err.println("Ошибка обработки клиента: " + e.getMessage());
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.err.println("Ошибка закрытия сокета: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Фатальная ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static double integrateSinX2Parallel(double a, double b, double h) throws InterruptedException {
        int NUM_THREADS = 8;
        double stepSize = (b - a) / NUM_THREADS;
        Thread[] threads = new Thread[NUM_THREADS];
        double[] partialResults = new double[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++) {
            final int index = i;
            double localA = a + index * stepSize;
            double localB = (index == NUM_THREADS - 1) ? b : localA + stepSize;

            threads[i] = new Thread(() -> {
                partialResults[index] = integrateSimpson(localA, localB, h);
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        double total = 0;
        for (double res : partialResults) {
            total += res;
        }

        return total;
    }
}