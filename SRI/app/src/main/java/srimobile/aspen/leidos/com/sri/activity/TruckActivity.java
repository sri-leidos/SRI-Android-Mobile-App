package srimobile.aspen.leidos.com.sri.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.activity.dialog.TruckImageDialogActivity;
import srimobile.aspen.leidos.com.sri.utility.CustomImageView;
import srimobile.aspen.leidos.com.sri.utils.ResizeImage;
import srimobile.aspen.leidos.com.sri.utils.TurnOnLocationServicesGPS;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;


/**
 * Created by walswortht on 3/3/2015.
 */
public class TruckActivity extends Activity implements DialogInterface.OnDismissListener {
    CustomImageView truckLay_takeTruckImage_imgBtn;

    public Switch truckLay_CDL_tglBtn;

    public EditText truckLay_CDL_DL_edtTxt;

    public EditText truckLay_VIN_edtTxt;
    public EditText truckLay_USDOT_edtTxt;
    public EditText truckLay_LP_edtTxt;

    Button truckLay_saveTruckInfo_btn;
    Button truckLay_cancelTruckInfo_btn;
    Button truckLay_launchBT_btn;
    Button truckLay_redGreen_btn;

    Button profileBackBtn;

    String CDL_temp = "";
    String DL_temp = "";

    public static int height = 400;
    public static int width = 600;

    public static final String MY_PREFS_NAME = "SRI";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.truck_layout);

        Intent intent = getIntent();
        profileBackBtn = (Button) findViewById(R.id.profileBackBtn);

        truckLay_takeTruckImage_imgBtn = (CustomImageView) findViewById(R.id.truckLay_takeTruckImage_imgBtn);

        truckLay_CDL_tglBtn = (Switch) findViewById(R.id.truckLay_CDL_tglBtn);

        truckLay_CDL_DL_edtTxt = (EditText) findViewById(R.id.truckLay_CDL_DL_edtTxt);

        truckLay_cancelTruckInfo_btn = (Button)findViewById(R.id.truckLay_cancelTruckInfo_btn);

//        truckLay_DL_edtTxt = (EditText) findViewById(R.id.truckLay_DL_edtTxt);

        truckLay_VIN_edtTxt = (EditText) findViewById(R.id.truckLay_VIN_edtTxt);
        truckLay_USDOT_edtTxt = (EditText) findViewById(R.id.truckLay_USDOT_edtTxt);
        truckLay_LP_edtTxt = (EditText) findViewById(R.id.truckLay_LP_edtTxt);

        truckLay_saveTruckInfo_btn = (Button) findViewById(R.id.truckLay_saveTruckInfo_btn);

//        truckLay_launchBT_btn = (Button) findViewById(R.id.truckLay_launchBT_btn);
//        truckLay_redGreen_btn = (Button) findViewById(R.id.truckLay_redGreen_btn);

        truckLay_CDL_DL_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_VIN_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_USDOT_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        truckLay_LP_edtTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        if (truckLay_CDL_tglBtn.isChecked()) {
            truckLay_CDL_tglBtn.setText("CDL");
            truckLay_CDL_DL_edtTxt.setHint("COMMERCIAL DRIVERS LICENSE");
        } else {
            truckLay_CDL_tglBtn.setText("DL");
            truckLay_CDL_DL_edtTxt.setHint("DRIVERS LICENSE");
        }

        truckLay_CDL_DL_edtTxt.setOnKeyListener(new View.OnKeyListener() {
                                                    @Override
                                                    public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (truckLay_CDL_tglBtn.isChecked() ) {
                    CDL_temp = truckLay_CDL_DL_edtTxt.getText().toString();
                    truckLay_CDL_DL_edtTxt.setHint("COMMERCIAL DRIVERS LICENSE");
                } else {
                    DL_temp = truckLay_CDL_DL_edtTxt.getText().toString();
                    truckLay_CDL_DL_edtTxt.setHint("DRIVERS LICENSE");
                }

                return false;
                }
            }
        );

        truckLay_CDL_tglBtn.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            truckLay_CDL_tglBtn.setText("CDL");
                            truckLay_CDL_DL_edtTxt.setText(CDL_temp);
                            truckLay_CDL_DL_edtTxt.setHint("COMMERCIAL DRIVERS LICENSE");
                        } else {
                            truckLay_CDL_tglBtn.setText("DL");

                            truckLay_CDL_DL_edtTxt.setText(DL_temp);
                            truckLay_CDL_DL_edtTxt.setHint("DRIVERS LICENSE");
                        }

                    }
                }
        );

        truckLay_takeTruckImage_imgBtn.setOnClickListener(takePhotoFromCameraHandler);

        truckLay_saveTruckInfo_btn.setOnClickListener(saveTruckInfoHandler);
        truckLay_cancelTruckInfo_btn.setOnClickListener(cancelButtonRedirect);
        profileBackBtn.setOnClickListener(cancelButtonRedirect);


