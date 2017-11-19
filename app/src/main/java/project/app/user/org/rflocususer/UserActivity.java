package project.app.user.org.rflocususer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

public class UserActivity extends AppCompatActivity{

    private static String TAG = "UserActivity";
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
        listMacs.add("6a:39:a3:67:51:8e");
        listMacs.add("df:11:bb:8f:a8:7a");
        listMacs.add("19:b3:82:86:06:6e");
        //listMacs.add("c7:45:50:51:06:d1");
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

        apList = new ArrayList<>();
        getMacList();

        startTTS();
        //autoConnectOPEN("UTFPRWEB");
        //autoConnectWAP("FUNBOX-BOARDGAME-CAFE","Fb-4130400780");
        //getMacList();
        setAps();
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
                        Log.d("error", "This Language is not supported");
                    } else {
                        startRefresh();
                    }
                } else
                    Log.d("error", "Initialization Failed!");
            }
        });
    }

    // To be Eliminated
    private void setAps() {
        //MAC1 = "FF:FF:FF:FF:FF:01";
        MAC1 = "6a:39:a3:67:51:8e";
        RSS1 = 3;

        //MAC2 = "FF:FF:FF:FF:FF:02";
        MAC2 = "df:11:bb:8f:a8:7a";
        RSS2 = 6;

        //MAC3 = "FF:FF:FF:FF:FF:03";
        MAC3 = "19:b3:82:86:06:6e";
        RSS3 = 8;
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
     * Função para atualizar as variaveis globais referentes aos Aps
     *
     * @param results a list of ScanResult
     * @param list    an ArrayList with the MAC Address or SSID
     * @param opc     a Integer witch determinate if the ArrayList contains MAC or SSID
     */
    public void updateAP(List<ScanResult> results, ArrayList list, int opc) {
        RSS1 = RSS2 = RSS3 = 0;

        switch (opc) {

            case 0: {
                for (ScanResult result : results) {
                    if (result.BSSID.equals(list.get(0))) {
                        MAC1 = result.BSSID;
                        RSS1 = result.level;
                    } else if (result.BSSID.equals(list.get(1))) {
                        MAC2 = result.BSSID;
                        RSS2 = result.level;
                    } else if (result.BSSID.equals(list.get(2))) {
                        MAC3 = result.BSSID;
                        RSS3 = result.level;
                    }
                }
            }
            break;
            case 1: {
                for (ScanResult result : results) {
                    if (result.SSID.equals(list.get(0))) {
                        MAC1 = result.BSSID;
                        RSS1 = result.level;
                    } else if (result.SSID.equals(list.get(1))) {
                        MAC2 = result.BSSID;
                        RSS2 = result.level;
                    } else if (result.SSID.equals(list.get(2))) {
                        MAC3 = result.BSSID;
                        RSS3 = result.level;
                    }
                }
            }
            break;
            case 2: {
                int i = 0;
                for (ScanResult result : results) {
                    if (i == 0) {
                        MAC1 = result.BSSID;
                        RSS1 = result.level;
                    } else if (i == 1) {
                        MAC2 = result.BSSID;
                        RSS2 = result.level;
                    } else if (i == 2) {
                        MAC3 = result.BSSID;
                        RSS3 = result.level;
                    }
                    i++;
                }
            }
            break;
        }
    }

    /**
     * Function to update AP information
     *
     * @param results a list of ScanResult
     * @param apList  List with the MAC's Address
     */
    private void updateAP(List<ScanResult> results, List<Ap> apList){
        for (ScanResult result : results) {
            for (Ap ap : apList) {
                ap.setRssi(0);
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
        RequestParams params2 = new RequestParams();
        params.put(MAC1, Integer.toString(RSS1));
        params.put(MAC2, Integer.toString(RSS2));
        params.put(MAC3, Integer.toString(RSS3));

        for (Ap ap : apList){
            params2.put(ap.getMac(),Integer.toString(ap.getRssi()));
        }

        Log.i("ParamGET 1",params.toString());
        Log.i("ParamGet 2",params2.toString());
        return params2;
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
        Log.i("APList",apList.toString());
    }

    /**
     * Function to create a HTTP GET request to a server URL
     *
     * @param params RequestParams with GET parameters
     */
    private void getLocation(RequestParams params) {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Accept", "application/json"));

        //RestClient.get(this, "api/notes", headers.toArray(new Header[headers.size()]), null
        RestClient.get(UserActivity.this, "?", headers.toArray(new Header[headers.size()]), params,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        LocusJson respJson = new LocusJson(response);
                        location = respJson.getArid();
                        updateUI();
                        Log.d("AsyncHttpRH","Success on GET: "+location);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d("AsyncHttpRH","Failure to send GET");
                    }
                });
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
        ArrayList macs = setListMAC();
        updateAP(results,macs,0);
        updateAP(results,apList);
        getLocation(getParams());
        updateUI();
        //Log.d("AutoRefresh","Scan Completed");
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
