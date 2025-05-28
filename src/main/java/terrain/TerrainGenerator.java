package terrain;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {
    public static void main(String[] args) {
        // Initialize window and OpenGL
        WindowManager windowManager = new WindowManager(1200, 800, "CircleScape");

        // Initialize components
        World world = new World(12345L);
        Camera camera = new Camera(800f / 600f);
        TextureLoader textureLoader = new TextureLoader();
        int textureID = textureLoader.loadTexture("src/main/resources/textures/atlas.png");
        ShaderProgram shaderProgram = new ShaderProgram();
        Renderer renderer = new Renderer(shaderProgram, textureID);
        InputHandler inputHandler = new InputHandler(windowManager.getWindow(), camera);

        // Load initial chunks
        List<Chunk> loadedChunks = new ArrayList<>();
        updateChunks(world, camera, loadedChunks);

        // Main loop
        double lastTime = windowManager.getTime();
        while (!windowManager.shouldClose()) {
            double currentTime = windowManager.getTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            inputHandler.processInput(deltaTime);
            updateChunks(world, camera, loadedChunks);

            renderer.render(camera, loadedChunks, inputHandler.getRadius());

            windowManager.update();
        }

        // Cleanup
        for (Chunk chunk : loadedChunks) {
            if (chunk.getMesh() != null) {
                chunk.getMesh().cleanup();
            }
        }
        textureLoader.cleanup();
        shaderProgram.cleanup();
        windowManager.cleanup();
    }

    private static void updateChunks(World world, Camera camera, List<Chunk> loadedChunks) {
        loadedChunks.clear();
        int renderDistance = 6; // Increased render distance
        int chunkX = (int) Math.floor(camera.getPosition().x / Chunk.SIZE);
        int chunkZ = (int) Math.floor(camera.getPosition().z / Chunk.SIZE);

        for (int x = chunkX - renderDistance; x <= chunkX + renderDistance; x++) {
            for (int z = chunkZ - renderDistance; z <= chunkZ + renderDistance; z++) {
                Chunk chunk = world.getChunk(x, z);
                if (chunk.getMesh() == null) {
                    chunk.generateMesh(world);
                }
                if (chunk.getMesh() == null) {
                    System.err.println("Warning: Null mesh for chunk at (" + x + ", " + z + ")");
                }
                loadedChunks.add(chunk);
            }
        }
    }
}