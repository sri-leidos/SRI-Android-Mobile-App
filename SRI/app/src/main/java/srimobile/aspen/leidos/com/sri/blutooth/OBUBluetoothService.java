package srimobile.aspen.leidos.com.sri.blutooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.bn.CoderFactory;
import org.bn.IEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import srimobile.aspen.leidos.com.sri.data.DriverVehicleInformationData;
import srimobile.aspen.leidos.com.sri.gps.CoordinateChecker;
import srimobile.aspen.leidos.com.sri.gps.GPSTracker;


/**
 * Created by cassadyja on 4/9/2015.
 */
public class OBUBluetoothService extends Service implements BlueToothServiceInterface, LocationListener {

    private static final UUID MY_UUID = UUID.fromString("66841278-c3d1-11df-ab31-001de000a901");
    private static final String NAME = "AndroidLocomateMessaging";
    private static final String TAG = "SRI";

    private BluetoothAdapter mAdapter;



    private int mState;
    private AcceptThread acceptThread;
    private OBUSocketListener socketListener;
    private BluetoothBinder myBinder;
    private BluetoothDevice device;

    private boolean requestPending = false;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECTION_LOST = 5;

    private GPSTracker gps = null;
    private boolean mBound;
    private boolean joined = false;
    private String lastRequestId = null;
    private CoordinateChecker checker = null;


    public static final int NO_MESSAGE_SENT = 0;
    public static final int INITIAL_DATA_SENT = 1;
    public static final int ID_RECEIVED = 2;
    public static final int WIM_MESSAGE_SENT = 3;
    public static final int BYPASS_NOTIFICATION_RECEIVED = 4;

    private int currentMessageState = 0;


    private ServiceConnection mConnection;

    private class GPSServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            gps = ((GPSTracker.GPSTrackerBinder)service).getTheService();
            gps.getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, OBUBluetoothService.this);
            gps.getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, OBUBluetoothService.this);

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    private final Handler socketListenerHandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
//                            mTitle.setText(R.string.title_connected_to);
//                            if(mConnectedDeviceName != null)
//                                mTitle.append(mConnectedDeviceName);
//                            else
//                                mTitle.append("..");
//                            mConversationArrayAdapter.clear();
                            break;
                        case STATE_CONNECTING:
//                            mTitle.setText(R.string.title_connecting);
                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
//                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
//                case MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if("Joined with RSU".equalsIgnoreCase(readMessage)){
                        joined = true;
                        performJoinedAction();
                    }else if("Out of the range of RSU".equalsIgnoreCase(readMessage)){
                        joined = false;
                        //Left RSU

                    }else{
                        // have a server response
                        processServerResponse(readMessage);
                    }
                    break;
                case MESSAGE_CONNECTION_LOST:
                    startAcceptThread();
                    break;
            }

        }
    };


    public class BluetoothBinder extends Binder{
        private BlueToothServiceInterface service;

        public BlueToothServiceInterface getService() {
            return service;
        }

        public void setService(BlueToothServiceInterface service) {
            this.service = service;
        }
    }



    protected void performJoinedAction(){
        if(!requestPending){
            sendMessage();
        }else{
            checkStatus();
        }
    }

    protected void sendMessage(){
        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckV_driversLicense      = preferences.getString("truckV_driversLicenseEdtFld",      "");
        String truckV_vin                 = preferences.getString("truckV_vinEdtFld",                 "");
        String truckV_usdot               = preferences.getString("truckV_usdotEdtFld",               "");
        String truckV_truck               = preferences.getString("truckV_truckEdtFld",               "");

        DriverVehicleInformationData data = new DriverVehicleInformationData();
        if(true) {
            data.setCdlNumber(truckV_driversLicense);
        }else {
            data.setDriversLicenseNumber(truckV_driversLicense);
        }
        data.setPlateNumber(truckV_truck);
        data.setUsdotNumber(truckV_usdot);
        data.setVin(truckV_vin);
        byte[] encodedMessage = encodeData(data);
        if(encodedMessage != null) {
            socketListener.writeData(encodedMessage);
            requestPending = true;
            currentMessageState = INITIAL_DATA_SENT;
        }else{
            Log.e(TAG, "Encountered an Error encoding message to server");
        }

    }

    private byte[] encodeData(DriverVehicleInformationData data){
        try {
            IEncoder<DriverVehicleInformationData> encoder = CoderFactory.getInstance().newEncoder("BER");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            encoder.encode(data, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     *  Called when we have already made a request to SRI
     *  but have not received a Red or Green
     *  and just joined with a new RSU
     */
    protected void checkStatus(){



    }

    protected void processServerResponse(String readMessage){
        switch(currentMessageState){
            case INITIAL_DATA_SENT:{
                //message received should contain our ID;

                break;
            }
            case WIM_MESSAGE_SENT:{
                //message received should contain our red or green
                break;
            }
        }
        //parse the message
        //determine if it was red or green
        //send red or green back to the activity
    }



    @Override
    public void setBluetoothDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public void connectBluetooth() {

    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        this.device = device;
        this.socketListener = new OBUSocketListener(socket, socketListenerHandler);
        new Thread(socketListener).start();
        setState(STATE_CONNECTED);

    }

    public void onCreate(){
        checker = new CoordinateChecker();

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        startAcceptThread();
        if(mConnection == null){
            mConnection = new GPSServiceConnection();
        }
        Intent intent = new Intent(this, GPSTracker.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onDestroy(){
        acceptThread.cancel();
        socketListener.cancel();
        setState(STATE_NONE);
        if(mBound){
            unbindService(mConnection);
        }
    }


    protected void startAcceptThread() {
        if(acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
        }
        acceptThread = new AcceptThread();
        acceptThread.start();
    }




    @Override
    public IBinder onBind(Intent intent) {
        if(myBinder == null){
            myBinder = new BluetoothBinder();
            myBinder.setService(this);
        }



        return myBinder;
    }




    private synchronized void setState(int state) {
        this.mState = state;
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            		/* For android versions less than gingerbread(2.3.3) */
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                } else {
            		/* For android versions gingerbread(2.3.3) and above */
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }



        public void run(){
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (OBUBluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        if(requestPending && joined){

        }else{
            //Not in RSU range.
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