//        truckLay_launchBT_btn.setOnClickListener(bTLaunchClickHandler);
//        truckLay_redGreen_btn.setOnClickListener(redGreenBtnHandler);

        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckImageLocation = preferences.getString("truckV_truckImage", "");

        // populate truck edittext form fields
        getCredPrefs();

        Log.d("TRUCKACTIVITY IMG PATH", truckImageLocation);
        Log.d("TRUCKACTIVITY IMG PATH", getApplicationContext().getPackageName());

        // if "truckV_truckImage" has not been set value of truckImageLocation would be null or empty ""
        Bitmap truckImageBitmap = null;
        try {
            if (truckImageLocation == null || truckImageLocation.trim().length() == 0) {

                truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
                truckLay_takeTruckImage_imgBtn.setImageBitmap(truckImageBitmap);

                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("truckV_truckImage", "");
                edit.commit();

            }
            else {


                // Now change ImageView's dimensions to match the scaled image
//                truckImageBitmap = BitmapFactory.decodeFile(truckImageLocation);
                truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation, TruckActivity.width, TruckActivity.height);

                if (truckImageBitmap == null) { // photo was deleted
                    truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);

//                    SharedPreferences.Editor edit = preferences.edit();
//                    edit.putString("truckV_truckImage", "");
//                    edit.commit();

                    Intent i = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }

            ExifInterface exif = new ExifInterface(truckImageLocation);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

            Bitmap resized = getScaledBitmap(truckImageBitmap, TruckActivity.width, TruckActivity.height);

            truckLay_takeTruckImage_imgBtn.setImageBitmap(resized);
            truckLay_takeTruckImage_imgBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } catch (Exception e) {
            Log.d("EXIF", e.toString());

        }

        // disable screen lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    View.OnClickListener cancelButtonRedirect = new View.OnClickListener() {
        public void onClick(View v) {

            String cdl_text = truckLay_CDL_DL_edtTxt.getText().toString();
            String vin_text = truckLay_VIN_edtTxt.getText().toString();
            String usdot_text = truckLay_USDOT_edtTxt.getText().toString();
            String lp_text = truckLay_LP_edtTxt.getText().toString();

            SharedPreferences prefs = getSharedPreferences("SRI", Context.MODE_PRIVATE);

            String cdl = prefs.getString("cdl", "");
            String dl = prefs.getString("dl", "");
            String vin = prefs.getString("vin", "");
            String usdot = prefs.getString("usdot", "");
            String lp = prefs.getString("lp", "");

            String message = "";
            boolean fieldValueChanged = false;
            boolean fieldValueEmpty = false;

            if (
                    (
                        (!vin.toString().trim().equalsIgnoreCase(vin_text)) || (vin_text.trim().length() == 0)) ||
                        (!usdot.toString().trim().equalsIgnoreCase(usdot_text)) || (usdot_text.trim().length() == 0) ||
                        (!lp.toString().trim().equalsIgnoreCase(lp_text) || (lp_text.trim().length() == 0)
                    )
                ) {

                if (vin_text.trim().length() == 0) {
                    message += "VIN VALUE IS EMPTY.\n";
                    fieldValueEmpty = true;
                } else if (!vin.toString().trim().equalsIgnoreCase(vin_text.trim())) {
                    message += "VIN VALUE NOT SAVED.\n";
                    fieldValueChanged = true;
                }

                if (usdot_text.trim().length() == 0) {
                    message += "USDOT VALUE IS EMPTY.\n";
                    fieldValueEmpty = true;
                } else if (!usdot.toString().trim().equalsIgnoreCase(usdot_text.trim())) {
                    message += "USDOT VALUE NOT SAVED.\n";
                    fieldValueChanged = true;
                }


                if (lp_text.trim().length() == 0) {
                    message += "LICENSE PLATE VALUE IS EMPTY.\n";
                    fieldValueEmpty = true;
                } else if (!lp.toString().trim().equalsIgnoreCase(lp_text.trim())) {
                    message += "LICENSE PLATE VALUE NOT SAVED.\n";
                    fieldValueChanged = true;
                }

                if (fieldValueEmpty ) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TruckActivity.this);

                    // set title
                    alertDialogBuilder.setTitle("ENTER/UPDATE A VALUE");

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("ENTER VALUES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close current activity
                                    //                                TruckActivity.this.finish();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else if (fieldValueChanged) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TruckActivity.this);

                    // set title
                    alertDialogBuilder.setTitle("UPDATE A VALUE");

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("SAVE UPDATED VALUES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    // save updated values
                                    truckLay_saveTruckInfo_btn.performClick();
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    alertDialog.show();

                }
            } else {
                Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    };


    private Bitmap resizeImage(Bitmap originalImage) {

        int heightResize = TruckActivity.width;
        int widthResize = TruckActivity.height;
        Bitmap background = Bitmap.createBitmap((int)widthResize, (int)heightResize, Bitmap.Config.ARGB_8888);
        float originalWidth = originalImage.getWidth(), originalHeight = originalImage.getHeight();
        Canvas canvas = new Canvas(background);
        float scale = widthResize/originalWidth;
        float xTranslation = 0.0f, yTranslation = (heightResize - originalHeight * scale)/2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalImage, transformation, paint);

        return background;
    }

    private void getCredPrefs() {
        SharedPreferences prefs = getSharedPreferences("SRI", Context.MODE_PRIVATE);

        CDL_temp = prefs.getString("cdl", "");
        DL_temp = prefs.getString("dl", "");

        if (CDL_temp.length() > 0 || DL_temp.length() == 0 ) {
            truckLay_CDL_tglBtn.setChecked(true);

            truckLay_CDL_DL_edtTxt.setText(CDL_temp);
            truckLay_CDL_DL_edtTxt.setHint("COMMERCIAL DRIVERS LICENSE");

        } else {

            truckLay_CDL_tglBtn.setChecked(false);

            truckLay_CDL_DL_edtTxt.setText(DL_temp);
            truckLay_CDL_DL_edtTxt.setHint("DRIVERS LICENSE");
        }

        truckLay_VIN_edtTxt.setText(prefs.getString("vin", ""));
        truckLay_USDOT_edtTxt.setText(prefs.getString("usdot", ""));
        truckLay_LP_edtTxt.setText(prefs.getString("lp", ""));
    }

    private void setCredPrefs() {

    }


    View.OnClickListener takePhotoFromCameraHandler = new View.OnClickListener() {

        public void onClick(View v) {
            try {
//                FragmentManager fm = getFragmentManager();
//                DialogFragment dialogFragment = new TruckImageDialogActivity();
//                dialogFragment.show(fm, "SELECT TRUCK IMAGE");

                Intent intent = new Intent(TruckActivity.this, TruckImageDialogActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (Exception e) {
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
            }
        }
    };

    View.OnClickListener saveTruckInfoHandler = new View.OnClickListener() {

        public void onClick(View view) {


            SharedPreferences.Editor edit = getSharedPreferences("SRI", Context.MODE_PRIVATE).edit();

            if (truckLay_CDL_tglBtn.isChecked()) {
                edit.putString("cdl", truckLay_CDL_DL_edtTxt.getText().toString().trim());
                edit.putString("dl", "");
            } else{
                edit.putString("cdl", "");
                edit.putString("dl", truckLay_CDL_DL_edtTxt.getText().toString());
            }

            edit.putString("vin", truckLay_VIN_edtTxt.getText().toString());
            edit.putString("usdot", truckLay_USDOT_edtTxt.getText().toString());
            edit.putString("lp", truckLay_LP_edtTxt.getText().toString());

            edit.commit();

            //

            SharedPreferences prefs = getSharedPreferences("SRI", Context.MODE_PRIVATE);

            String cdl = prefs.getString("cdl", "");
            String dl = prefs.getString("dl", "");

            String vin = prefs.getString("vin", "");
            String usdot = prefs.getString("usdot","");
            String lp = prefs.getString("lp", "");

            String message = "";

            if (
                    (
                        (vin.toString().trim().length() == 0) ||
                        (usdot.toString().trim().length() == 0) ||
                        (lp.toString().trim().length() == 0)
                    )
                ) {

                if (vin.toString().trim().length() == 0) {
                    message += "ENTER A VIN VALUE.\n";
                }

                if (usdot.toString().trim().length() == 0) {
                    message += "ENTER A USDOT VALUE.\n";
                }

                if (lp.toString().trim().length() == 0) {
                    message += "ENTER A LICENSE PLATE VALUE.\n";
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TruckActivity.this);

                // set title
                alertDialogBuilder.setTitle("ENTER A VALUE FOR ALL FIELDS");

                // set dialog message
                alertDialogBuilder
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close current activity
//                                TruckActivity.this.finish();
                            }
                        });
//                        .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,int id) {
//                                Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
//                                startActivity(intent);
//                            }
//                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
            else {
                Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
                startActivity(intent);
            }


        }
    };

    @Override
    public void onBackPressed() {

        SharedPreferences prefs = getSharedPreferences("SRI", Context.MODE_PRIVATE);

        String cdl = prefs.getString("cdl", "");
        String dl = prefs.getString("dl", "");

        String vin = prefs.getString("vin", "");
        String usdot = prefs.getString("usdot", "");
        String lp = prefs.getString("lp", "");

        String message = "";

        if (
                (
                    (vin.toString().trim().length() == 0) ||
                    (usdot.toString().trim().length() == 0) ||
                    (lp.toString().trim().length() == 0)
                )
            ) {

            if (vin.toString().trim().length() == 0) {
                message += "ENTER A VIN VALUE.\n";
            }

            if (usdot.toString().trim().length() == 0) {
                message += "ENTER A USDOT VALUE.\n";
            }

            if (lp.toString().trim().length() == 0) {
                message += "ENTER A LICENSE PLATE VALUE.\n";
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TruckActivity.this);

            // set title
            alertDialogBuilder.setTitle("ENTER A VALUE HAS NOT BEEN ENTERED FOR ALL FIELDS");

            // set dialog message
            alertDialogBuilder
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close current activity
//                                TruckActivity.this.finish();
                        }
                    });
