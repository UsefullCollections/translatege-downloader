import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainForm {
    public JPanel panel;
    private JComboBox<String> comboBox1;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private JTextField textField1;

    private DownloadHelper downloadHelper;

    public MainForm() {

        downloadHelper = DownloadHelper.getInstance();

        comboBox1.addItem("English");
        comboBox1.addItem("Georgian");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int language = comboBox1.getSelectedIndex();
                int threadCount;

                try {
                    threadCount = Integer.parseInt(textField1.getText());
                } catch (NumberFormatException ignored) {
                    threadCount = 0;
                }

                if (language < 0 && language > 1) {
                    statusLabel.setText("Language error");
                    return;
                }

                if (threadCount <= 0) {
                    statusLabel.setText("Thread count error");
                    return;
                }

                downloadHelper.start(language, threadCount, new Callback() {
                    @Override
                    public synchronized void updateStatus(String status) {
                        statusLabel.setText(status);
                    }
                });
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadHelper.stop();
            }
        });
    }

    public interface Callback {
        void updateStatus(String status);
    }
}
