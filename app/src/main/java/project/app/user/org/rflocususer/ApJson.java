package project.app.user.org.rflocususer;

import org.json.JSONException;
import org.json.JSONObject;

public class ApJson {

    private String posx;
    private String posy;
    private String posz;
    private String arid;

    public ApJson(JSONObject object) {
        try {
            this.posx = object.getString("posx");
            this.posy = object.getString("posy");
            this.posz = object.getString("posz");
            this.arid = object.getString("arid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPosx() {
        return posx;
    }

    public void setPosx(String posx) {
        this.posx = posx;
    }

    public String getPosy() {
        return posy;
    }

    public void setPosy(String posy) {
        this.posy = posy;
    }

    public String getPosz() {
        return posz;
    }

    public void setPosz(String posz) {
        this.posz = posz;
    }

    public String getArid() {
        return arid;
    }

    public void setArid(String arid) {
        this.arid = arid;
    }
}