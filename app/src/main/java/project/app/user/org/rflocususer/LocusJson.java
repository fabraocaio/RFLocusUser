package project.app.user.org.rflocususer;

import org.json.JSONException;
import org.json.JSONObject;

class LocusJson {

    private String posx;
    private String posy;
    private String posz;
    private String arid;

    LocusJson(JSONObject object) {
        try {
            this.posx = object.getString("posx");
            this.posy = object.getString("posy");
            this.posz = object.getString("posz");
            this.arid = object.getString("arid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    String getArid() {
        return arid;
    }
}