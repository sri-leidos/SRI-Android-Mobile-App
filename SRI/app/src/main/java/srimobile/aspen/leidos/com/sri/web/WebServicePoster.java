package srimobile.aspen.leidos.com.sri.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;

import srimobile.aspen.leidos.com.sri.activity.ProfileActivity;

/**
 * Created by blackch on 6g/1/2015.
 */
public class WebServicePoster {

    // Production
    public static final String APPROACH_URL = "http://sri.leidosweb.com/DashCon/resources/truck/approachEnter";
    public static final String WIM_ENTER = "http://sri.leidosweb.com/DashCon/resources/truck/wimEnter";
    public static final String SAVE_IMAGE = "http://sri.leidosweb.com/DashCon/resources/truck/saveImage";
    public static final String WIM_LEAVE = "http://sri.leidosweb.com/DashCon/resources/truck/wimLeave";
    public static final String LOGIN_URL = "http://sri.leidosweb.com/DashCon/j_security_check";

//    public static final String APPROACH_URL = "http://192.168.43.15:8080/DashCon/resources/truck/approachEnter";
//    public static final String WIM_ENTER = "http://192.168.43.15:8080/DashCon/resources/truck/wimEnter";
//    public static final String WIM_LEAVE = "http://192.168.43.15:8080/DashCon/resources/truck/wimLeave";
//    public static final String SAVE_IMAGE = "http://192.168.43.15:8080/DashCon/resources/truck/saveImage";
//    public static final String LOGIN_URL = "http://192.168.43.15:8080/DashCon/j_security_check";

    // iPhone
//    private static final String APPROACH_URL = "http://172.20.10.3:8080/DashCon/resources/truck/approachEnter";
//    private static final String WIM_ENTER = "http://172.20.10.3:8080/DashCon/resources/truck/wimEnter";
//    public static final String SAVE_IMAGE = "http://172.20.10.3:8080/DashCon/resources/truck/saveImage";
//    private static final String WIM_LEAVE = "http://172.20.10.3:8080/DashCon/resources/truck/wimLeave";
//    public static final String LOGIN_URL = "http://172.20.10.3:8080/DashCon/j_security_check";

    public HttpResponse postToWebService(String url, JSONObject jsonObject) {

        int token = 0;
        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            HttpPost httpPost = new HttpPost(url);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            ByteArrayEntity baEntity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF8"));
            baEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(baEntity);

            System.out.println("JSON -> " + jsonObject.toString());

            HttpResponse httpResponse = httpClient.execute(httpPost);
            return httpResponse;
        } catch (Exception e) {
            System.out.println("Exception -> " + e.toString());
        }

        return null;
    }


    private JSONObject getJSONObject(Context context,double lat, double lon, int siteId, int wsTrackingId) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("SRI", Context.MODE_PRIVATE);

        JSONObject jsonObject = new JSONObject();
        if(wsTrackingId != 0) {
            jsonObject.accumulate("id", wsTrackingId);
        }

        jsonObject.accumulate("siteId", siteId);
        jsonObject.accumulate("timestamp", new Timestamp(Calendar.getInstance().getTime().getTime()));
        jsonObject.accumulate("licensePlate", prefs.getString("lp", "License Plate"));
        jsonObject.accumulate("imageUrl", "imgURL_TBD");
        jsonObject.accumulate("driversLicense", prefs.getString("dl", "Driver's License"));
        jsonObject.accumulate("commercialDriversLicense", prefs.getString("cdl", "Commercial Driver's License"));
        jsonObject.accumulate("vin", prefs.getString("vin", "Vehicle Identification Number"));
        jsonObject.accumulate("usdotNumber", prefs.getString("usdot", "USDOT Number"));
        jsonObject.accumulate("latitude", lat);
        jsonObject.accumulate("longitude", lon);
        jsonObject.accumulate("mobileAppVersion", ProfileActivity.versionName ); //ProfileActivity.versionName 1.1.1 ProfileActivity.versionCode 3

        return jsonObject;
    }



    private String getObjectXML(){

        return null;
    }


    public int callApproach(Context context, double lat, double lon, int siteId, int wsTrackingId){
        int retId = 0;
        try {
            Log.d("SRI-WSP","Getting JSON");
            JSONObject jsonObject = getJSONObject(context, lat, lon, siteId, wsTrackingId);
            Log.d("SRI-WSP","Calling WS");
            HttpResponse response = postToWebService(APPROACH_URL, jsonObject);
            Log.d("SRI-WSP","Processing Response");
            String result = EntityUtils.toString(response.getEntity());
            JSONObject retObj = new JSONObject(result);
            Log.d("SRI-WSP","Getting ID value");
            retId = retObj.getInt("id");
            Log.d("SRI-WSP","Have id: "+retId);
        } catch (JSONException e) {
            Log.e("SRI-WSP","Error parsing JSON",e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("SRI-WSP","IOE",e);
            e.printStackTrace();
        }

        return retId;
    }




    public void callWimEnter(Context context, double lat, double lon, int siteId, int wsTrackingId){
        try {
//            JSONObject jsonObject = getJSONObject(context, lat, lon, siteId, wsTrackingId, ProfileActivity.versionName, ProfileActivity.versionCode);
            JSONObject jsonObject = getJSONObject(context, lat, lon, siteId, wsTrackingId);
            HttpResponse response = postToWebService(WIM_ENTER, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String callWimLeave(Context context, double lat, double lon, int siteId, int wsTrackingId){
        try {

            JSONObject jsonObject = getJSONObject(context, lat, lon, siteId, wsTrackingId);
            HttpResponse response = postToWebService(WIM_LEAVE, jsonObject);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject obj = new JSONObject(result);
            return obj.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void populateTruckFeedData(String truckFeedjson) {

        JSONObject truckFeedJson = new JSONObject();

    }


}
