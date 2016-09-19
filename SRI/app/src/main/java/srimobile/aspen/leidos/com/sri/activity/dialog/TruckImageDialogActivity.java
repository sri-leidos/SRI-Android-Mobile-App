package srimobile.aspen.leidos.com.sri.activity.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.activity.TruckActivity;
import srimobile.aspen.leidos.com.sri.utility.CustomImageView;
import srimobile.aspen.leidos.com.sri.utils.ResizeImage;

/**
 * Created by walswortht on 4/13/2015.
 */
public class TruckImageDialogActivity extends Activity {
    public static final int TRUCK_SD_IMAGE = 2343456;

    CustomImageView truckDiag_truckImage_imgVw;

    ImageButton truckDiag_camera_imgBtn;
    ImageButton truckDiag_folder_imgBtn;
    ImageButton truckDiag_cancel_imgBtn;
    ImageButton truckDiag_ok_imgBtn;

    public static int REQUEST_CODE = 929346389;

    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.truck_photo_dialog_layout);

//        View view = inflater.inflate(R.layout.truck_photo_dialog_layout, container, false);
//        setContentView(R.layout.truck_photo_dialog_layout);

//        getDialog().setTitle("SELECT TRUCK IMAGE");
//        getDialog().setCanceledOnTouchOutside(false);

        truckDiag_truckImage_imgVw = (CustomImageView) findViewById(R.id.truckDiag_truckImage_imgVw);

        truckDiag_camera_imgBtn = (ImageButton) findViewById(R.id.truckDiag_camera_imgBtn);
        truckDiag_camera_imgBtn.setOnClickListener(getPhotoFromCamera);

        truckDiag_folder_imgBtn= (ImageButton) findViewById(R.id.truckDiag_folder_imgBtn);
        truckDiag_folder_imgBtn.setOnClickListener(getPhotoFromSd);

        truckDiag_cancel_imgBtn= (ImageButton) findViewById(R.id.truckDiag_cancel_imgBtn);
        truckDiag_cancel_imgBtn.setOnClickListener(cancel);

        truckDiag_ok_imgBtn= (ImageButton) findViewById(R.id.truckDiag_ok_imgBtn);
        truckDiag_ok_imgBtn.setOnClickListener(ok);

        //
        // DISPLAY CURRENT TRUCK IMAGE
        //
