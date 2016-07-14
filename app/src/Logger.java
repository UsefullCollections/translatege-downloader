import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String FILE_PATH = "./log.txt";
    private static OutputStream out;

    public synchronized static void println(String data) {
        System.out.println(data);
        final byte bytes[] = (getDateFormat() + "\t\t" + data + "\r\n").getBytes();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (prepare()) {
                    try {
                        out.write(bytes, 0, bytes.length);
                        close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }).start();
    }

    private static boolean prepare() {
        Path path = Paths.get(FILE_PATH);
        try {
            out = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }

    private static String getDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static void close() {
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {
        }
    }
}
