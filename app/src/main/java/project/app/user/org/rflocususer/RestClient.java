package project.app.user.org.rflocususer;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class RestClient {
    private static String TAG = "RestClient";
    //private static final String BASE_URL = "http://192.168.1.82:5000/";
    //private static final String BASE_URL = "https://api.github.com/users/";
    private static final String BASE_URL = "http://192.168.100.18:5500/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(Context context, String url, Header[] headers, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
        //client.get(context, getAbsoluteUrl(url), params, responseHandler);
        Log.d(TAG,client.toString());
        //client.get(context, getAbsoluteUrl(url), responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
        //return BASE_URL;
    }
}
