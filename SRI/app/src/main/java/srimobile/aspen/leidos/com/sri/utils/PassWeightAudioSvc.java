package srimobile.aspen.leidos.com.sri.utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

import srimobile.aspen.leidos.com.sri.R;


public class PassWeightAudioSvc extends Service {
    private static final String LOGCAT = null;
    MediaPlayer objPlayer;

    public void onCreate() {
        super.onCreate();
        Log.d(LOGCAT, "Service Started!");
        objPlayer = MediaPlayer.create(this, R.raw.pass_weight);

        try {
            Class audioSystemClass = Class.forName("android.media.AudioSystem");
            Method setForceUse = audioSystemClass.getMethod("setForceUse", int.class, int.class);
            // First 1 == FOR_MEDIA, second 1 == FORCE_SPEAKER. To go back to the default
            // behavior, use FORCE_NONE (0).
            setForceUse.invoke(null, 1, 1);

        } catch (Exception e) {
            Log.d("AUDIO CREATE", "ERROR " + e.toString());
        }

    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        objPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    objPlayer.stop();
                } catch (Exception e) {
                    Log.e("APPR AUDIO", e.toString());
                }

                try {
                    objPlayer.stop();
                } catch (Exception e) {
                    Log.e("APPR AUDIO", e.toString());
                }

                try {
                    Class audioSystemClass = Class.forName("android.media.AudioSystem");
                    Method setForceUse = audioSystemClass.getMethod("setForceUse", int.class, int.class);
                    // First 1 == FOR_MEDIA, second 1 == FORCE_SPEAKER. To go back to the default
                    // behavior, use FORCE_NONE (0).
                    setForceUse.invoke(null, 0, 0);

                } catch (Exception e) {
                    Log.d("AUDIO CREATE", "ERROR " + e.toString());
                }

                stopSelf();
            }
        });

        objPlayer.start();

        Log.d(LOGCAT, "Media Player started!");
        if (objPlayer.isLooping() != true) {
            Log.d(LOGCAT, "Problem in Playing Audio");
        }

        return 1;
    }

    @Override
    public void onDestroy() {

        try {
            objPlayer.stop();
        } catch (Exception e) {
            Log.e("APPR AUDIO", e.toString());
        }

        try {
            objPlayer.stop();
        } catch (Exception e) {
            Log.e("APPR AUDIO", e.toString());
        }

        try {
            Class audioSystemClass = Class.forName("android.media.AudioSystem");
            Method setForceUse = audioSystemClass.getMethod("setForceUse", int.class, int.class);
            // First 1 == FOR_MEDIA, second 1 == FORCE_SPEAKER. To go back to the default
            // behavior, use FORCE_NONE (0).
            setForceUse.invoke(null, 0, 0);

        } catch (Exception e) {
            Log.d("AUDIO CREATE", "ERROR " + e.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}