//                        .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,int id) {
//                                Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
//                                startActivity(intent);
//                            }
//                        });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent intent = new Intent(TruckActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            finish(); // call this to finish the current activity

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    View.OnClickListener bTLaunchClickHandler = new View.OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(TruckActivity.this, SriBlutoothConnector.class);
            startActivity(intent);
        }
    };

    View.OnClickListener redGreenBtnHandler = new View.OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(TruckActivity.this, R.class);
            startActivity(intent);
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {

        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckImageLocation = preferences.getString("truckV_truckImage", "");

        if (truckImageLocation.equalsIgnoreCase("")) {
            Bitmap truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
            truckLay_takeTruckImage_imgBtn.setImageBitmap(truckImageBitmap);
            return;
        }

        // Now change ImageView's dimensions to match the scaled image
//        Bitmap truckImageBitmap = BitmapFactory.decodeFile(truckImageLocation);
        Bitmap truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation, TruckActivity.width, TruckActivity.height);

        try {

            ExifInterface exif = new ExifInterface(truckImageLocation);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

            Bitmap resized = getScaledBitmap(truckImageBitmap, TruckActivity.width, TruckActivity.height);

            truckLay_takeTruckImage_imgBtn.setImageBitmap(resized);
            truckLay_takeTruckImage_imgBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);



        } catch (Exception e) {
            Log.d("EXIF", e.toString());
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        try {
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Bitmap getScaledBitmap(Bitmap b, int reqWidth, int reqHeight) {
        if (b != null) {
            if(reqWidth < 1 && reqHeight < 1) {
                reqWidth = TruckActivity.width;
                reqHeight = TruckActivity.height;
            }

            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
        }
        return b;
    }

    public static Bitmap getRoundedCornerImage(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 100;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;

    }

    public String getPath(Uri uri) {
        // Will return "image:x*"
        // getDocumentId requires api level 19
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = this.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{id}, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        return filePath;
    }


}