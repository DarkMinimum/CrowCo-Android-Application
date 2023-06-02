package ua.dark.crowco;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Covers REST part of this application
 */
public class RestService {

    private static final String HOST = "192.168.32.219";
    private static final String PORT = "7001";
    private static final String PREFIX = "http://";
    private static final String IMG_PREF = "img";
    private OkHttpClient client;

    /**
     * Default constructor
     */
    public RestService() {
        this.client = new OkHttpClient();
    }

    /**
     * Sends request to the python node
     *
     * @param base64
     * @return string with counted crowd
     */
    public String postData(String base64) {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add(IMG_PREF, base64)
                    .build();

            Request request = new Request.Builder()
                    .url(PREFIX + HOST + ":" + PORT)
                    .post(formBody)
                    .build();

            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body().string();
        } catch (Exception e) {
            LoggerWrapper.err("Error occurred while posting image to server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends ping request to the python node
     *
     * @return string with counted crowd
     */
    public boolean getServerStatus() {
        try {
            Request request = new Request.Builder()
                    .url(PREFIX + HOST + ":" + PORT)
                    .get()
                    .build();

            Call call = client.newCall(request);
            Response response = call.execute();
            return response.code() == 200 ? true : false;
        } catch (Exception e) {
            LoggerWrapper.err("Error occurred while posting image to server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
