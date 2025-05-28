package terrain;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position;
    private final float pitch = -60f; // Fixed 45-degree downward angle
    private float yaw; // Rotation around Y-axis
    private final Matrix4f projectionMatrix;
    private final float sensitivity = 0.1f;
    private final float moveSpeed = 1.0f;

    public Camera(float aspectRatio) {
        position = new Vector3f(8, 90, 8); // Elevated starting position
        yaw = 45f; // Diagonal view
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(45), aspectRatio, 0.1f, 1000f);
    }

    public void rotate(float deltaX) {
        yaw += deltaX * sensitivity;
    }

    public void move(float forwardAmount, float rightAmount) {
        Vector3f forward = new Vector3f(
                (float) Math.cos(Math.toRadians(yaw)),
                0,
                (float) Math.sin(Math.toRadians(yaw))
        ).normalize();

        Vector3f right = new Vector3f(
                (float) -Math.sin(Math.toRadians(yaw)),
                0,
                (float) Math.cos(Math.toRadians(yaw))
        ).normalize();

        Vector3f movement = new Vector3f();
        movement.add(forward.mul(forwardAmount * moveSpeed));
        movement.add(right.mul(rightAmount * moveSpeed));

        position.add(movement);
    }

    public Matrix4f getViewMatrix() {
        Vector3f forward = new Vector3f(
                (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))),
                (float) Math.sin(Math.toRadians(pitch)),
                (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)))
        ).normalize();

        return new Matrix4f().lookAt(position, position.add(forward, new Vector3f()), new Vector3f(0, 1, 0));
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }
}