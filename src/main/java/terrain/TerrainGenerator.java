package terrain;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class TerrainGenerator {
    public static void main(String[] args) {
        PerlinNoise noise = new PerlinNoise(67, 0.05); // Seed = 12345, scale = 0.05
        System.out.println("Testing Perlin Noise (10x10 grid):");
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                double value = noise.fbm(x * 0.1, y * 0.1, 4, 0.5, 2.0);
                System.out.printf("%.3f ", value);
            }
            System.out.println();
        }

        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Create a window
        long window = GLFW.glfwCreateWindow(800, 600, "CircleScape", 0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            throw new IllegalStateException("Failed to create GLFW window");
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Main loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            // Clear the screen
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Swap buffers and poll events
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        // Clean up
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}