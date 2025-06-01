package edu.kosa.terrainproject.app;

import edu.kosa.terrainproject.graphics.*;
import edu.kosa.terrainproject.input.InputHandler;
import edu.kosa.terrainproject.terrain.Chunk;
import edu.kosa.terrainproject.terrain.TerrainConfig;
import edu.kosa.terrainproject.terrain.World;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {
    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final int RENDER_DISTANCE = 6;
    private static final Map<String, Chunk> loadedChunksMap = new HashMap<>();

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

        updateChunks(world, camera);

        double lastTime = windowManager.getTime();
        ImString seedInput = new ImString(String.valueOf(config.seed), 64);
        String[] noiseTypes = {"Standard", "Ridged", "Billowy", "Hybrid"};
        ImInt currentNoiseType = new ImInt(0); // Selected noise type index
        final int fpsSampleSize = 30;
        float[] fpsSamples = new float[fpsSampleSize];
        int fpsIndex = 0;
        boolean fpsBufferFilled = false;


        while (!windowManager.shouldClose()) {
            double currentTime = windowManager.getTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            imGuiGlfw.newFrame();
            ImGui.newFrame();

            // --- FPS Counter Start ---
            fpsSamples[fpsIndex] = 1.0f / deltaTime;
            fpsIndex = (fpsIndex + 1) % fpsSampleSize;
            if (fpsIndex == 0) fpsBufferFilled = true;

            int count = fpsBufferFilled ? fpsSampleSize : fpsIndex;
            float fpsSum = 0.0f;
            for (int i = 0; i < count; i++) {
                fpsSum += fpsSamples[i];
            }
            float avgFps = fpsSum / count;


            ImGui.setNextWindowPos(1100, 10, ImGuiCond.Always);
            ImGui.setNextWindowSize(220, 60);
            ImGui.begin("FPS Counter", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse);
            ImGui.text(String.format("FPS: %.1f", avgFps));
            ImGui.end();
            // --- FPS Counter End ---




            float[] scale = new float[]{config.scale};
            int[] octaves = new int[]{config.octaves};
            float[] persistence = new float[]{config.persistence};
            float[] lacunarity = new float[]{config.lacunarity};
            float[] heightScale = new float[]{config.heightScale};
            float[] baseHeight = new float[]{config.baseHeight};
            int[] sandHeightThreshold = new int[]{config.sandHeightThreshold};

            ImGui.begin("Terrain Settings");
            ImGui.setWindowSize(300, 290);
            ImGui.setWindowPos(10, 10);
            ImGui.text("Press Enter to toggle cursor for GUI interaction");
            if (ImGui.combo("Noise Type", currentNoiseType, noiseTypes)) {
                config.noiseType = noiseTypes[currentNoiseType.get()];
            }
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
            if (ImGui.sliderInt("Sand Height Threshold", sandHeightThreshold, 1, 8)) {
                config.sandHeightThreshold = sandHeightThreshold[0];
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
            }
            ImGui.end();

            inputHandler.processInput(deltaTime);
            updateChunks(world, camera);

            GL20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            renderer.render(camera, new ArrayList<>(loadedChunksMap.values()), inputHandler.getRadius());


            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            windowManager.update();
        }

        cleanupImGui();
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

    private static void updateChunks(World world, Camera camera) {
        int chunkX = (int) Math.floor(camera.getPosition().x / Chunk.SIZE);
        int chunkZ = (int) Math.floor(camera.getPosition().z / Chunk.SIZE);

        Set<String> neededChunks = new HashSet<>();

        for (int x = chunkX - RENDER_DISTANCE; x <= chunkX + RENDER_DISTANCE; x++) {
            for (int z = chunkZ - RENDER_DISTANCE; z <= chunkZ + RENDER_DISTANCE; z++) {
                String key = x + "," + z;
                neededChunks.add(key);
                if (!loadedChunksMap.containsKey(key)) {
                    Chunk chunk = world.getChunk(x, z);
                    if (chunk.getMesh() == null) {
                        chunk.generateMesh(world);
                    }
                    loadedChunksMap.put(key, chunk);
                }
            }
        }

        // Remove chunks out of range
        loadedChunksMap.keySet().removeIf(key -> {
            if(!neededChunks.contains(key)) {
                loadedChunksMap.get(key).cleanup();
                return true;
            }
            return false;
        });
    }
}