import java.util.ArrayList;

public class DownloadHelper {

    private static final char[] alphabetEN = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] alphabetKA = {'ა', 'ბ', 'გ', 'დ', 'ე', 'ვ', 'ზ', 'თ', 'ი', 'კ', 'ლ', 'მ', 'ნ', 'ო', 'პ', 'ჟ', 'რ', 'ს', 'ტ', 'უ', 'ფ', 'ქ', 'ღ', 'ყ', 'შ', 'ჩ', 'ც', 'ძ', 'წ', 'ჭ', 'ხ', 'ჯ', 'ჰ'};

    private static DownloadHelper sInstance;
    private ArrayList<Downloader> threadContainer;
    private Callback callback;
    private MainForm.Callback statusCallback;
    private boolean stopMessage;

    private DownloadHelper() {
        stopMessage = false;
        threadContainer = new ArrayList<>();
        callback = getCallback();
    }

    public static DownloadHelper getInstance() {
        if (sInstance == null) sInstance = new DownloadHelper();
        return sInstance;
    }

    public void start(int language, int threadCount, MainForm.Callback callback) {
        char[] alphabet = language == 0 ? alphabetEN : alphabetKA;
        stopMessage = false;
        statusCallback = callback;

        for (int i = 0; i < threadCount; i++) {
            startTask(alphabet, i);
        }
    }

    private synchronized void startTask(final char[] alphabet, final int index) {
        if (stopMessage) return;
        final Downloader thread = new Downloader();
        threadContainer.add(thread);
        statusCallback.updateStatus("Progress: " + threadContainer.size() + "/" + alphabet.length);

        new Thread(new Runnable() {
            @Override
            public void run() {
                thread.start(Character.toString(alphabet[index]), alphabet, callback);
            }
        }).start();
    }

    public void stop() {
        stopMessage = true;
        for (Downloader obj: threadContainer) {
            obj.sendStopMessage();
        }
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public synchronized void taskEnded(final char[] alphabet) {
                final int maxTaskCount = alphabet.length;
                final int currentTaskCount = threadContainer.size();
                if (currentTaskCount >= maxTaskCount) {
                    statusCallback.updateStatus("Completed");
                    return;
                }

                startTask(alphabet, currentTaskCount);
            }
        };
    }

    public interface Callback {
        void taskEnded(char[] alphabet);
    }
}
