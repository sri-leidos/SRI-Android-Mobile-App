package srimobile.aspen.leidos.com.sri.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import srimobile.aspen.leidos.com.sri.R;
import srimobile.aspen.leidos.com.sri.activity.TruckActivity;
import srimobile.aspen.leidos.com.sri.utils.ResizeImage;

public class OkHttpUpload {



	 public static void saveImage(Context context, String imageFileLocation, String imageId) {
		    try {
//				String url = "http://192.168.43.10:8080/DashCon/resources/truck/saveimage";

				OkHttpClient client = new OkHttpClient();
                System.out.println("truckimagelocation" + imageFileLocation);
                System.out.println("truckimagelocation" + imageFileLocation);
                System.out.println("truckimagelocation" + imageFileLocation);

                byte [] truckImgBytes;
                if (imageFileLocation.length() != 0) {

                    Bitmap truckImage = ResizeImage.decodeSampledBitmapFromFile(imageFileLocation, TruckActivity.width, TruckActivity.height);

                    Bitmap resized = Bitmap.createScaledBitmap(truckImage, TruckActivity.width, TruckActivity.height, true);

                    FileOutputStream fileOutputStream = new FileOutputStream(imageFileLocation + "resized");

                    // keep original full size image, avoid compression of compression
                    resized.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream);

                    FileInputStream truckImageResizedFile = new FileInputStream(imageFileLocation + "resized");

                    truckImgBytes = IOUtils.toByteArray(truckImageResizedFile);

                } else {
                    Bitmap truckImageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.truck_photo_placeholder_grayed);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    truckImageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    truckImgBytes = bos.toByteArray();
                }

		        RequestBody requestBody = new MultipartBuilder()
		                .type(MultipartBuilder.FORM)
		                .addFormDataPart("file", "someName", RequestBody.create(MediaType.parse("image/png"), truckImgBytes) )
		                .addFormDataPart("imageId", imageId)
		                .build();

		        Request request = new Request.Builder()
		                .url(WebServicePoster.SAVE_IMAGE)
		                .post(requestBody)
		                .build();

		        client.newCall(request).enqueue(new Callback() {

		            @Override
		            public void onFailure(Request request, IOException e) {
	                	System.out.println("OKHTTP" + "UPLOAD FAILURE");
		            }

		            @Override
		            public void onResponse(Response response) throws IOException {
		                if (!response.isSuccessful()) {
		                	System.out.println("OKHTTP" + "UPLOAD RESPONSE FAILURE");
		                } else {
		                	System.out.println("OKHTTP" + "UPLOAD RESPONSE SUCCESS");
		                }
		                // Upload successful
		            }
		        });

		    } catch (Exception ex) {
            	System.out.println("OKHTTP" + "UPLOAD EXCEPTION " + ex);
		    }
	 }
}

