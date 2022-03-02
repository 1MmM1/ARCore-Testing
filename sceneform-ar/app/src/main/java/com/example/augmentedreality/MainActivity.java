package com.example.augmentedreality;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment mArFragment;
    private Renderable model;
    private ViewRenderable viewRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.ar_fragment, ArFragment.class, null)
                        .commit();
            }
        }

//        loadModels();
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.ar_fragment) {
            mArFragment = (ArFragment) fragment;
            mArFragment.setOnSessionConfigurationListener(this);
            mArFragment.setOnViewCreatedListener(this);
            mArFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        mArFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    public void loadModels() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("TigerModel.glb"))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
//        ViewRenderable.builder()
//                .setView(this, R.layout.view_model_title)
//                .build()
//                .thenAccept(viewRenderable -> {
//                    MainActivity activity = weakActivity.get();
//                    if (activity != null) {
//                        activity.viewRenderable = viewRenderable;
//                    }
//                })
//                .exceptionally(throwable -> {
//                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
//                    return null;
//                });
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());


        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(244, 244, 244))
                .thenAccept(material -> {
                    Vector3 vector3 = new Vector3(0.05f, 0.05f, 0.05f);
                    ModelRenderable model = ShapeFactory.makeCube(vector3,
                            Vector3.zero(), material);
                    model.setShadowCaster(false);
                    model.setShadowReceiver(false);

                    TransformableNode transformableNode = new TransformableNode(mArFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(model);
                    transformableNode.select();
                });

////        if (model == null || viewRenderable == null) {
//        if (model == null) {
//            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Create the Anchor.
//        Anchor anchor = hitResult.createAnchor();
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        anchorNode.setParent(mArFragment.getArSceneView().
//
//                getScene());
//
//        // Create the transformable model and add it to the anchor.
//        TransformableNode model = new TransformableNode(mArFragment.getTransformationSystem());
//        model.setParent(anchorNode);
//        model.setRenderable(this.model)
//                .
//
//                        animate(true).
//
//                start();
//        model.select();
//
////        Node titleNode = new Node();
////        titleNode.setParent(model);
////        titleNode.setEnabled(false);
////        titleNode.setLocalPosition(new Vector3(0.0f, 1.0f, 0.0f));
////        titleNode.setRenderable(viewRenderable);
////        titleNode.setEnabled(true);
    }
}
