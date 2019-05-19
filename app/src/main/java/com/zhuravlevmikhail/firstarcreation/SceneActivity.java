package com.zhuravlevmikhail.firstarcreation;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class SceneActivity extends AppCompatActivity implements Scene.OnUpdateListener {


    private ArFragment mArFragment;
    private boolean shouldAddModel = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);

        mArFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        mArFragment.getPlaneDiscoveryController().hide();


        //Request permission
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SceneActivity.this, "Permission need to display camera", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        initSceneView();
    }

    private void initSceneView() {
        mArFragment.getArSceneView().getScene().addOnUpdateListener(this :: onUpdateFrame);
    }

    public boolean buildDatabase(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap bitmap = loadImage();
        if (bitmap == null){
            return false;
        }
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("fox_qr", bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private Bitmap loadImage() {
        try {
            InputStream is = getAssets().open("fox_qr.jpeg");
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = mArFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage image : augmentedImages) {
            if (image.getTrackingState() == TrackingState.TRACKING) {
                if (image.getName().equals("fox_qr") && shouldAddModel) {
                    try {
                        MyArNode myArNode = new MyArNode(this, R.raw.model, R.layout.discr_view);
                        myArNode.setImage(image, mArFragment);
                    } catch (Exception e) {
                        Log.e("Render", e.toString());
                    }
                    shouldAddModel = false;
                }
            }
        }
    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = mArFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages  = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage image : augmentedImages) {
            if (image.getTrackingState() == TrackingState.TRACKING){
                if (image.getName().equals("fox_qr") && shouldAddModel){
                    MyArNode myArNode = new MyArNode(this, R.raw.model, R.layout.discr_view);
                    myArNode.setImage(image, mArFragment);
                    shouldAddModel = false;
                }
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SceneActivity.this, "Permission need to display camera", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mArFragment.getArSceneView().pause();
    }
}

