package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.prefs.Preferences;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.utils.TurnOnLocationServicesGPS;

/**
 * Created by walswortht on 5/5/2015.
 */
public class ProfileActivity extends Activity {

    Button profileLayout_logout_btn;
    ImageButton profileLayout_editprofile_imgBtn;
    ImageButton profileLayout_weighstationnotification_imgBtn;
    ImageView profileSriLogo;
    public static String versionName = "";
    public static String versionCode = "";

    public final static int REQUEST_CODE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        profileSriLogo                                  = (ImageView)findViewById(R.id.profileSriLogo);
        profileLayout_logout_btn                        = (Button)findViewById(R.id.profileLayout_logout_btn);
        profileLayout_editprofile_imgBtn                = (ImageButton)findViewById(R.id.profileLayout_editprofile_imgBtn);
        profileLayout_weighstationnotification_imgBtn   = (ImageButton)findViewById(R.id.profileLayout_weighstationnotification_imgBtn);

        profileLayout_logout_btn.setOnClickListener(logoutHandler);
        profileLayout_editprofile_imgBtn.setOnClickListener(editProfileHandler);
        profileLayout_weighstationnotification_imgBtn.setOnClickListener(weighStationNotificationHandler);
        profileSriLogo.setOnClickListener(sriVersionHandler);


        // DISABLE SCREEN LOCK
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            ProfileActivity.versionName = pInfo.versionName;

            ProfileActivity.versionCode = String.valueOf(pInfo.versionCode);

        } catch (Exception e) {
            Log.d("SRI", e.toString());
        }

    }


    // logout
    View.OnClickListener sriVersionHandler= new View.OnClickListener() {
        public void onClick(View v) {

           try {

                Toast.makeText(getApplicationContext(), "SRI Version Name: " + ProfileActivity.versionName + "\\n SRI Version Code:  " + ProfileActivity.versionCode, Toast.LENGTH_LONG ).show();

            } catch (Exception e) {
                Log.d("SRI", e.toString());
            }
        }
    };

    // logout
    View.OnClickListener logoutHandler= new View.OnClickListener() {
        public void onClick(View v) {

            Intent intent = new Intent(ProfileActivity.this, LoginNewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            finish();
        }
    };

    // enter truck information
    View.OnClickListener editProfileHandler = new View.OnClickListener() {
        public void onClick(View v) {

            Intent intent = new Intent(ProfileActivity.this, TruckActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            finish();

        }
    };


    View.OnClickListener weighStationNotificationHandler = new View.OnClickListener() {
        public void onClick(View v) {

            SharedPreferences prefs = getSharedPreferences("SRI", Context.MODE_PRIVATE);


            String cdl = prefs.getString("cdl", "");
                            String dl = prefs.getString("dl", "");

            String vin = prefs.getString("vin", "");
            String usdot = prefs.getString("usdot","");
            String lp = prefs.getString("lp", "");

            if (
                            (vin.toString().trim().length() == 0) ||
                            (usdot.toString().trim().length() == 0) ||
                            (lp.toString().trim().length() == 0)
                    )
                {

                Toast toast = Toast.makeText(ProfileActivity.this, "NOT ALL VALUES HAVE BEEN SAVED IN EDIT PROFILE", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0 ,0);
                toast.show();
            } else {
                Intent intent = new Intent(ProfileActivity.this, TruckSmartParking.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

//            finish();

        }
    };

}
