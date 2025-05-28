package terrain;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImString;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public static void main(String[] args) {
        WindowManager windowManager = new WindowManager(1200, 800, "CircleScape");

        initImGui(windowManager.getWindow());

        TerrainConfig config = new TerrainConfig();
        World world = new World(config);
        Camera camera = new Camera(800f / 600f);
        TextureLoader textureLoader = new TextureLoader();
        int textureID = textureLoader.loadTexture("src/main/resources/textures/atlas.png");
        ShaderProgram shaderProgram = new ShaderProgram();
        Renderer renderer = new Renderer(shaderProgram, textureID);
        InputHandler inputHandler = new InputHandler(windowManager.getWindow(), camera);

        List<Chunk> loadedChunks = new ArrayList<>();
        updateChunks(world, camera, loadedChunks);

        double lastTime = windowManager.getTime();
        ImString seedInput = new ImString(String.valueOf(config.seed), 64);

        while (!windowManager.shouldClose()) {
            double currentTime = windowManager.getTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            imGuiGlfw.newFrame();
            ImGui.newFrame();

            float[] scale = new float[]{config.scale};
            int[] octaves = new int[]{config.octaves};
            float[] persistence = new float[]{config.persistence};
            float[] lacunarity = new float[]{config.lacunarity};
            float[] heightScale = new float[]{config.heightScale};
            float[] baseHeight = new float[]{config.baseHeight};

            ImGui.begin("Terrain Settings");
            if (ImGui.sliderFloat("Scale", scale, 0.01f, 0.2f)) {
                config.scale = scale[0];
            }
            if (ImGui.sliderInt("Octaves", octaves, 1, 10)) {
                config.octaves = octaves[0];
            }
            if (ImGui.sliderFloat("Persistence", persistence, 0.1f, 1.0f)) {
                config.persistence = persistence[0];
            }
            if (ImGui.sliderFloat("Lacunarity", lacunarity, 1.0f, 4.0f)) {
                config.lacunarity = lacunarity[0];
            }
            if (ImGui.sliderFloat("Height Scale", heightScale, 1.0f, 16.0f)) {
                config.heightScale = heightScale[0];
            }
            if (ImGui.sliderFloat("Base Height", baseHeight, 0.0f, 8.0f)) {
                config.baseHeight = baseHeight[0];
            }
            if (ImGui.inputText("Seed", seedInput)) {
                try {
                    config.seed = Long.parseLong(seedInput.get().trim());
                } catch (NumberFormatException e) {
                    config.seed = 67890L;
                }
            }
            if (ImGui.button("Generate World")) {
                world.regenerate(config);
                loadedChunks.clear();
                updateChunks(world, camera, loadedChunks);
            }
            ImGui.end();

            inputHandler.processInput(deltaTime);
            updateChunks(world, camera, loadedChunks);

            GL20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            renderer.render(camera, loadedChunks, inputHandler.getRadius());

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            windowManager.update();
        }

        cleanupImGui();
        for (Chunk chunk : loadedChunks) {
            if (chunk.getMesh() != null) {
                chunk.getMesh().cleanup();
            }
        }
        textureLoader.cleanup();
        shaderProgram.cleanup();
        windowManager.cleanup();
    }

    private static void initImGui(long window) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 330 core");
    }

    private static void cleanupImGui() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    private static void updateChunks(World world, Camera camera, List<Chunk> loadedChunks) {
        loadedChunks.clear();
        int renderDistance = 6;
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