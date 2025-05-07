package network;

import integrals.RecIntegral;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class NetworkIntegrationTask extends Thread {
    private final RecIntegral rec;
    private final int row;
    private final JTable table;

    public NetworkIntegrationTask(RecIntegral rec, int row, JTable table, DefaultTableModel model) {
        this.rec = rec;
        this.row = row;
        this.table = table;
    }

    @Override
    public void run() {
        try (java.net.Socket socket = new java.net.Socket("localhost", 6005);
             java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
             java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new IntegralRequest(rec.getA(), rec.getB(), rec.getH()));
            IntegralResponse response = (IntegralResponse) in.readObject();

            SwingUtilities.invokeLater(() -> {
                table.setValueAt(response.result, row, 3);
                rec.setResult(response.result);
            });

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                table.setValueAt("Ошибка", row, 3);
            });
            e.printStackTrace();
        }
    }
}