package integrals;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.LinkedList;
import network.NetworkIntegrationTask;

public class MainFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private LinkedList<RecIntegral> data = new LinkedList<>();

    public MainFrame() {
        setTitle("Интеграл sin(x^2)");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[]{"a", "b", "h", "Результат"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return col != 3;
            }
        };

        table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);

        JButton addBtn = new JButton("Добавить");
        JButton delBtn = new JButton("Удалить");
        JButton calcBtn = new JButton("Вычислить");
        JButton saveTxtBtn = new JButton("Сохранить TXT");
        JButton loadTxtBtn = new JButton("Загрузить TXT");
        JButton saveBinBtn = new JButton("Сохранить BIN");
        JButton loadBinBtn = new JButton("Загрузить BIN");
        JButton netCalcBtn = new JButton("Вычислить на сервере");

        addBtn.addActionListener(e -> {
            try {
                double a = Double.parseDouble(JOptionPane.showInputDialog("a:"));
                double b = Double.parseDouble(JOptionPane.showInputDialog("b:"));
                double h = Double.parseDouble(JOptionPane.showInputDialog("h:"));
                RecIntegral rec = new RecIntegral(a, b, h);
                data.add(rec);
                model.addRow(new Object[]{a, b, h, ""});
            } catch (InvalidDataException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка ввода!");
            }
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
                data.remove(row);
            }
        });

        calcBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                try {
                    double a = Double.parseDouble(model.getValueAt(row, 0).toString());
                    double b = Double.parseDouble(model.getValueAt(row, 1).toString());
                    double h = Double.parseDouble(model.getValueAt(row, 2).toString());

                    RecIntegral rec = new RecIntegral(a, b, h);
                    IntegrationTask task = new IntegrationTask(rec);
                    task.start();
                    task.join();
                    model.setValueAt(String.format("%.8f", rec.getResult()), row, 3);
                    data.set(row, rec);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Неверные данные для вычисления.");
                }
            }
        });

        netCalcBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                try {
                    double a = Double.parseDouble(model.getValueAt(row, 0).toString());
                    double b = Double.parseDouble(model.getValueAt(row, 1).toString());
                    double h = Double.parseDouble(model.getValueAt(row, 2).toString());

                    RecIntegral rec = new RecIntegral(a, b, h);
                    new NetworkIntegrationTask(rec, row, table, model).start();
                    data.set(row, rec);

                    // Временно устанавливаем статус "Вычисление..."
                    model.setValueAt("Вычисление...", row, 3);
                } catch (InvalidDataException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
                    model.setValueAt("Ошибка", row, 3);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка ввода!");
                    model.setValueAt("Ошибка", row, 3);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Выберите строку для вычисления",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            }
        });

        saveTxtBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
                    for (RecIntegral r : data) {
                        writer.write(r.getA() + "," + r.getB() + "," + r.getH() + "," + r.getResult());
                        writer.newLine();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка записи файла.");
                }
            }
        });

        loadTxtBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                    String line;
                    model.setRowCount(0);
                    data.clear();
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        double a = Double.parseDouble(parts[0]);
                        double b = Double.parseDouble(parts[1]);
                        double h = Double.parseDouble(parts[2]);
                        double res = parts.length > 3 ? Double.parseDouble(parts[3]) : 0;
                        RecIntegral r = new RecIntegral(a, b, h);
                        r.setResult(res);
                        data.add(r);
                        model.addRow(new Object[]{a, b, h, res});
                    }
                } catch (IOException | InvalidDataException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка чтения файла.");
                }
            }
        });

        saveBinBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Binary files", "bin"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(chooser.getSelectedFile()))) {
                    out.writeObject(data);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка записи бинарного файла.");
                }
            }
        });

        loadBinBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Binary files", "bin"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()))) {
                    LinkedList<RecIntegral> loaded = (LinkedList<RecIntegral>) in.readObject();
                    data = loaded;
                    model.setRowCount(0);
                    for (RecIntegral r : data) {
                        model.addRow(new Object[]{r.getA(), r.getB(), r.getH(), r.getResult()});
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка чтения бинарного файла.");
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(addBtn);
        panel.add(delBtn);
        panel.add(calcBtn);
        panel.add(saveTxtBtn);
        panel.add(loadTxtBtn);
        panel.add(saveBinBtn);
        panel.add(loadBinBtn);
        panel.add(netCalcBtn);

        add(pane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }
}