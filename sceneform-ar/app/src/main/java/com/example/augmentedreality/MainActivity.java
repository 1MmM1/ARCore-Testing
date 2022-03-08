package com.example.augmentedreality;

import android.os.Bundle;
import android.util.Log;
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
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
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

import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {
    private static final String DEBUG_TAG = "MainActivity";
    /*
        Flag value to change which test case the app is running:
        0 - base case
        1 - test creation of 2 colocated cubes on the same anchor
        2 - test creation of 2 colocated cubes on the same anchor when done in parallel
        3 - test click handling of 2 colocated cubes on the same anchor
     */
    private static final int TEST_CASE = 3;

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
        Log.i(DEBUG_TAG, "Running test case: " + TEST_CASE);
        switch (TEST_CASE) {
            case 0:
                // base case
                break;
            case 1:
                // Test overlapping objects
                createColocatedCubes(hitResult, plane, motionEvent);
                break;
            case 2:
                // Test overlapping objects in parallel
                createColocatedCubesInParallel(hitResult, plane, motionEvent);
                break;
            case 3:
                createClickableColocatedCubes(hitResult, plane, motionEvent);
                break;
            default:
                break;
        }
    }

    private void createColocatedCubesInParallel(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        CompletableFuture.allOf(
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
                        }),

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
                        })
        );
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

    private void createClickableColocatedCubes(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
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
                    transformableNode.setOnTapListener((hitTestResult, tapMotionEvent) -> {
                        Toast.makeText(getApplicationContext(), "Tapped red cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping red cube");
                    });
//                    transformableNode.setOnTouchListener((hitTestResult, tapMotionEvent) -> {
//                        Log.i(DEBUG_TAG, "touching red cube");
//                        return false;
//                    });
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
                    transformableNode.setOnTapListener((hitTestResult, tapMotionEvent) -> {
                        Toast.makeText(getApplicationContext(), "Tapped blue cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping blue cube");
                    });
//                    transformableNode.setOnTouchListener((hitTestResult, tapMotionEvent) -> {
//                        Log.i(DEBUG_TAG, "touching blue cube");
//                        return false;
//                    });
                });
    }
}
