package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.gps.CoordinateChecker;
import srimobile.aspen.leidos.com.sri.gps.GPSTracker;
import srimobile.aspen.leidos.com.sri.utils.ApproachingWeighStationAudioSvc;
import srimobile.aspen.leidos.com.sri.utils.FailWeightAudioSvc;
import srimobile.aspen.leidos.com.sri.utils.PassWeightAudioSvc;
import srimobile.aspen.leidos.com.sri.utils.TurnOnLocationServicesGPS;
import srimobile.aspen.leidos.com.sri.web.OkHttpUpload;
import srimobile.aspen.leidos.com.sri.web.WebServicePoster;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;


/**
 * Created by walswortht on 5/14/2015.
 */
public class RedGreenSignalActivity extends Activity implements LocationListener {

    private DrawingView         drawingView;
    private Location            location;
    private boolean             mBound;
    private boolean             GPS_WIM         = false;
    private boolean             isWeightOver    = false;
    private WebServicePoster webServicePoster;
    private ServiceConnection   mConnection;
    public GPSTracker gps;
    private WebServicePoster poster = new WebServicePoster();

    Intent serviceIntent = null;
    boolean isWsApproachCalled = false;
    boolean isWsWimCalled = false;
    boolean isWsExitWimCalled = false;

    public RedGreenSignalActivity() {
        super();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weigh_station_layout);

        webServicePoster = new WebServicePoster();

        // PROFILE ACTIVITY BUTTON CLICK
        Button profileClk = (Button)findViewById(R.id.wsl_profileBtn);
        profileClk.setOnClickListener(profilePageRedirect);

        // TURN ON GPS POLLING FOR RedGreenSignalActivity
        if (drawingView == null) {

            // CREATE ANDROID SURFACEVIEW
            drawingView = new DrawingView(this);
            SurfaceCallback surfaceCallback = new SurfaceCallback();
            drawingView.getHolder().addCallback(surfaceCallback);
        }

        // DISABLE SCREEN LOCK
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        TruckSmartParking.speed = 20;

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d("RedGreenSignalActivity", "START Start");


        try {


            TruckSmartParking.speed = 20;

            // START GPS LOCATION SERVICE
            // RedGreenSigngalActivity LISTEN for GPS POLLING LOCATION UPDATES
            if (mConnection == null) {
                mConnection = new GPSServiceConnection();
            }

            Intent intent = new Intent(this, GPSTracker.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            // SET GPS POLLING INTERVAL
            if (gps != null && gps.getLocationManager() != null) {
                List<String> gpsProviders = gps.getLocationManager().getAllProviders();
                for (String provider : gpsProviders) {
                    gps.getLocationManager().requestLocationUpdates(provider, 100, 0, (LocationListener)this);
                }
            }
            // END START GPS LOCATION SERVICE

            // START ADD WEIGH STATION VIEWS
            LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            dummyParams.weight = 1f;

            // START ADDING WEIGH STATION VIEW
            LinearLayout linLayout = new LinearLayout(this);
            linLayout.setLayoutParams(dummyParams);
            linLayout.addView(drawingView);

            // CREATE weighStationDynamic FROM weigh_station_dynamic_layout
            LinearLayout wsdv = (LinearLayout)findViewById(R.id.wsl_weighStationDynamic);
            wsdv.addView(linLayout);

            // SET WIM TO FALSE WHEN ENTERING RedGreenSignalActvity
            GPS_WIM = false;
            // END ADDING WEIGH STATION VIEW

            Log.d("RedGreenSignalActivity", "END Start");

            TurnOnLocationServicesGPS turnOnLocationServicesGPS = new TurnOnLocationServicesGPS(this);
            turnOnLocationServicesGPS.turnOffAirplaneMode();
            turnOnLocationServicesGPS.turnOnGpsMode();

        } catch (Exception e) {
            Log.d("REDGREEN START", e.toString());
        }

    }

    @Override
    protected void onStop() {
        // Unbind from the service
        super.onStop();
        if (mBound) {
            if (mConnection != null) {

                unbindService(mConnection);
                drawingView = null;
                mBound = false;

                // REMOVE POLLING FROM RedGreenSignalActivity
                if (gps != null && gps.getLocationManager() != null) {
                    gps.getLocationManager().removeUpdates(this);
                }
            }
        }

        GPS_WIM = false;
    }


