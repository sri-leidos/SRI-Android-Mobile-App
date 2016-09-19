package srimobile.aspen.leidos.com.sri.activity.dialog;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.activity.ProfileActivity;
import srimobile.aspen.leidos.com.sri.activity.TruckActivity;
import srimobile.aspen.leidos.com.sri.utils.ResizeImage;

/**
 * Created by walswortht on 4/15/2015.
 */
public class TruckPhotoFromCameraActivity extends Activity {

    public final static int REQUEST_CODE = 1234;

    Intent intent;
    public static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);

        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
//        photoButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//            }
//        });

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.camera_layout);
//
////        requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        intent = getIntent();
//
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "New Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
//        Uri mImageUri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
//        startActivityForResult(takePictureIntent, TruckPhotoFromCameraActivity.REQUEST_CODE);

    }
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo);
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == TruckPhotoFromCameraActivity.CAMERA_REQUEST) {

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, null, null, null);
            if (cursor == null)
                return;
            // find the file in the media area
            cursor.moveToLast();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            File source = new File(filePath);
            source.getParentFile().mkdirs();

            cursor.close();

            try {
                SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();

                Log.d("IMAGE PATH", source.getCanonicalPath());
                Log.d("IMAGE PATH", source.getCanonicalPath());
                Log.d("IMAGE PATH", source.getCanonicalPath());

                if (source.getCanonicalPath().length() > 0) {
                    edit.putString("truckV_truckImage_temp", source.getCanonicalPath().toString());
                } else {
                    edit.putString("truckV_truckImage_temp", preferences.getString("truckV_truckImage", ""));
                }

                edit.commit();

            } catch (Exception e) {
                Log.d("TRUCK ERROR IMAGE", e.toString());
                Log.d("TRUCK ERROR IMAGE", e.toString());
                Log.d("TRUCK ERROR IMAGE", e.toString());
            }

        }

        super.onActivityResult(requestCode, resultCode, data);

        finish();

    }
}

