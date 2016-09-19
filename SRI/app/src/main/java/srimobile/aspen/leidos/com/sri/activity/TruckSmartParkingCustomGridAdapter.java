package srimobile.aspen.leidos.com.sri.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import srimobile.aspen.leidos.com.sri.R;

/**
 * Created by walswortht on 5/26/2015.
 */
public class TruckSmartParkingCustomGridAdapter extends BaseAdapter{

    Context context;
    ArrayList<TruckSmartParking.TruckStopDetailsVo> gridItemsAL;

    private static LayoutInflater inflater=null;

    static HashMap<String, Bitmap> logoImageCacheBitMap = null;

    public TruckSmartParkingCustomGridAdapter(TruckSmartParking mainActivity, ArrayList<TruckSmartParking.TruckStopDetailsVo> gridItems) {

        context             =   mainActivity;
        this.gridItemsAL    =   gridItems;

        if (logoImageCacheBitMap == null) {
            logoImageCacheBitMap = new HashMap<String, Bitmap>();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return gridItemsAL.size();
    }

    @Override
    public Object getItem(int position) {

        return gridItemsAL.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView location_name;
        TextView name;
        TextView distance;
        ImageView truckStopImg;

        TextView latlong;
        TextView bearingTv;
        TextView tspsurlTv;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View gridItem;

        if (convertView == null) {

            gridItem = new View(context);

            gridItem = inflater.inflate(R.layout.truck_parking_grid_item, null);

            try {

                Holder holder = new Holder();

                holder.name = (TextView) gridItem.findViewById(R.id.text1);
                holder.location_name = (TextView) gridItem.findViewById(R.id.text2);
                holder.distance = (TextView) gridItem.findViewById(R.id.text3);
                holder.truckStopImg = (ImageView) gridItem.findViewById(R.id.truckStopImg);

//                holder.latlong = (TextView) gridItem.findViewById(R.id.gps);
//                holder.bearingTv = (TextView) gridItem.findViewById(R.id.bearing);
//                holder.tspsurlTv = (TextView) gridItem.findViewById(R.id.tspsurl);
//                holder.latlong.setText(gridItemsAL.get(position).getToLatitude() + "  " + gridItemsAL.get(position).getToLongitude());
//                holder.bearingTv.setText(gridItemsAL.get(position).getBearing());
//                holder.tspsurlTv.setText(gridItemsAL.get(position).getTspsurl());


                String urlBitmap = gridItemsAL.get(position).getBitmapUrl();

//            String URL = "https://s3.amazonaws.com/tsps_prod/chains/thumb/12-pilot.jpg?1406587345";

                Bitmap image = null;

                // get image
                if (urlBitmap.trim().length() != 0) {
                    // download bitmap image
                    holder.truckStopImg.setTag(urlBitmap);
                    AsyncTask task = new DownloadImageTask(holder.truckStopImg).execute();
                }

                holder.name.setText(gridItemsAL.get(position).getName());
                holder.location_name.setText(gridItemsAL.get(position).getLocation_text());
                holder.distance.setText(gridItemsAL.get(position).getDistance());

                LinearLayout truckParkingGridItem = (LinearLayout) gridItem.findViewById(R.id.gridRow);

                    // DISABLED LAUNCHING OF TSPS
                    truckParkingGridItem.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                                final String appPackageName = "com.tsps.hud"; // getPackageName() from Context or Activity object
                                boolean isAppInstalled = appInstalledOrNot(appPackageName);
                                if (isAppInstalled) {
                                    if (((TruckSmartParking)context).speed <= .3 )  { // 10 mph

                                        Intent launchIntent = context.getApplicationContext().getPackageManager().getLaunchIntentForPackage(appPackageName);
                                        context.startActivity(launchIntent);
                                    } else {
                                        new AlertDialog.Builder(context)
                                                .setTitle("SPEED GREATER THAN 10 MPH")
                                                .setMessage("SPEED GREATER THAN 10 MPH PLEASE ALLOW UP TO 10 SECONDS FOR AN ACCURATE SPEED READING")
                                                .setPositiveButton("SPEED GREATER THAN 10 MPH", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                }
                                            )
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                    }
                                } else {


                                    new AlertDialog.Builder(context)
                                            .setTitle("INSTALL TSPS")
                                            .setMessage("INSTALL TSPS")
                                            .setPositiveButton("INSTALL TSPS", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                }
                                                }
                                            })
                                            .setNegativeButton("BACK TO SRI", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                }
                            }
                });




            } catch (Exception e) {

            }
        }else {
            gridItem = (View)convertView;
        }

        return gridItem;
    }


    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView image) {
            this.imageView = image;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String url = ((String)imageView.getTag() );
            Bitmap bm = download_Image((String)imageView.getTag());

            return bm;
        }

        protected void onPostExecute(Bitmap result) {

            imageView.setImageBitmap(result);

        }

        private Bitmap download_Image(String url) {

            Bitmap bm = null;
            try {
                if ( Patterns.WEB_URL.matcher(url).matches() ) {

                    URL aURL = new URL(url);
                    URLConnection conn = aURL.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                }

            } catch (IOException e) {

            }
            return bm;

        }
        public Bitmap loadBitmap(String url)
        {
            Bitmap bm = null;

            bm = logoImageCacheBitMap.get(url);
            if (bm != null) {
                return bm;
            }


            InputStream is = null;
            BufferedInputStream bis = null;
            try
            {
                URLConnection conn = new URL(url).openConnection();
                conn.connect();
                is = conn.getInputStream();
                bis = new BufferedInputStream(is, 8192);
                bm = BitmapFactory.decodeStream(bis);
            }
            catch (Exception e)
            {

            }
            finally {
                if (bis != null)
                {
                    try
                    {
                        bis.close();
                    }
                    catch (IOException e)
                    {

                    }
                }
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {

                    }
                }
            }
            return bm;
        }

    }

}
