package project.app.user.org.rflocususer;

class Ap {
    private String ssid;
    private String mac;
    private int rssi;

    Ap(String mac){
        this.ssid="RFLocus";
        this.mac=mac;
        this.rssi=0;
    }

    void setSsid(String ssid) {
        this.ssid = ssid;
    }

    String getMac() {
        return mac;
    }

    int getRssi() {
        return rssi;
    }

    void setRssi(int rssi) {
        this.rssi = rssi;
    }
}

