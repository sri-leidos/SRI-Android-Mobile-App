package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieStore;
import java.util.ArrayList;
import java.util.List;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.gps.HTTPClients;
import srimobile.aspen.leidos.com.sri.gps.OkHttpDefaultClient;
import srimobile.aspen.leidos.com.sri.web.WebServicePoster;


/**
 * Created by walswortht on 5/6/2015.
 */
public class LoginNewActivity extends Activity {

    EditText loginLayout_username_edtTxt;
    EditText loginLayout_password_edtTxt;
    Button loginLayout_signin_btn;
    Button loginLayout_forgotpassword_btn;
    Button loginLayout_signup_btn;

    public static final int REQUEST_CODE = 1111111;

    private String loginSuccessful;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_new_layout);

        loginLayout_username_edtTxt = (EditText) findViewById(R.id.loginLayout_username_edtTxt);
        loginLayout_password_edtTxt = (EditText) findViewById(R.id.loginLayout_password_edtTxt);
        loginLayout_signin_btn = (Button) findViewById(R.id.loginLayout_signin_btn);
        loginLayout_forgotpassword_btn = (Button) findViewById(R.id.loginLayout_forgotpassword_btn);
        loginLayout_signup_btn = (Button) findViewById(R.id.loginLayout_signup_btn);

        loginLayout_signin_btn.setOnClickListener(signinHandler);
        loginLayout_forgotpassword_btn.setOnClickListener(forgotpasswordHandler);
        loginLayout_signup_btn.setOnClickListener(signupHandler);

        // DISABLE SCREEN LOCK
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    View.OnClickListener signinHandler = new View.OnClickListener() {
        public void onClick(View v) {

            SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();

            edit.putString("loginLayout_username_edtTxt", ((EditText) findViewById(R.id.loginLayout_username_edtTxt)).getText().toString());

            edit.commit();

            HTTPClients.session_id = "";

            LoginSri asyncLogin = new LoginSri();
            asyncLogin.execute(WebServicePoster.LOGIN_URL);

        }
    };


    View.OnClickListener forgotpasswordHandler = new View.OnClickListener() {
        public void onClick(View v) {

            // DIALOG PASSWORD RESET EMAIL SENT
        }
    };

    View.OnClickListener signupHandler = new View.OnClickListener() {
        public void onClick(View v) {

            Intent explicitIntent = new Intent(LoginNewActivity.this, LoginRegisterActivity.class);
            startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);
        }
    };


    public void showLoginDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(LoginNewActivity.this);

        b.setTitle("LOGIN UNSUCCESSFUL")
                .setMessage("LOGIN UNSUCCESSFUL")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Log.d("LOGIN", "LOGIN BUTTON" + which);
                            }
                        }
                );
        b.create().show();

//        AlertDialog alertDialog = b.create();
//        alertDialog.show();

    }


    private class LoginSri extends AsyncTask {

        Integer responseCode = -1;

        protected Object doInBackground(Object[] params) {

            Log.e("download", params[0].toString());

            try {
                for (Object url : params) {

                    OkHttpDefaultClient client = (OkHttpDefaultClient)OkHttpDefaultClient.getOkHttpClient();
//                    HttpClient client = HTTPClients.getDefaultHttpClient();

                    RequestBody feb = new FormEncodingBuilder()
                        .add("j_password", loginLayout_password_edtTxt.getText().toString().trim())
                        .add("j_username", loginLayout_username_edtTxt.getText().toString().trim())
                        .build();

                    Request request = new Request.Builder()
                        .url(WebServicePoster.APPROACH_URL)
                        .post(feb)
                        .build();

                    client.setFollowRedirects(true);
                    Response response = client.newCall(request).execute();

//                    HttpPost requestLogin = new HttpPost(url.toString());
//                    HttpResponse response = null;
//
//                    List<NameValuePair> params2 = new ArrayList<NameValuePair>();
//                    params2.add(new BasicNameValuePair("j_username", loginLayout_username_edtTxt.getText().toString().trim()));
//                    params2.add(new BasicNameValuePair("j_password", loginLayout_password_edtTxt.getText().toString().trim()));
//
//                    requestLogin.setEntity(new UrlEncodedFormEntity(params2, HTTP.UTF_8));
//                    response = client.execute(requestLogin);
//
//                    InputStream is = (InputStream)response.getEntity().getContent();

//                    StringWriter writer = new StringWriter();
//                    IOUtils.copy(is, writer, "UTF-8");
//                    System.out.println("----");
//                    String theString = writer.toString();
//                    System.out.println("----" + theString);

//j                    if (HTTPClients.session_id.length() == 0) { // logged in
//                        showLoginDialog();
//                    }

                    System.out.println("session_id" + HTTPClients.session_id );
                    System.out.println("session_id" + HTTPClients.session_id );
                    System.out.println("session_id" + HTTPClients.session_id );

                    return response;

//                    if (!response.isSuccessful()) {
//
//                        responseCode = -1;
//                        throw new IOException("Unexpected code " + response);
//                    } else {
//                        System.out.println(response.code());
//                        System.out.println("");
//                        System.out.println(response.body().string());
//                        System.out.println("");
//
//                        responseCode = response.code();
//
//                    }

            }
        } catch (Exception ex) {
                ex.printStackTrace();
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Object result) {
            try {
                BasicHttpResponse response = (BasicHttpResponse)result;

                InputStream is = (InputStream)response.getEntity().getContent();

                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer, "UTF-8");
                System.out.println("----");
                String theString = writer.toString();
                System.out.println("----" + theString);

                System.out.println(theString);


            } catch (Exception e) {
                System.out.println("result ");
            }
            Log.e("postExe", "post execute.." + result);
            Log.e("postExe", "post execute.." + result);

            Intent explicitIntent = new Intent(LoginNewActivity.this, TruckActivity.class);
            startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);
//            if (HTTPClients.session_id != null && HTTPClients.session_id.length() > 0) {
//
//                Intent explicitIntent = new Intent(LoginNewActivity.this, TruckActivity.class);
//                startActivityForResult(explicitIntent, LoginNewActivity.REQUEST_CODE);
//            } else {
//                showLoginDialog();
//            }

        }
    }

        }


