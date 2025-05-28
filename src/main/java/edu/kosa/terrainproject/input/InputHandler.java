package edu.kosa.terrainproject.input;

import edu.kosa.terrainproject.graphics.Camera;
import org.lwjgl.glfw.GLFW;

public class InputHandler {
    private final long window;
    private final Camera camera;
    private double lastX;
    private boolean firstMouse = true;
    private final float visibilityRadius = 100f; // Fixed visibility radius
    private boolean cursorVisible = false;

    public InputHandler(long window, Camera camera) {
        this.window = window;
        this.camera = camera;

        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (!cursorVisible) { // Only rotate camera if cursor is hidden
                if (firstMouse) {
                    lastX = xpos;
                    firstMouse = false;
                }
                float deltaX = (float) (xpos - lastX);
                lastX = xpos;
                camera.rotate(deltaX);
            }
        });


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

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS) {
            try {
                Thread.sleep(200); // Debounce to prevent rapid toggling
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            cursorVisible = !cursorVisible;
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                    cursorVisible ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED);
            firstMouse = true; // Reset mouse position
            System.out.println("Cursor " + (cursorVisible ? "visible" : "hidden"));
        }

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