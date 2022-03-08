package com.example.augmentedreality;

import android.os.Bundle;
import android.view.MotionEvent;

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
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ArFragment mArFragment;

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

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Test overlapping objects
        createColocatedCubes(hitResult, plane, motionEvent);
    }

    private void createColocatedCubes(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());


        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(244, 0, 0))
                .thenAccept(material -> {
                    Vector3 vector3 = new Vector3(0.1f, 0.1f, 0.1f);
                    ModelRenderable model = ShapeFactory.makeCube(vector3,
                            Vector3.zero(), material);
                    model.setShadowCaster(false);
                    model.setShadowReceiver(false);

                    TransformableNode transformableNode = new TransformableNode(mArFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(model);
                    transformableNode.select();
                });

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(0, 0, 244))
                .thenAccept(material -> {
                    Vector3 vector3 = new Vector3(0.1f, 0.1f, 0.1f);
                    ModelRenderable model = ShapeFactory.makeCube(vector3,
                            Vector3.zero(), material);
                    model.setShadowCaster(false);
                    model.setShadowReceiver(false);

                    TransformableNode transformableNode = new TransformableNode(mArFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(model);
                    transformableNode.select();
                });
    }
}
