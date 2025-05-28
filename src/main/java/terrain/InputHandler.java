package terrain;

import org.lwjgl.glfw.GLFW;

public class InputHandler {
    private final long window;
    private final Camera camera;
    private double lastX;
    private boolean firstMouse = true;
    private final float visibilityRadius = 100f; // Fixed visibility radius

    public InputHandler(long window, Camera camera) {
        this.window = window;
        this.camera = camera;

        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (firstMouse) {
                lastX = xpos;
                firstMouse = false;
            }
            float deltaX = (float) (xpos - lastX);
            lastX = xpos;
            camera.rotate(deltaX);
        });

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void processInput(float deltaTime) {
        float speed = 50f * deltaTime;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            camera.move(0f, speed); // Move right (lateral)
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            camera.move(0f, -speed); // Move left (lateral)
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            camera.move(-speed, 0f); // Move backward
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            camera.move(speed, 0f); // Move forward
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }
    }

    public float getRadius() {
        return visibilityRadius;
    }
}