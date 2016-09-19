package srimobile.aspen.leidos.com.sri.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

/**
 * Created by walswortht on 9/14/2015.
 */
public class TurnOnLocationServicesGPS {
    Context context = null;

    public TurnOnLocationServicesGPS(Activity context) {
        this.context = context;
    }

    public void turnOnGpsMode() {

        GPSManager gps = new GPSManager(((Activity)context));
        gps.start();

    }


    public void turnOffAirplaneMode() {

        final boolean isEnabled = Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;

        if (isEnabled) {
            new AlertDialog.Builder(context)
                    .setTitle("AIRPLANE MODE IS ON")
                    .setMessage("SET AIRPLANE MODE TO OFF IN 'SETTINGS'")
                    .setPositiveButton("TURN OFF AIRPLANE MODE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent i = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}