//        SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
        SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
        String truckImageLocation = preferences.getString("truckV_truckImage", "");

        // Now change ImageView's dimensions to match the scaled image
        Bitmap truckImageBitmap = null;

        try {
            if (truckImageLocation.equalsIgnoreCase("")) {
                truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
                Bitmap resized = getScaledBitmap(truckImageBitmap, 250, 250);

                truckDiag_truckImage_imgVw.setImageBitmap(resized);
                truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {

                truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation, 250, 250);
//                truckImageBitmap  = BitmapFactory.decodeFile(truckImageLocation);

                ExifInterface exif = new ExifInterface(truckImageLocation);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

                Bitmap resized = getScaledBitmap(truckImageBitmap, 250, 250);

                truckDiag_truckImage_imgVw.setImageBitmap(resized);
                truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } catch (Exception e) {
            Log.d("EXIF", e.toString());


        }

//        return view;
    }


    public static Bitmap getScaledBitmap(Bitmap b, int reqWidth, int reqHeight) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
    }


    // SD PHOTO
    // SD PHOTO
    // SD PHOTO
    View.OnClickListener getPhotoFromSd = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(intent, TruckImageDialogActivity.TRUCK_SD_IMAGE);

            } catch (Exception e) {
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
                Log.d("TRUCKACTIVITY", "takePhotoFromCameraHandler" + e);
            }
        }
    };

    // CAMERA PHOTO
    // CAMERA PHOTO
    // CAMERA PHOTO
    View.OnClickListener getPhotoFromCamera = new View.OnClickListener() {
        public void onClick(View v) {
//            Intent intent = new Intent(getActivity(), TruckPhotoFromCameraActivity.class);
            Intent intent = new Intent(TruckImageDialogActivity.this, TruckPhotoFromCameraActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivityForResult(intent, TruckPhotoFromCameraActivity.REQUEST_CODE);

//            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
//            startActivityForResult(cameraIntent, TruckPhotoFromCameraActivity.REQUEST_CODE);
        }

    };

    // CANCEL IMAGE
    // CANCEL IMAGE
    // CANCEL IMAGE
    View.OnClickListener cancel = new View.OnClickListener() {
        public void onClick(View v) {

//            dismiss();
            Intent intent = new Intent(TruckImageDialogActivity.this, TruckActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivityForResult(intent, TruckPhotoFromCameraActivity.REQUEST_CODE);

        }
    };

    // OK KEEP IMAGE
    // OK KEEP IMAGE
    // OK KEEP IMAGE
    View.OnClickListener ok = new View.OnClickListener() {
        public void onClick(View v) {

//            SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
            SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();

            String truckLocationTemp = preferences.getString("truckV_truckImage_temp", "");
            String truckLocation = preferences.getString("truckV_truckImage", "");

            edit.putString("truckV_truckImage", "");

            if (truckLocationTemp.equalsIgnoreCase("")
                    || truckLocationTemp.length() == 0
                    ) {
                edit.putString("truckV_truckImage", "");
            } else {
                edit.putString("truckV_truckImage", truckLocationTemp);
            }
            edit.commit();

            Log.d("TRUCK ACTIVITY", truckLocationTemp);
            Log.d("TRUCK ACTIVITY", truckLocation);

//            dismiss();
            Intent intent = new Intent(TruckImageDialogActivity.this, TruckActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivityForResult(intent, TruckPhotoFromCameraActivity.REQUEST_CODE);

        }
    };

    //
    // RETURN FROM INTENT
    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //
        // PHOTO FROM CAMERA INTENT FINISH
        //
//        if (requestCode == TruckPhotoFromCameraActivity.REQUEST_CODE) {

            if (requestCode == TruckPhotoFromCameraActivity.REQUEST_CODE ) {

                String[] filePathColumn = {MediaStore.Images.Media.DATA};


//                Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, filePathColumn, null, null, null);
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

//                SharedPreferences preferences = getActivity().getApplicationContext().getSharedPreferences("SRI", Context.MODE_PRIVATE);
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("SRI", Context.MODE_PRIVATE);
                try {
                    SharedPreferences.Editor edit = preferences.edit();

                    Log.d("IMAGE PATH", source.getCanonicalPath());
                    Log.d("IMAGE PATH", source.getCanonicalPath());
                    Log.d("IMAGE PATH", source.getCanonicalPath());

                    if (source.getCanonicalPath().length() > 0) {
                        edit.putString("truckV_truckImage_temp", source.getCanonicalPath().toString());

                        Bitmap truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(source.getCanonicalPath().toString(), 250, 250);
//                truckImageBitmap  = BitmapFactory.decodeFile(truckImageLocation);

                        ExifInterface exif = new ExifInterface(source.getCanonicalPath().toString());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

                        Bitmap resized = getScaledBitmap(truckImageBitmap, 250, 250);

                        truckDiag_truckImage_imgVw.setImageBitmap(resized);
                        truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);


                    } else {
                        edit.putString("truckV_truckImage_temp", preferences.getString("truckV_truckImage", ""));
                    }
                    edit.commit();

                } catch (Exception e) {
                    Log.d("TRUCK ERROR IMAGE", e.toString());
                    Log.d("TRUCK ERROR IMAGE", e.toString());
                    Log.d("TRUCK ERROR IMAGE", e.toString());
                }

//            SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
//            String truckImageLocation = preferences.getString("truckV_truckImage_temp", "");
//
//            Log.d("TRUCK ACTIVITY", truckImageLocation);
//            Log.d("TRUCK ACTIVITY", truckImageLocation);
//            Log.d("TRUCK ACTIVITY", truckImageLocation);
//
//                Bitmap truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation, TruckActivity.width, TruckActivity.height);
//
//                try {
//                    ExifInterface exif = new ExifInterface(truckImageLocation);
//                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//                    truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);
//
//                    Bitmap resized = getScaledBitmap(truckImageBitmap, TruckActivity.width, TruckActivity.height);
//
//                    truckDiag_truckImage_imgVw.setImageBitmap(resized);
//                    truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//                } catch (Exception e) {
//                    Log.d("EXIF", e.toString());
//
//                    truckImageLocation = preferences.getString("truckV_truckImage", "");
//
//                    if (truckImageLocation.equalsIgnoreCase("")) {
//                        truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
//                        truckDiag_truckImage_imgVw.setImageBitmap(truckImageBitmap);
//                    }
//                }
        }

        //
        // PHOTO FROM STATIC DRIVE INTENT FINISH
        //
        if (requestCode == TruckImageDialogActivity.TRUCK_SD_IMAGE) {

            if (data != null && data.getData() != null) {

                Uri selectedImageUri = data.getData();

                try {
                    String truckImageLocation = getPath(selectedImageUri);

//                    SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
                    SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = preferences.edit();

                    edit.putString("truckV_truckImage_temp", truckImageLocation);
                    edit.commit();

                    Log.d("TRUCK ACTIVITY", truckImageLocation);
                    Log.d("TRUCK ACTIVITY", truckImageLocation);
                    Log.d("TRUCK ACTIVITY", truckImageLocation);

                    //
                    // DISPLAY CURRENT TRUCK IMAGE
                    //
                    Bitmap truckImageBitmap = ResizeImage.decodeSampledBitmapFromFile(truckImageLocation, TruckActivity.width, TruckActivity.height);

                    try {
                        ExifInterface exif = new ExifInterface(truckImageLocation);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        truckImageBitmap = rotateBitmap(truckImageBitmap, orientation);

                        Bitmap resized = getScaledBitmap(truckImageBitmap, TruckActivity.width, TruckActivity.height);

                        truckDiag_truckImage_imgVw.setImageBitmap(resized);
                        truckDiag_truckImage_imgVw.setScaleType(ImageView.ScaleType.CENTER_CROP);


                    } catch (Exception e) {
                        Log.d("EXIF", e.toString());

                        truckImageLocation = preferences.getString("truckV_truckImage", "");

                        if (truckImageLocation.equalsIgnoreCase("")) {
                            truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
                            truckDiag_truckImage_imgVw.setImageBitmap(truckImageBitmap);
                        }
                    }

                } catch (Exception e) {
                    Log.d("ExceImg", e.toString());
                    Log.d("ExceImg", e.toString());

//                    SharedPreferences preferences = getActivity().getSharedPreferences("SRI", Context.MODE_PRIVATE);
                    SharedPreferences preferences = getSharedPreferences("SRI", Context.MODE_PRIVATE);
                    String excepTruckImageLocation = preferences.getString("truckV_truckImage", "");

                    if (excepTruckImageLocation.equalsIgnoreCase("truck_photo_placeholder_grayed")) {
                        Bitmap truckImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.truck_photo_placeholder_grayed);
                        truckDiag_truckImage_imgVw.setImageBitmap(truckImageBitmap);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

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

//        Cursor cursor = getActivity().getContentResolver().
                Cursor cursor = getContentResolver().
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

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
//        Log.d("mesg", "onattch");
//    }

//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//        final Activity activity = getActivity();
//
//        if (activity instanceof DialogInterface.OnDismissListener) {
//            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
//        }
//    }

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

}

