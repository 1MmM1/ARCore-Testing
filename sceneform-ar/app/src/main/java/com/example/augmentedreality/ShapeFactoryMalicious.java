package com.example.augmentedreality;

import androidx.annotation.Nullable;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.Vertex;
import com.google.ar.sceneform.utilities.AndroidPreconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ShapeFactoryMalicious {
    private static final int COORDS_PER_TRIANGLE = 3;

    /**
     * Creates a {@link ModelRenderable} in the shape of a cube with the give specifications.
     *
     * @param size the size of the constructed cube
     * @param center the center of the constructed cube
     * @param material the material to use for rendering the cube
     * @return renderable representing a cube with the given parameters
     */
    @SuppressWarnings("AndroidApiChecker")
    // CompletableFuture requires api level 24
    public static ModelRenderable makeCube(Vector3 size, Vector3 center, Material material) {
        AndroidPreconditions.checkMinAndroidApiLevel();

        Vector3 extents = size.scaled(0.5f);

        Vector3 p0 = Vector3.add(center, new Vector3(-extents.x, -extents.y, extents.z));
        Vector3 p1 = Vector3.add(center, new Vector3(extents.x, -extents.y, extents.z));
        Vector3 p2 = Vector3.add(center, new Vector3(extents.x, -extents.y, -extents.z));
        Vector3 p3 = Vector3.add(center, new Vector3(-extents.x, -extents.y, -extents.z));
        Vector3 p4 = Vector3.add(center, new Vector3(-extents.x, extents.y, extents.z));
        Vector3 p5 = Vector3.add(center, new Vector3(extents.x, extents.y, extents.z));
        Vector3 p6 = Vector3.add(center, new Vector3(extents.x, extents.y, -extents.z));
        Vector3 p7 = Vector3.add(center, new Vector3(-extents.x, extents.y, -extents.z));

        Vector3 up = Vector3.up();
        Vector3 down = Vector3.down();
        Vector3 front = Vector3.forward();
        Vector3 back = Vector3.back();
        Vector3 left = Vector3.left();
        Vector3 right = Vector3.right();

        Vertex.UvCoordinate uv00 = new Vertex.UvCoordinate(0.0f, 0.0f);
        Vertex.UvCoordinate uv10 = new Vertex.UvCoordinate(1.0f, 0.0f);
        Vertex.UvCoordinate uv01 = new Vertex.UvCoordinate(0.0f, 1.0f);
        Vertex.UvCoordinate uv11 = new Vertex.UvCoordinate(1.0f, 1.0f);

        ArrayList<Vertex> vertices =
                new ArrayList<>(
                        Arrays.asList(
                                // Bottom
                                Vertex.builder().setPosition(p0).setNormal(down).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p1).setNormal(down).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p2).setNormal(down).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p3).setNormal(down).setUvCoordinate(uv00).build(),
                                // Left
                                Vertex.builder().setPosition(p7).setNormal(left).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p4).setNormal(left).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p0).setNormal(left).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p3).setNormal(left).setUvCoordinate(uv00).build(),
                                // Back
                                Vertex.builder().setPosition(p4).setNormal(back).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p5).setNormal(back).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p1).setNormal(back).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p0).setNormal(back).setUvCoordinate(uv00).build(),
                                // Front
                                Vertex.builder().setPosition(p6).setNormal(front).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p7).setNormal(front).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p3).setNormal(front).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p2).setNormal(front).setUvCoordinate(uv00).build(),
                                // Right
                                Vertex.builder().setPosition(p5).setNormal(right).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p6).setNormal(right).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p2).setNormal(right).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p1).setNormal(right).setUvCoordinate(uv00).build(),
                                // Top
                                Vertex.builder().setPosition(p7).setNormal(up).setUvCoordinate(uv01).build(),
                                Vertex.builder().setPosition(p6).setNormal(up).setUvCoordinate(uv11).build(),
                                Vertex.builder().setPosition(p5).setNormal(up).setUvCoordinate(uv10).build(),
                                Vertex.builder().setPosition(p4).setNormal(up).setUvCoordinate(uv00).build()));

        final int numSides = 6;
        final int verticesPerSide = 4;
        final int trianglesPerSide = 2;

        ArrayList<Integer> triangleIndices =
                new ArrayList<>(numSides * trianglesPerSide * COORDS_PER_TRIANGLE);
        for (int i = 0; i < numSides; i++) {
            // First triangle for this side.
            triangleIndices.add(3 + verticesPerSide * i);
            triangleIndices.add(1 + verticesPerSide * i);
            triangleIndices.add(0 + verticesPerSide * i);

            // Second triangle for this side.
            triangleIndices.add(3 + verticesPerSide * i);
            triangleIndices.add(2 + verticesPerSide * i);
            triangleIndices.add(1 + verticesPerSide * i);
        }

        RenderableDefinition.Submesh submesh =
                RenderableDefinition.Submesh.builder().setTriangleIndices(triangleIndices).setMaterial(material).build();

        RenderableDefinition renderableDefinition =
                RenderableDefinition.builder()
                        .setVertices(vertices)
                        .setSubmeshes(Arrays.asList(submesh))
                        .build();

        CompletableFuture<ModelRenderable> future =
                ModelRenderable.builder().setSource(renderableDefinition).build();

        @Nullable ModelRenderable result;
        try {
            result = future.get();
        } catch (ExecutionException | InterruptedException ex) {
            throw new AssertionError("Error creating renderable.", ex);
        }

        if (result == null) {
            throw new AssertionError("Error creating renderable.");
        }

        return result;
    }
}
