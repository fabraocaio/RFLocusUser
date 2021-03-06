package project.app.user.org.rflocususer;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;

class RestClient {
    private static String TAG = "RestClient";
    private static final String BASE_URL = "http://192.168.0.1:5500/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    static void get(Context context, String url, Header[] headers, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
        Log.d(TAG,"GET Request Send");
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
