package srimobile.aspen.leidos.com.sri.gps;

import android.util.Log;

import com.squareup.okhttp.Connection;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.Header;

import java.io.IOException;
import java.util.HashSet;
import java.util.prefs.Preferences;

/**
 * Created by walswortht on 8/21/2015.
 */
public class OkHttpDefaultClient extends OkHttpClient implements Interceptor {
    private static OkHttpClient okHttpClient;

    private OkHttpDefaultClient() {

        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
            okHttpClient.interceptors().add(this);
        }
    }

    public static OkHttpClient getOkHttpClient() {

        OkHttpDefaultClient.okHttpClient = new OkHttpDefaultClient();

        return okHttpClient;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        request = request.newBuilder()
                .addHeader("AUTHORIZATION", "Test")
                .build();

        System.out.println("headers" + request.headers().toString() );
        System.out.println("headers" + request.headers().toString() );
        System.out.println("headers" + request.headers().toString() );
        System.out.println("headers" + request.headers().toString() );

        Response response = chain.proceed(request);

        for (int x = 0; x < request.headers().size(); x++ ) {
            request.headers().newBuilder().add("ses", "ses");
        }

        for (int x = 0; x < request.headers().size(); x++ ) {
            System.out.println("header " + request.headers().name(x) + "  " + request.headers().name(x));
        }

        return response;
    }

}
