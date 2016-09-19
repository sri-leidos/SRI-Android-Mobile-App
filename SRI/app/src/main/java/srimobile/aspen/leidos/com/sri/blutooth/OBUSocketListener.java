package srimobile.aspen.leidos.com.sri.blutooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by cassadyja on 4/9/2015.
 */
public class OBUSocketListener implements Runnable {
    private static final String TAG = "SRI";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handler;

    public OBUSocketListener(BluetoothSocket socket, Handler handler) {
        this.mmSocket = socket;
        this.handler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("SRI", e.toString());
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        //listen for message with Read
        //once bytes have been read send message to handler
        //return to reading
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                handler.obtainMessage(OBUBluetoothService.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                handler.obtainMessage(OBUBluetoothService.MESSAGE_CONNECTION_LOST, 0, -1, buffer)
                        .sendToTarget();
                break;
            }
        }

    }


    public void writeData(byte[] data) {
        try {
            mmOutStream.write(data);
        } catch (IOException e) {
            Log.e("SRI", e.toString());
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.d("SRI", e.toString());
        }
    }


}
