package com.akash.trackingobject;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.hardware.Camera;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import android.content.Context;
import 	android.content.Intent;
import java.io.IOException;
import 	android.net.Uri;
import android.provider.MediaStore;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.graphics.Color;
import android.hardware.Camera.PictureCallback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap.CompressFormat;

public class TrackingActivity extends AppCompatActivity {

    // Load the native libraries.
    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("nftSimpleNative");
    }


    private Camera mCamera;
    private CameraSurface mPreview;
    private ImageView imgOverLay,camera_image;
    private float xCoOrdinate, yCoOrdinate;
    private final static int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private Button buttonClick;
    public static int count = 0;
    int TAKE_PHOTO_CODE = 0;
    private FileOutputStream fos;
    private File dir_image2,dir_image;
    private Bitmap bmp,bmp1;
    private BitmapFactory.Options options,o,o2;
    private FileInputStream fis;
    ByteArrayInputStream fis2;
    private RelativeLayout CamView;
    private ByteArrayOutputStream bos;
    final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!checkPermissionForCamera()) {
            requestPermissionForCamera();
            Log.d("MSG", "NO Camera");
        } else {

            mCamera = getCameraInstance();
            // if(mCamera != null) {

            mPreview = new CameraSurface(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.frame_layout);
            preview.addView(mPreview, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            camera_image = (ImageView) findViewById(R.id.camera_image);

            imgOverLay = (ImageView) findViewById(R.id.imageView);
            imgOverLay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            xCoOrdinate = view.getX() - event.getRawX();
                            yCoOrdinate = view.getY() - event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                            break;
                        default:
                            return false;
                    }
                    return true;
                }
            });


            buttonClick = (Button) findViewById(R.id.buttonClick);
            buttonClick.setOnClickListener(new View.OnClickListener() {
                                               public void onClick(View v) {

                                                   buttonClick.setClickable(false);
                                                   buttonClick.setVisibility(View.INVISIBLE);  //<-----HIDE HERE
                                                   mCamera.takePicture(null, null, mPicture);
                                          //Now you have a target image in My_Custom_folder

                                                  //rest code is for tracking using ARToolKit SDK IMport


                                               }

                                           });


//            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });
        }
    }

    public Camera getCameraInstance() {
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
//                Camera.Parameters parameters = mCamera.getParameters();
//                parameters.set("orientation", "portrait");
//                mCamera.setParameters(parameters);
                //mCamera.setDisplayOrientation(90);
            }// attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist);
            Log.d("TAG", "Camera not found: " + e);
        }
        return mCamera; // returns null if camera is unavailable
    }


    public void requestPermissionForCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    public boolean checkPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mCamera = getCameraInstance();
                    // if(mCamera != null) {

                    mPreview = new CameraSurface(this, mCamera);
                    FrameLayout preview = (FrameLayout) findViewById(R.id.frame_layout);
                    preview.addView(mPreview);
                    imgOverLay=(ImageView) findViewById(R.id.imageView);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            dir_image2 = new  File(Environment.getExternalStorageDirectory()+
                    File.separator+"My_Custom_Folder");

         // dir_image2.mkdirs();
            if (!dir_image2.exists()) {
                dir_image2.mkdir();
            }
            else{
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            }

            File tmpFile = new File(dir_image2,"TempImage.jpg");
            try {
                fos = new FileOutputStream(tmpFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
            }
            options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bmp1 = decodeFile(tmpFile);
            bmp=Bitmap.createScaledBitmap(bmp1,CamView.getWidth(), CamView.getHeight(),true);
            camera_image.setImageBitmap(bmp);
            tmpFile.delete();
            TakeScreenshot();

        }
    };
    public Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
            int IMAGE_MAX_SIZE = 1000;
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }
    public void TakeScreenshot(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int nu = preferences.getInt("image_num",0);
        nu++;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("image_num",nu);
        editor.commit();
        CamView.setDrawingCacheEnabled(true);
        CamView.buildDrawingCache(true);
        bmp = Bitmap.createBitmap(CamView.getDrawingCache());
        CamView.setDrawingCacheEnabled(false);
        bos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        fis2 = new ByteArrayInputStream(bitmapdata);

        String picId=String.valueOf(nu);
        String myfile="MyImage"+picId+".jpeg";

        dir_image = new  File(Environment.getExternalStorageDirectory()+
                File.separator+"My_Custom_Folder");
        dir_image.mkdirs();

        try {
            File tmpFile = new File(dir_image,myfile);
            fos = new FileOutputStream(tmpFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = fis2.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fis2.close();
            fos.close();

            Toast.makeText(getApplicationContext(),
                    "The file is saved at :/My_Custom_Folder/"+"MyImage"+picId+".jpeg",Toast.LENGTH_LONG).show();

            bmp1 = null;
            camera_image.setImageBitmap(bmp1);
            mCamera.startPreview();
            buttonClick.setClickable(true);
            buttonClick.setVisibility(View.VISIBLE);//<----UNHIDE HER
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
