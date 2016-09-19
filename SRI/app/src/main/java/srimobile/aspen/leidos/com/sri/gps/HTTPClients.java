package srimobile.aspen.leidos.com.sri.gps;


import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class HTTPClients implements Interceptor {

    public static String session_id;
    private static DefaultHttpClient _defaultClient;
    private static HTTPClients _me;

    private HTTPClients() {

    }

    public static DefaultHttpClient getDefaultHttpClient() {
        if (_defaultClient == null) {
            _defaultClient = new DefaultHttpClient();
            _me = new HTTPClients();
            _defaultClient.addResponseInterceptor(_me.new SessionKeeper());
            _defaultClient.addRequestInterceptor(_me.new SessionAdder());
        }
        return _defaultClient;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        System.out.println(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        System.out.println(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }

    private class SessionAdder implements HttpRequestInterceptor {

        @Override
        public void process(HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            if (session_id != null) {
                request.setHeader("Cookie", session_id);
            }
        }

    }

    private class SessionKeeper implements HttpResponseInterceptor {

        @Override
        public void process(HttpResponse response, HttpContext context)
                throws HttpException, IOException {
            Header[] headers = response.getHeaders("Set-Cookie");
            if (headers != null && headers.length == 1) {
                session_id = headers[0].getValue();
            }
        }

    }
}