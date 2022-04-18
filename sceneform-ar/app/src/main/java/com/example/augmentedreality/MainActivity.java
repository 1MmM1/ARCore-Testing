package com.example.augmentedreality;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
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
        4 - test creation of invisible object
        5 - test creation of colocated invisible objects
        6 - test creation of object which plays sound
        7 - test synthetic click
        8 - test creation of object with null material
        9 - test creation of 2 colocated no material cubes
        10 - test creation of collision object bigger than visible object
     */
    private static final int TEST_CASE = 10;

    private ArFragment mArFragment;
    private Handler mHandler;
    private boolean syntheticOverride = false;

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
        mHandler = new Handler();
        mArFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        Log.i(DEBUG_TAG, "Running test case: " + TEST_CASE);
        Log.i(DEBUG_TAG, "Current click registered at (" + motionEvent.getX() + ", " + motionEvent.getY() + ")");
        Log.i(DEBUG_TAG, "MotionEvent type: " + motionEvent.getAction());
        Log.i(DEBUG_TAG, "Number of children nodes for this ArFragment: " + mArFragment.getArSceneView().getScene().getChildren().size());
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
            case 4:
                createInvisibleCube(hitResult, plane, motionEvent);
                break;
            case 5:
                createColocatedInvisibleCube(hitResult, plane, motionEvent);
                break;
            case 6:
                createInvisibleCubeWithSound(hitResult, plane, motionEvent);
                break;
            case 7:
                createCubeAfterDelaySynthetic(hitResult, plane, motionEvent);
                break;
            case 8:
                createNoMaterialCube(hitResult, plane, motionEvent);
                break;
            case 9:
                createColocatedNoMaterialCube(hitResult, plane, motionEvent);
                break;
            case 10:
                createLargeMesh(hitResult, plane, motionEvent);
                break;
            default:
                break;
        }
    }

    private void createLargeMesh(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(0, 244, 0))
                .thenAccept(material -> {
                    Vector3 vector3 = new Vector3(0.1f, 0.1f, 0.1f);
                    ModelRenderable model = ShapeFactory.makeCube(vector3,
                            Vector3.zero(), material);
                    model.setShadowCaster(false);
                    model.setShadowReceiver(false);

                    Vector3 collisionVector3 = new Vector3(0.5f, 0.5f, 0.5f);
                    model.setCollisionShape(new Box(collisionVector3));

                    TransformableNode transformableNode = new TransformableNode(mArFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(model);
                    transformableNode.select();
                    transformableNode.setOnTapListener((hitTestResult, tapMotionEvent) -> {
                        Toast.makeText(getApplicationContext(), "Tapped green cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping green cube");
                    });
                });
    }

    private void createCubeAfterDelaySynthetic(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (syntheticOverride) {
            createClickableColocatedCubes(hitResult, plane, motionEvent);
            syntheticOverride = false;
        } else {
            Toast.makeText(getApplicationContext(), "Start synthetic click", Toast.LENGTH_SHORT).show();
            syntheticOverride = true;
            mHandler.postDelayed(() -> {
                // Obtain MotionEvent object
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100;
                float x = 500f;
                float y = 1000f;
                // List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                int metaState = 0;
                MotionEvent syntheticMotionEvent = MotionEvent.obtain(
                        downTime,
                        eventTime,
                        MotionEvent.ACTION_UP,
                        x,
                        y,
                        metaState
                );

                // Dispatch touch event
                Log.i(DEBUG_TAG, "About to dispatch synthetic touch event");
                mArFragment.getArSceneView().dispatchTouchEvent(syntheticMotionEvent);
                Log.i(DEBUG_TAG, "After dispatch synthetic touch event");
            }, 5000);
        }
    }

    private void createInvisibleCubeWithSound(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(0, 244, 0))
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
                    MediaPlayer mp = MediaPlayer.create(this, R.raw.band_piece);
                    mp.setLooping(true);
                    transformableNode.setOnTapListener((hitTestResult, tapMotionEvent) -> {
                        Toast.makeText(getApplicationContext(), "Tapped green cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping green cube");
                        try {
                            mp.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
    }

    private void createColocatedInvisibleCube(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(0, 0, 0, 0))
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
                        Toast.makeText(getApplicationContext(), "Tapped invisible cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping invisible cube");
                    });
                });
        Toast.makeText(getApplicationContext(), "Made invisible cube", Toast.LENGTH_SHORT).show();
        Log.i(DEBUG_TAG, "Making invisible cube");

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
                });
    }

    private void createColocatedNoMaterialCube(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), null)
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
                        Toast.makeText(getApplicationContext(), "Tapped invisible cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping invisible cube");
                    });
                });
        Toast.makeText(getApplicationContext(), "Made invisible cube", Toast.LENGTH_SHORT).show();
        Log.i(DEBUG_TAG, "Making invisible cube");

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
                });
    }

    private void createNoMaterialCube(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), null)
                .thenAccept(material -> {
                    Vector3 vector3 = new Vector3(0.5f, 0.5f, 0.5f);
                    ModelRenderable model = ShapeFactory.makeCube(vector3,
                            Vector3.zero(), material);
                    model.setShadowCaster(false);
                    model.setShadowReceiver(false);

                    TransformableNode transformableNode = new TransformableNode(mArFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(model);
                    transformableNode.select();
                    transformableNode.setOnTapListener((hitTestResult, tapMotionEvent) -> {
                        Toast.makeText(getApplicationContext(), "Tapped invisible cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping invisible cube");
                    });
                });
        Toast.makeText(getApplicationContext(), "Made invisible cube", Toast.LENGTH_SHORT).show();
        Log.i(DEBUG_TAG, "Making invisible cube");
    }

    private void createInvisibleCube(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(mArFragment.getArSceneView().getScene());

        MaterialFactory.makeTransparentWithColor(getApplicationContext(), new Color(0, 0, 0, 0))
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
                        Toast.makeText(getApplicationContext(), "Tapped invisible cube", Toast.LENGTH_SHORT).show();
                        Log.i(DEBUG_TAG, "tapping invisible cube");
                    });
                });
        Toast.makeText(getApplicationContext(), "Made invisible cube", Toast.LENGTH_SHORT).show();
        Log.i(DEBUG_TAG, "Making invisible cube");
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
