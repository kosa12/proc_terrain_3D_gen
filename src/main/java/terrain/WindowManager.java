package terrain;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class WindowManager {
    private final long window;

    public WindowManager(int width, int height, String title) {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            throw new IllegalStateException("Failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0.1f, 0.1f, 0.3f, 1.0f); // Blue background
    }

    public long getWindow() {
        return window;
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public void update() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    public double getTime() {
        return GLFW.glfwGetTime();
    }

    public void cleanup() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}