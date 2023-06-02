package ua.dark.crowco;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.TextureView;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async job that is executed to refresh Crowd counter
 */
public class AsyncJob {

    private static int TIME_PREIOD = 5 * 1000; // 5 seconds

    private final RestService restService;
    private final EditText crowdCount;
    private final CheckBox checkBox;
    private final TextureView textureView;

    /**
     * Default constructor
     *
     * @param crowdCount  editText label for counter
     * @param checkBox    is crowd counter enabled
     * @param textureView surface with preview
     */
    public AsyncJob(final EditText crowdCount, final CheckBox checkBox, final TextureView textureView) {
        this.restService = new RestService();
        this.crowdCount = crowdCount;
        this.checkBox = checkBox;
        this.textureView = textureView;
    }

    /**
     * Creates async task and runs it every TIME_PERIOD and updates an UI
     */
    public void pinRegularTask() {
        final Handler asyncHandler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!checkBox.isChecked()) {
                    return;
                }
                asyncHandler.post(() -> {
                    try {
                        String encoded = takeImage(textureView.getBitmap());
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler restHandler = new Handler(Looper.getMainLooper());
                        executor.execute(() -> {
                            String value = restService.postData(encoded);
                            restHandler.post(() -> {
                                crowdCount.setText(value);
                            });
                        });
                    } catch (Exception e) {
                        LoggerWrapper.err("The response is lost");
                    }
                });
            }
        };

        timer.schedule(task, 0, TIME_PREIOD);
    }

    /**
     * Pings the server and updates an UI in background thread
     */
    public void pingService() {
        AtomicBoolean result = new AtomicBoolean(false);
        final Handler asyncHandler = new Handler();
        Executors.newSingleThreadExecutor().execute(() -> {
            asyncHandler.post(() -> {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler restHandler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    boolean value = restService.getServerStatus();
                    restHandler.post(() -> crowdCount.setText((value) ? "SERVER IS READY" : "SERVER IS DOWN"));
                });

            });
        });

    }


    /**
     * Takes image from surface and encodes it to Base64 format
     *
     * @param image Bitmap of the textureView
     * @return base64 encoded image
     */
    private String takeImage(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap resized = Bitmap.createScaledBitmap(image, (int) (image.getWidth() * 0.6), (int) (image.getHeight() * 0.6), true);
        resized.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream); // how does it affect ???
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