    //
    View.OnClickListener profilePageRedirect = new View.OnClickListener() {
        public void onClick(View v) {

            Intent intent = new Intent(RedGreenSignalActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // call this to finish the current activity
        }
    };

    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
        Intent intent = new Intent(RedGreenSignalActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // call this to finish the current activity
    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(RedGreenSignalActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // call this to finish the current activity

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (holder.getSurface().isValid()) {

                Intent gpsLoc = getIntent();
                Double lat = gpsLoc.getDoubleExtra("LATITUDE", 0.0);
                Double lon = gpsLoc.getDoubleExtra("LONGITUDE", 0.0);

                Location loc = new Location("");
                loc.setLatitude(lat);
                loc.setLongitude(lon);

                drawingView.determineGeoFence(loc);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    // START - SURFACEVIEW
    class DrawingView extends SurfaceView {

        private final SurfaceHolder surfaceHolder;

        int wsTrackingId = 0;

        Paint redCirclePaint = new Paint();
        Paint greenCirclePaint = new Paint();

        private Context parentContext;

        CoordinateChecker coordinateChecker = new CoordinateChecker();

        public DrawingView(Context context) {
            super(context);

            parentContext = context;

            surfaceHolder = getHolder();

            isWeightOver = false;
        }

        public void playAudio() {

            try {

                AudioManager m_amAudioManager;
                m_amAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                m_amAudioManager.setMode(AudioManager.MODE_IN_CALL);
                m_amAudioManager.setSpeakerphoneOn(true);

            } catch (Exception e) {
                Log.d("AUDIO", e.toString());
            }
        }

        public void determineGeoFence(Location location) {

            Double _lat = location.getLatitude();
            Double _long = location.getLongitude();

            String gateName = coordinateChecker.gate_name_coordinate(_lat, _long);
            String currentWeighStation = coordinateChecker.maxGps(location);
            int station_id = coordinateChecker.gateId(location);

            Canvas canvas = null;

            // entering wim audio stop audio service
            Intent objIntent = null;

            try {
//
                    if (surfaceHolder.getSurface().isValid()) {

                        canvas = surfaceHolder.lockCanvas();

                        if (
                                currentWeighStation.length() != 0 // in weigh station
                                        &&
                                (
                                        gateName.toUpperCase().contains("APPROACH")
                        )
                        &&
                            !GPS_WIM
                        ) {
                            Log.d("APPROACH", "START APPROACH");
                            initialWeighStationEntered(canvas);

                            Log.d("APPROACH", "SEND TRUCK INFO");


                        if (!isWsApproachCalled) {

                            // wsTrackingId set to 0 as a default
                            isWsApproachCalled = true;
                            wsTrackingId = poster.callApproach(parentContext, location.getLatitude(), location.getLongitude(), station_id, 0);

                            SharedPreferences preferences = parentContext.getSharedPreferences("SRI", Context.MODE_PRIVATE);
                            String truckImageLocation = preferences.getString("truckV_truckImage", "");
                            OkHttpUpload.saveImage(getContext(), truckImageLocation, Integer.toString(wsTrackingId));

                            // CALL APPROACH WEIGH STATION ONCE
                            serviceIntent = new Intent();
                            serviceIntent.setAction(".utils.ApproachingWeighStationAudioSvc");
                            objIntent = new Intent(parentContext, ApproachingWeighStationAudioSvc.class);
                            startService(objIntent);

                            Log.d("SRI","Have wsTrackingID: "+wsTrackingId);
                        } else {
                            // stop approach weigh station audio
                            // manually give time for audio to

                        }

                        Log.d("APPROACH", "END APPROACH");
                    }

                        if (
                            currentWeighStation.length() != 0 // in weigh station
                            &&
                            (
                                gateName.toUpperCase().contains("WIM")
                                ||
                                GPS_WIM
                            )
                        ) {
                        Log.d("WIM", "START WIM");
//                        drawSignal(canvas);
                        GPS_WIM = true;

                        // stop approach weigh station audio
                        objIntent = new Intent(parentContext, ApproachingWeighStationAudioSvc.class);
                        stopService(objIntent);


                            // ENTER WIM
                        if (gateName.toUpperCase().contains("WIM") && GPS_WIM) {
                            if (!isWsWimCalled) {
                                // wsTrackingId set to 0 as a default
                                isWsWimCalled = true;
                                poster.callWimEnter(parentContext, location.getLatitude(), location.getLongitude(), station_id, wsTrackingId);
                            }
                            initialWeighStationEntered(canvas);
                        }


                        // EXIT WIM
                        if (gateName.length() == 0 && GPS_WIM) {

                            if (!isWsExitWimCalled) {
                                isWsExitWimCalled = true;
                                String passFail = poster.callWimLeave(parentContext, location.getLatitude(), location.getLongitude(), station_id, wsTrackingId);
                                if(passFail != null && passFail.equalsIgnoreCase("P")){
                                    isWeightOver = false;

                                    Intent serviceIntent = new Intent();
                                    objIntent = new Intent(parentContext, PassWeightAudioSvc.class);
                                    serviceIntent.setAction(".utils.PassWeightAudio");
                                    startService(objIntent);

                                }else{

                                    Intent serviceIntent = new Intent();
                                    objIntent = new Intent(parentContext, FailWeightAudioSvc.class);
                                    serviceIntent.setAction(".utils.FailWeightAudio");
                                    startService(objIntent);

                                    isWeightOver = true;
                                }
                            }
                            drawSignal(canvas);
                        }

                        Log.d("WIM", "END WIM");
                    }


                    if (
                        currentWeighStation.length() != 0 // in weigh station
                        &&
                        (
                            gateName.toUpperCase().contains("EXIT")
                        )
                    ) {
                        // release audio
//                        stopService(objIntent);

                        isWsApproachCalled = false;
                        isWsWimCalled = false;
                        isWsExitWimCalled = false;
                        wsTrackingId = 0;

                        try {
                            Intent passIntent = new Intent(parentContext, PassWeightAudioSvc.class);
                            stopService(passIntent);
                        } catch (Exception e) {
                            Log.d("PASS INTENT", e.toString());
                        }

                        try {
                            Intent failIntent = new Intent(parentContext, FailWeightAudioSvc.class);
                            stopService(failIntent);
                        } catch (Exception e) {
                            Log.d("PASS INTENT", e.toString());
                        }

                        Intent intent = new Intent(RedGreenSignalActivity.this, TruckSmartParking.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }


                    // KEEP WEIGH STATION IMAGE OPEN UNTIL WIM
                    if (
                            currentWeighStation.length() != 0 // OFALLON
                            &&
                            gateName.length() == 0 // !APPROACH, !WIM, !EXIT
                            &&
                            !GPS_WIM
                        ) {
                            initialWeighStationEntered(canvas);

                    }
                }

            } catch (Exception e) {

                Log.d("RedGreenSignalActivity", e.toString());
                Log.d("RedGreenSignalActivity", e.toString());

            } finally {
                if (canvas != null) {

                    Paint p = new Paint();
                    p.setTextSize(100);
                    p.setColor(Color.WHITE);

//                    canvas.drawText("weigh station " + currentWeighStation, 0, 1000, p);
//                    canvas.drawText("gate name " + gateName, 0, 1100, p);
//                    canvas.drawText("lat" + location.getLatitude(), 0, 1200, p);
//                    canvas.drawText("lon " + location.getLongitude(), 0, 1300, p);

                    surfaceHolder.unlockCanvasAndPost(canvas);

                }
            }
        }
        // END - SURFACEVIEW


        // WEIGH STATION ENTERED
        private void initialWeighStationEntered(Canvas canvas) {

            Paint p = new Paint();
            canvas.drawColor(Color.BLACK);            // SET BACKGROUND
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_approaching_image);

            Double imgWidth = new Double(b.getWidth());
            Double imgHeight = new Double(b.getHeight());
            Double layoutWidth = new Double(this.getWidth());
            Double layoutHeight = new Double(this.getHeight());

            Double scale = (imgWidth - layoutWidth) / imgWidth;
            scale = imgHeight - (scale * imgHeight);

            int midpoint = (int)(layoutHeight / 2);

            b = b.createScaledBitmap(b, this.getWidth(), this.getHeight(), true);
            canvas.drawBitmap(b, 0, 0, p);


        }

        public void drawSignal(Canvas canvas) {

            Paint p = new Paint();
            canvas.drawColor(Color.BLACK);            // SET BACKGROUND

            Bitmap b = null;

            if (!isWeightOver) {
                b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_passed_image);
            } else {
                b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_failed_image);
            }
            Double imgWidth = new Double(b.getWidth());
            Double imgHeight = new Double(b.getHeight());
            Double layoutWidth = new Double(this.getWidth());
            Double layoutHeight = new Double(this.getHeight());

            Integer scaleW = 0;
            Integer scaleH = 0;

            b = b.createScaledBitmap(b, this.getWidth(), this.getHeight(), true);

            canvas.drawBitmap(b, 0, 0, p);

        }


        public void drawText(Canvas canvas, Paint paint, String text, int y) {

                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
//        int y = (canvas.getHeight() / 2) - (bounds.height() / 2);
                canvas.drawText(text, x, y, paint);
        }

    }


    class GPSServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            gps = ((GPSTracker.GPSTrackerBinder) service).getTheService();
//            gpsTracker.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10* 1000, 0, RedGreenSignalActivity.this);
            gps.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, RedGreenSignalActivity.this);
            Toast.makeText(RedGreenSignalActivity.this, "GPS Service Bound", Toast.LENGTH_LONG);
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (drawingView != null) {
            this.location = location;
            drawingView.determineGeoFence(location);
        }
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

}
