package project.app.user.org.rflocususer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class UserActivity extends AppCompatActivity{

    private static String TAG = "UserActivity";
    private static String network = "RFLocus";
    private static String password = "oficina3";

    private PeriodicScan periodicScan;
    WifiManager wifiMgr;
    private boolean wifiInitStt;

    private List<Ap> apList;

    private String MAC1, MAC2, MAC3;
    private Integer RSS1, RSS2, RSS3;

    private String location="Bem vindo ao RFLocus";

    private TextView tvLocation;
    private Switch swtTTS;

    private TextToSpeech tts;

    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

    /**
     * Function to check if the device still connected to a network
     * @param ssid  Network SSID to be checked
     * @return boolean
     */
    private boolean isConnected(String ssid){
        wifiMgr = (WifiManager) getApplicationContext().getSystemService (Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        return wifiInfo.getSSID().equals("\""+ssid+"\"");
    }

    /**
     * Class that manager the log saving to the device's storage directory
     */
    public static class CreateLog extends Application {
        private static String TAG = "CreateLog";

        /**
         * Function to save the logcat into files on the device's storage directory
         */
        private static void createLog() {
            if (isExternalStorageWritable()) {

                //DateFormat df = new SimpleDateFormat.getDateTimeInstance("ddmmyy","hhmm");
                File appDirectory = new File(Environment.getExternalStorageDirectory() + "/rflocusUser");
                File logDirectory = new File(appDirectory + "/log");
                File logFile1 = new File(logDirectory, "logcat_default" + System.currentTimeMillis() + ".txt");
                File logFile2 = new File(logDirectory, "logcat_debug" + System.currentTimeMillis() + ".txt");

                if (!appDirectory.exists()) {
                    appDirectory.mkdir();
                    Log.d(TAG,"Creating app directory");
                }

                if (!logDirectory.exists()) {
                    logDirectory.mkdir();
                    Log.d(TAG,"Creating log directory");
                }

                try {
                    Process process = Runtime.getRuntime().exec("logcat -c");
                    process = Runtime.getRuntime().exec("logcat -f " + logFile1 + " *:I");
                    process = Runtime.getRuntime().exec("logcat -f " + logFile2 + " *:D");
                    Log.d(TAG,"Log Saved");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (isExternalStorageReadable()) {
                Log.e(TAG, "External Storage only readable");
            } else {
                Log.e(TAG, "External Storage only accessible");
            }
        }

        /***
         * Checks if external storage is available for read and write
         *
         * @return boolean
         */
        public static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state);
        }

        /***
         * Checks if external storage is available to at least read
         *
         * @return boolean
         */
        public static boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            return Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        }
    }

    /**
     * Function to automatically connect to a OPEN Wi-Fi network
     *
     * @param networkSSID String with the SSID of the desire network
     */
    private void autoConnectOPEN(String networkSSID) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        //For OPEN password
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //Add setting to WifiManager
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netID = wifiMgr.addNetwork(conf);
        wifiMgr.disconnect();
        wifiMgr.enableNetwork(netID, true);
        wifiMgr.reconnect();
        Log.d("AutoConnect", "Open");
    }

    /**
     * Function to automatically connect to a WPA Wi-Fi network
     *
     * @param networkSSID String with the SSID of the desire network
     * @param networkPass String with the password of the desire network
     */
    private void autoConnectWPA(String networkSSID, String networkPass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        //For WPA password
        conf.preSharedKey = "\"" + networkPass + "\"";
        //Add setting to WifiManager
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMgr.addNetwork(conf);
        int netID = wifiMgr.addNetwork(conf);
        wifiMgr.disconnect();
        wifiMgr.enableNetwork(netID, true);
        wifiMgr.reconnect();
        Log.d("AutoConnect", "WPA");
    }

    /**
     * Function to automatically connect to a WEP Wi-Fi network
     *
     * @param networkSSID String with the SSID of the desire network
     * @param networkPass String with the password of the desire network
     */
    private void autoConnectWEP(String networkSSID, String networkPass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        //for wep
        conf.wepKeys[0] = "\"" + networkPass + "\"";
        conf.wepTxKeyIndex = 0;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        //Add setting to WifiManager
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMgr.addNetwork(conf);
        int netID = wifiMgr.addNetwork(conf);
        wifiMgr.disconnect();
        wifiMgr.enableNetwork(netID, true);
        wifiMgr.reconnect();
    }

    /**
     * Function that returns a list of MACs for testing
     *
     * @return ArrayList with the default MAC address
     */
    private ArrayList<String> setListMAC() {
        ArrayList<String> listMacs = new ArrayList<>();
        /*
        // -----------MACS UTFPRWEB---------- //
        listMacs.add("64:ae:0c:65:7a:71");
        listMacs.add("64:ae:0c:be:71:03");
        listMacs.add("64:ae:0c:91:76:31");
        // ---------------------------------- //
        */

        // -----------MACS RFLocus---------- //
        listMacs.add("a2:20:a6:14:ea:ec");
        listMacs.add("a2:20:a6:17:37:d8");
        listMacs.add("a2:20:a6:19:10:45");
        //listMacs.add("a2:20:a6:19:0E:30");
        //listMacs.add("b8:27:eb:a3:7d:75");
        // ---------------------------------- //

        return listMacs;
    }

    /**
     * Function to request the user permission to access Fine location
     */
    private void permissionRequest(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Send user a resquest for permition
                Log.d("PerFinLoc", "request needed");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Log.d("PerFineLoc", "Permission granted");
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Log.d("PerFineLoc", "Permission denied");
            }
        }
        // other 'case' lines to check for other
        // permissions this app might request
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Log.d(TAG,"onCreate");
        CreateLog.createLog();

        // Force portrait orientation to this activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Check if Android version is superior then Lollipop
            // Check permission of FINE LOCATION
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            permissionRequest();

        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInitStt = wifiMgr.isWifiEnabled();

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        swtTTS = (Switch) findViewById(R.id.swtTTS);
        swtTTS.setChecked(true);

        autoConnectWPA(network,password);
        apList = new ArrayList<>();
        getMacList();
        startTTS();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefresh();
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        wifiMgr.setWifiEnabled(wifiInitStt);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefresh();
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startTTS();
    }

    /**
     * Function that prepares the TTS resource to run
     */
    private void startTTS(){
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR)
                    tts.setLanguage(Locale.getDefault());

                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d("TTSError", "This Language is not supported");
                    } else {
                        startRefresh();
                        Log.d("TTS","Incializado com sucesso");
                    }
                } else
                    Log.d("TTSError", "Initialization Failed!");
            }
        });
    }

    /**
     * Function to call the TTS speak method
     *
     * @param text String to be speak
     */
    private void ttsCall(String text) {
        if(!(text==null||"".equals(text))){
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                //Log.d("TSS","LOLLIPOP");
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                //Log.d("TSS","< LOLLIPOP");
            }
        }
    }

    /**
     * Function to update the information displayed in the user interface
     */
    public void updateUI() {
        if (!tvLocation.getText().toString().equals(location)) {
            tvLocation.setText(location);
            if (swtTTS.isChecked()) {
                ttsCall(location);
            }
        }
    }

    /**
     * Function to update AP information
     *
     * @param results a list of ScanResult
     * @param apList  List with the MAC's Address
     */
    private void updateAP(List<ScanResult> results, List<Ap> apList){
        for (Ap ap : apList){
            ap.setRssi(0);
        }

        for (ScanResult result : results) {
            for (Ap ap : apList) {
                if (ap.getMac().equals(result.BSSID)){
                    ap.setSsid(result.SSID);
                    ap.setRssi(result.level);
                }
            }
        }
    }

    /**
     * Function to generate the parameters of the GET method of the RESTFul Server
     *
     * @return RequestParams in the format type:type,MAC1:rssi1,MAC2:rssi2,MAC3:rssi3
     */
    private RequestParams getParams(){
        RequestParams params = new RequestParams();
        for (Ap ap : apList){
            params.put(ap.getMac(),Integer.toString(ap.getRssi()));
        }

        Log.i("ParamGET",params.toString());
        return params;
    }

    /**
     * Function to request the MAC address of the APs
     */
    private void getMacList(){
        ArrayList<String> listMacs = setListMAC();
        for (String mac : listMacs) {
            Ap ap = new Ap(mac);
            apList.add(ap);
        }
        Log.d("APList",apList.toString());
    }

    /**
     * Function to create a HTTP GET request to a server URL
     *
     * @param params RequestParams with GET parameters
     */
    private void getLocation(RequestParams params) {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept", "application/json"));

        //if (isConnected("\""+network+"\"")){
        Log.d(TAG,"Requesting GET");
        RestClient.get(UserActivity.this, "?", headers.toArray(new Header[headers.size()]), params,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("ResponseJSON",response.toString());
                        LocusJson respJson = new LocusJson(response);
                        location = respJson.getArid();
                        updateUI();
                        Log.d("AsyncHttpRH","Success on GET: "+location);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d("AsyncHttpRH","Failure to send GET" + errorResponse.toString());
                    }
                });
        //} else{
            //autoConnectWPA(network,password);
            //Log.d(TAG,"Reconectando");
        //}
    }

    /**
     * Function to stop the periodic thread
     */
    private void stopRefresh() {
        if(periodicScan != null)
            periodicScan.keepRunning = false;
        periodicScan = null;
    }

    /**
     * Function to start the periodic thread
     */
    private void startRefresh() {
        if(periodicScan == null) {
            periodicScan = new PeriodicScan();
            periodicScan.execute();
        }
        if (!periodicScan.keepRunning) {
            periodicScan = new PeriodicScan();
            periodicScan.execute();
        }
    }

    Runnable doRefresh = new Runnable() {
        @Override
        public void run() {
            refresh();
        }
    };

    /**
     * Function that scans the Wi-Fi networks. It makes sure to keep Wi-Fi active.
     */
    private void refresh() {
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiMgr.isWifiEnabled()) wifiMgr.setWifiEnabled(true);
            wifiMgr.startScan();
        List<ScanResult> results= wifiMgr.getScanResults();
        updateAP(results,apList);
        getLocation(getParams());
        updateUI();
    }

    /**
     * Class that implements AsyncTask
     */
    private class PeriodicScan extends AsyncTask<Void, Void, Void> {

        boolean keepRunning = true;
        int time = 1000;

        @Override
        protected Void doInBackground(Void... params) {
            while(keepRunning) {
                runOnUiThread(doRefresh);
                try {
                    Thread.sleep(time); // Refresh every second
                } catch (InterruptedException e) {
                    keepRunning = false;
                }
            }
            return null;
        }

    }
}
