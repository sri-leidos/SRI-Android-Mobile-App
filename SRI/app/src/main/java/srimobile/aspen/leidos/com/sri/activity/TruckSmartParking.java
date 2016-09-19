package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;


import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.gps.CoordinateChecker;
import srimobile.aspen.leidos.com.sri.gps.GPSTracker;
import srimobile.aspen.leidos.com.sri.utils.CalculateDistance;
import srimobile.aspen.leidos.com.sri.utils.TurnOnLocationServicesGPS;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.GeomagneticField;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;

import android.os.IBinder;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TruckSmartParking extends Activity implements GpsStatus.Listener, GpsStatus.NmeaListener, LocationListener {

    int i = 0;
    private GPSTracker gps = null;
    private ServiceConnection mConnection;
    private boolean mBound;
    private Location location = null;
    CoordinateChecker coordinateChecker;

    private long startTime = 0;
    private long endTime = 0;
    private static double bearing;

    private static Location prevLocation = new Location("start location");
    private static Location currLocation = new Location("end location");
    public static double distance = 0.0;
    public static double speed = 20.0;
    String truckSmartParkingUrl = "";
    GridView gridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weigh_station_dynamic_layout);

        if (mConnection == null) {
            mConnection = new GPSServiceConnection();
        }

        coordinateChecker = new CoordinateChecker();

        Intent intent = new Intent(this, GPSTracker.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        TruckSmartParking.speed = 20.0;

        // DISABLE SCREEN LOCK
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mConnection == null) {
            mConnection = new GPSServiceConnection();
        }

        Intent intent = new Intent(this, GPSTracker.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // UPDATE GPS EVERY 10 SECONDS
        if (gps != null && gps.getLocationManager() != null) {
            List<String> gpsProviders = gps.getLocationManager().getAllProviders();
            for (String provider : gpsProviders) {
                gps.getLocationManager().requestLocationUpdates(provider, (10 * 1000), 1.0f, (LocationListener)this);
            }
        }
        // VIEW PROFILE
        Button profileClk = (Button)findViewById(R.id.profileBtn);
        profileClk.setOnClickListener(profilePageRedirect);

        // START ADD TRUCKPARKING VIEWS
        LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        dummyParams.weight = 1f;

        View truckParkLayout = (View)getLayoutInflater().inflate(R.layout.truck_parking_activity, null);
        truckParkLayout.setLayoutParams(dummyParams);

        LinearLayout truckSmartParking = (LinearLayout)findViewById(R.id.weighStationDynamic);
        truckSmartParking.addView(truckParkLayout);
        // END ADD TRUCKPARKING VIEWS

        gridView = (GridView)findViewById(R.id.gridView1);

        Intent gpsLoc = getIntent();
        Double lat = gpsLoc.getDoubleExtra("LATITUDE", 0.0);
        Double lon = gpsLoc.getDoubleExtra("LONGITUDE", 0.0);

        if  (location == null) {
            location = new Location("");
        }

        if (lat != 0.0) {
            location.setLatitude(lat);
            location.setLongitude(lon);

            onLocationChanged(location);
        }

        TruckSmartParking.speed = 20;

        TurnOnLocationServicesGPS turnOnLocationServicesGPS = new TurnOnLocationServicesGPS(this);
        turnOnLocationServicesGPS.turnOffAirplaneMode();
        turnOnLocationServicesGPS.turnOnGpsMode();
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mConnection = null;
            mBound = false;

            if (gps != null && gps.getLocationManager() != null) {
                gps.getLocationManager().removeUpdates(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
        Intent intent = new Intent(TruckSmartParking.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // call this to finish the current activity
    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(TruckSmartParking.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // call this to finish the current activity

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    View.OnClickListener profilePageRedirect = new View.OnClickListener() {
        public void onClick(View v) {

//            Intent intent = new Intent(TruckSmartParking.this, ProfileActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
            finish();
        }
    };


    @Override
    public void onLocationChanged(Location location) {
        try {
            this.location = location;
            if (location == null) {
                return;
            }

            Double gpsLatitude = location.getLatitude();
            Double gpsLongitude = location.getLongitude();

            Log.d("TRUCKSMARTPARKING", Double.valueOf(gpsLatitude) + ", " + Double.valueOf(gpsLongitude));

            String gateName = coordinateChecker.gate_name_coordinate(gpsLatitude, gpsLongitude);

            if (gateName.toUpperCase().contains("APPROACH")) {
                if (gps != null && gps.getLocationManager() != null) {
                    gps.getLocationManager().removeUpdates(this);
                }

                Intent intent = new Intent(TruckSmartParking.this, RedGreenSignalActivity.class);
    //            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("LATITUDE", location.getLatitude());
                intent.putExtra("LONGITUDE", location.getLongitude());
                startActivity(intent);
                finish();
            } else {

                    endTime = startTime;
                    startTime = System.currentTimeMillis();
                    TruckSmartParking.distance = CalculateDistance.distanceLocation(location, currLocation, "M");
                    TruckSmartParking.speed = CalculateDistance.calculateSpeed(distance, startTime, endTime);

                    truckSmartParkingUrl = "https://onlineparkingnetwork.net/api/v1/truckstops/ahead.json?lat=" + gpsLatitude + "&lon=" + gpsLongitude + "&bearing=" + getBearing(location);

    //            String truckSmartParkingUrl = "https://onlineparkingnetwork.net/api/v1/truckstops/ahead.json?lat=" + gpsLatitude + "&lon=" + gpsLongitude ;
                    Log.d("BEARING", truckSmartParkingUrl);
                    new TruckStopsTask().execute(truckSmartParkingUrl);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception" + e, Toast.LENGTH_SHORT);
        }
    }



    public static double getBearing(Location loc) {

        try {

            if (
                (loc != null) &&
                (currLocation != null) &&
                (loc.getLatitude() != currLocation.getLatitude()) &&
                (loc.getLongitude() != currLocation.getLongitude())
            ){
                prevLocation.setLatitude(currLocation.getLatitude());
                prevLocation.setLongitude(currLocation.getLongitude());

                currLocation.setLatitude(loc.getLatitude());
                currLocation.setLongitude(loc.getLongitude());
            }


//        bearing = Double.valueOf(prevLocation.bearingTo(currLocation));

            double startLat = prevLocation.getLatitude();
            double endLat= currLocation.getLatitude();

            double longitude1 = prevLocation.getLongitude();
            double longitude2 = currLocation.getLongitude();
            double latitude1 = Math.toRadians(startLat);
            double latitude2 = Math.toRadians(endLat);
            double longDiff= Math.toRadians(longitude2-longitude1);
            double y= Math.sin(longDiff)*Math.cos(latitude2);
            double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

            TruckSmartParking.bearing = (Math.toDegrees(Math.atan2(y, x))+360)%360;

            if (TruckSmartParking.bearing <0){
                return (180+(180+TruckSmartParking.bearing ));
            }
            else{
                return TruckSmartParking.bearing;
            }

        } catch (Exception e) {
            Log.d("GET BEARING", e.toString());
        }

        return TruckSmartParking.bearing;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {

    }

    class TruckStopDetailsVo {

        public String getBitmapUrl() {
            return bitmapUrl;
        }

        public void setBitmapUrl(String bitmapUrl) {
            this.bitmapUrl = bitmapUrl;
        }

        private String bitmapUrl;
        private String name;
        private String location_text;

        private Double toLatitude;
        private Double toLongitude;

        private String distance;
        public String gps;
        public String bearing;
        public String tspsurl;


        public String getBearing() {
            return bearing;
        }

        public void setBearing(String bearing) {
            this.bearing = bearing;
        }

        public String getGps() {
            return gps;
        }

        public void setGps(String gps) {
            this.gps = gps;
        }

        public String getTspsurl() {
            return tspsurl;
        }

        public void setTspsurl(String tspsurl) {
            this.tspsurl = tspsurl;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public Double getToLatitude() {
            return toLatitude;
        }

        public void setToLatitude(Double toLatitude) {
            this.toLatitude = toLatitude;
        }

        public Double getToLongitude() {
            return toLongitude;
        }

        public void setToLongitude(Double toLongitude) {
            this.toLongitude = toLongitude;
        }

        public String getLocation_text() {
            return location_text;
        }

        public void setLocation_text(String location_text) {
            this.location_text = location_text;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public void updateGrid(String jsnArr) {

        try {

            GridView gridView = (GridView) findViewById(R.id.gridView1);

            JSONArray jsonArrTsps = new JSONArray(jsnArr);

            ArrayList<TruckStopDetailsVo> truckStopDetails = new ArrayList<TruckStopDetailsVo>();

            for(int x = 0; x < jsonArrTsps.length() ; x++) {

                JSONObject jsonObj = jsonArrTsps.getJSONObject(x);
                Iterator<String> tspsKeyArr = jsonObj.keys();

                String keyTSPSstr = tspsKeyArr.next();

                TruckStopDetailsVo tsd = new TruckStopDetailsVo();
                tsd.setLocation_text(jsonObj.getString("location_text"));
                tsd.setName(jsonObj.getString("name"));
                tsd.setToLatitude(Double.parseDouble(jsonObj.getString("latitude")));
                tsd.setToLongitude(Double.parseDouble(jsonObj.getString("longitude")));

                tsd.setBearing(String.valueOf(getBearing(location)));
                tsd.setTspsurl(truckSmartParkingUrl);

                tsd.setBitmapUrl(jsonObj.getString("logo_thumb_url"));

                Double fromLatitudeUpd = location.getLatitude();
                Double fromLongitudeUpd = location.getLongitude();
//                Double fromLatitudeUpd = 38.5852654;
//                Double fromLongitudeUpd = -89.92754;
                Double toLatitudeUpd = tsd.getToLatitude();
                Double toLongitudeUpd = tsd.getToLongitude();

                Double distTo = CalculateDistance.distance(fromLatitudeUpd, fromLongitudeUpd, toLatitudeUpd, toLongitudeUpd, "M");
                DecimalFormat df = new DecimalFormat("#.##");
                String calcDistanceMiles = df.format(distTo);
                tsd.distance = calcDistanceMiles + " Miles";

                truckStopDetails.add(tsd);

//                Log.d("KEY", keyTSPSstr + "  " +  jsonObj.get(keyTSPSstr ));
            }

            TruckSmartParkingCustomGridAdapter cga = null;
            if (truckStopDetails.size() > 0) {
                cga = new TruckSmartParkingCustomGridAdapter(this, truckStopDetails);
            } else {
                truckStopDetails.add(noTruckStopDetailsResults(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()), String.valueOf(getBearing(location))));
                cga = new TruckSmartParkingCustomGridAdapter(this, truckStopDetails);
            }
            gridView.setAdapter(cga);

        } catch (Exception e) {

            Log.d("EXC", e.toString());
            Log.d("EXC", e.toString());

        }

    }

    private class TruckStopsTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            try {

                JSONArray jsnArr = null;

                for (String url : params) {

                    try {

                        HttpUriRequest request = new HttpGet(url); // Or HttpPost(), depends on your needs
                        String credentials = "ARSKdc9zJjCvi6rjD53E" + ":" + "";
                        String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        request.addHeader("Authorization", "Basic " + base64EncodedCredentials);

                        HttpClient httpclient = new DefaultHttpClient();
                        HttpResponse response = httpclient.execute(request);

                        StringBuffer result = new StringBuffer();

                        // PUT JSON RESULT INTO A STRING 'result'
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

                        String line = "";
                        while ((line = reader.readLine()) != null) {

                            result.append(line);
                        }

                        jsnArr = new JSONArray(result.toString());

                        for (int x = 0; x < jsnArr.length(); x++) {

                            JSONObject jsonObj = jsnArr.getJSONObject(x);
                            Iterator<String> tspsKeyArr = jsonObj.keys();

                            while (tspsKeyArr.hasNext()) {
                                String keyTSPSstr = tspsKeyArr.next();
                            }

                        }
                    } catch (Exception e) {

                        Log.d("err", e.toString());
                        Log.d("err", e.toString());
                    }

//                updateGrid(jsnArr.toString());
                    if (jsnArr == null) {
                        return "";
                    } else {
                        System.out.println(jsnArr.toString());
                        return jsnArr.toString();
                    }
                }
            } catch (Exception e) {
                Log.d("PARK TASK", e.toString());
                Log.d("PARK TASK", e.toString());

            }

            return "";
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateGrid(s);
        }
    }

    public TruckStopDetailsVo noTruckStopDetailsResults(String lat, String lon, String bearing) {

        TruckStopDetailsVo tsd = new TruckStopDetailsVo();
        tsd.setLocation_text("Lat: " + lat + " Lon:" + lon + " Bearing:" + bearing );
        tsd.setName("NO REST AREAS");
        tsd.setToLatitude(0.0);
        tsd.setToLongitude(0.0);

        tsd.setBearing(String.valueOf(getBearing(location)));
        tsd.setTspsurl(truckSmartParkingUrl);

        tsd.setBitmapUrl("");

        return tsd;

    }

    private class GPSServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {


                // We've bound to LocalService, cast the IBinder and get LocalService instance
                gps = ((GPSTracker.GPSTrackerBinder) service).getTheService();
    //            gps.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 * 1000, 0, TruckSmartParking.this);
                gps.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 0, TruckSmartParking.this);
                Toast.makeText(TruckSmartParking.this, "GPS Service Bound", Toast.LENGTH_LONG);
                mBound = true;

                Location gpsloc = new Location("");
                gpsloc = gps.getLocation();
                if (gpsloc != null) {
    //                String truckSmartParkingUrl = "https://onlineparkingnetwork.net/api/v1/truckstops/ahead.json?lat=" + gpsloc.getLatitude() + "&lon=" + gpsloc.getLongitude() + "&bearing=W";
                    String truckSmartParkingUrl = "https://onlineparkingnetwork.net/api/v1/truckstops/ahead.json?lat=" + gpsloc.getLatitude() + "&lon=" + gpsloc.getLongitude() + "&bearing=" + getBearing(gpsloc);

                    // calculate speed for truck parking
                    endTime = startTime;
                    startTime = System.currentTimeMillis();
                    TruckSmartParking.distance = CalculateDistance.distanceLocation(location, currLocation, "M");

                    TruckSmartParking.speed = 20;
                    if (location.getLatitude() != 0 || location.getLatitude() != 0) {
                        TruckSmartParking.speed = CalculateDistance.calculateSpeed(distance, startTime, endTime);
                    }

    //            TruckSmartParking.speed = location.getSpeed() * 2.2369;

                    new TruckStopsTask().execute(truckSmartParkingUrl);
                }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    }


}
