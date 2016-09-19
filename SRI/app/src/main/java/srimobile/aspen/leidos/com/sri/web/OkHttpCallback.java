package srimobile.aspen.leidos.com.sri.web;

import android.view.SurfaceHolder;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import srimobile.aspen.leidos.com.sri.data.TruckFeed;

/**
 * Created by walswortht on 7/31/2015.
 */
public class OkHttpCallback  implements Callback
{

    private int retId = 0;

    public int getRetId() {
        return retId;
    }
    @Override
    public void onFailure(Request request, IOException e) {

        System.out.println("OKHTTP" + " UPLOAD RESPONSE FAILURE");
    }

    @Override
    public void onResponse(Response response) throws IOException {

        if (!response.isSuccessful()) {

            System.out.println("OKHTTP" + " UPLOAD RESPONSE FAILURE" + response);

            System.out.println(response.toString());

        } else {

            Gson gson = new Gson();

            TruckFeed tf = gson.fromJson(response.body().charStream(), TruckFeed.class);

            System.out.println(tf.getId());

            retId = tf.getId();
        }
    }
}
