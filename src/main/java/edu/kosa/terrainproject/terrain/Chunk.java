package edu.kosa.terrainproject.terrain;

import edu.kosa.terrainproject.graphics.Mesh;
import edu.kosa.terrainproject.noise.FbmGenerator;
import edu.kosa.terrainproject.noise.NoiseConfig;
import edu.kosa.terrainproject.noise.NoiseVariant;
import edu.kosa.terrainproject.noise.PerlinNoiseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class Chunk {
    private static final Logger LOGGER = LoggerFactory.getLogger(Chunk.class);
    public static final int SIZE = 16;
    private final byte[][][] blocks;
    private final ChunkPos pos;
    private final FbmGenerator terrainFbm;
    private final FbmGenerator waterFbm;
    private final TerrainConfig config;
    private final World world;
    private Mesh mesh;

    public Chunk(ChunkPos pos, PerlinNoiseGenerator noise, TerrainConfig config, World world) {
        this.pos = pos;
        this.terrainFbm = new FbmGenerator(noise);
        this.waterFbm = new FbmGenerator(new PerlinNoiseGenerator(config.seed + 1, 0.04));
        this.config = config;
        this.world = world;
        this.blocks = new byte[SIZE][config.maxHeight][SIZE]; // Updated to maxHeight
        generateTerrain();
    }

    private void generateTerrain() {
        int waterCount = 0;
        int waterRegionCount = 0;
        int[][] terrainHeights = new int[SIZE][SIZE];
        boolean[][] isWaterRegion = new boolean[SIZE][SIZE];
        double[][] blendFactors = new double[SIZE][SIZE]; // For biome transitions
        NoiseConfig terrainConfig = NoiseConfig.forTerrain(config.seed, config.scale);
        NoiseVariant noiseVariant = NoiseVariant.valueOf(config.noiseType.toUpperCase());

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = pos.getX() * SIZE + x;
                int worldZ = pos.getZ() * SIZE + z;
                terrainHeights[x][z] = world.getTerrainHeight(worldX, worldZ);
                double waterNoiseValue = waterFbm.generate(worldX, worldZ, NoiseConfig.forWater(config.seed + 1), NoiseVariant.STANDARD);
                isWaterRegion[x][z] = waterNoiseValue > 0.6;
                if (isWaterRegion[x][z]) {
                    waterRegionCount++;
                }
                // Calculate blend factor for grass-to-sand transition
                double sandNoiseValue = terrainFbm.generate(worldX, worldZ, terrainConfig, noiseVariant);
                sandNoiseValue = (sandNoiseValue + 1) / 2; // Normalize to [0, 1]
                int baseHeight = (int) Math.floor(sandNoiseValue * config.heightScale + config.baseHeight);
                if (!isWaterRegion[x][z] && baseHeight <= config.sandHeightThreshold + config.biomeBlendRange) {
                    double t = (baseHeight - (config.sandHeightThreshold - config.biomeBlendRange / 2)) / config.biomeBlendRange;
                    blendFactors[x][z] = Math.max(0, Math.min(1, t));
                } else {
                    blendFactors[x][z] = isWaterRegion[x][z] ? 0 : 1; // 0 for water, 1 for full grass
                }
            }
        }
        LOGGER.debug("Chunk at {}: {} water regions identified", pos, waterRegionCount);

        int waterSurfaceHeight = Integer.MAX_VALUE;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                if (!isWaterRegion[x][z]) continue;

                int maxSurroundHeight = terrainHeights[x][z];
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        int nx = x + dx;
                        int nz = z + dz;
                        if (nx >= 0 && nx < SIZE && nz >= 0 && nz < SIZE) {
                            maxSurroundHeight = Math.max(maxSurroundHeight, terrainHeights[nx][nz]);
                        } else {
                            int worldX = pos.getX() * SIZE + nx;
                            int worldZ = pos.getZ() * SIZE + nz;
                            maxSurroundHeight = Math.max(maxSurroundHeight, world.getTerrainHeight(worldX, worldZ));
                        }
                    }
                }
                waterSurfaceHeight = Math.min(waterSurfaceHeight, maxSurroundHeight);
            }
        }

        for (int side = 0; side < 4; side++) {
            int offsetX = side == 0 ? -1 : side == 1 ? 1 : 0;
            int offsetZ = side == 2 ? -1 : side == 3 ? 1 : 0;
            ChunkPos neighborPos = new ChunkPos(pos.getX() + offsetX, pos.getZ() + offsetZ);
            int neighborWaterHeight = world.getWaterSurfaceHeight(neighborPos);
            waterSurfaceHeight = Math.min(waterSurfaceHeight, neighborWaterHeight);
        }

        if (waterSurfaceHeight == Integer.MAX_VALUE) {
            waterSurfaceHeight = config.sandHeightThreshold;
        }
        waterSurfaceHeight = Math.max(1, Math.min(config.maxHeight - 1, waterSurfaceHeight));
        world.setWaterSurfaceHeight(pos, waterSurfaceHeight);

        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int height = terrainHeights[x][z];
                boolean isWater = isWaterRegion[x][z];
                int waterDepth = isWater ? 3 : 0;
                int lakeBedHeight = isWater ? waterSurfaceHeight - waterDepth : -1;

                // Blend height for grass-to-sand transition
                double blendFactor = blendFactors[x][z];
                int blendedHeight = height;
                if (!isWater && blendFactor < 1 && height <= config.sandHeightThreshold + config.biomeBlendRange) {
                    blendedHeight = (int) lerp(config.sandHeightThreshold, height, blendFactor);
                }

                boolean isSandBiome = blendedHeight <= config.sandHeightThreshold && !isWater;
                for (int y = 0; y < config.maxHeight; y++) {
                    if (isWater && y <= waterSurfaceHeight && y > lakeBedHeight) {
                        blocks[x][y][z] = 4; // Water
                        waterCount++;
                    } else if (isWater && y <= lakeBedHeight) {
                        blocks[x][y][z] = 3; // Sand for lake bed
                    } else if (isSandBiome && y <= config.sandHeightThreshold) {
                        blocks[x][y][z] = 3; // Sand
                    } else if (y < blendedHeight && !isWater) {
                        blocks[x][y][z] = 2; // Stone
                    } else if (y == blendedHeight && !isWater) {
                        blocks[x][y][z] = 1; // Grass
                    } else {
                        blocks[x][y][z] = 0; // Air
                    }
                }
            }
        }
        LOGGER.debug("Chunk at {}: {} water blocks placed, water surface height: {}", pos, waterCount, waterSurfaceHeight);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * Math.max(0, Math.min(1, t));
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= config.maxHeight || z < 0 || z >= SIZE) {
            return 0;
        }
        return blocks[x][y][z];
    }

    public ChunkPos getPos() {
        return pos;
    }

    public void generateMesh(World world) {
        List<Float> vertices = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> alphas = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int index = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < config.maxHeight; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte type = blocks[x][y][z];
                    if (type == 0) continue;

                    int wx = pos.getX() * SIZE + x;
                    int wz = pos.getZ() * SIZE + z;

                    if (world.getBlock(wx + 1, y, wz) == 0) {
                        addFace(vertices, texCoords, normals, alphas, indices, index, wx + 1, y, wz, type, 0);
                        index += 4;
                    }
                    if (world.getBlock(wx - 1, y, wz) == 0) {
                        addFace(vertices, texCoords, normals, alphas, indices, index, wx, y, wz, type, 1);
                        index += 4;
                    }
                    if (world.getBlock(wx, y + 1, wz) == 0) {
                        addFace(vertices, texCoords, normals, alphas, indices, index, wx, y + 1, wz, type, 2);
                        index += 4;
                    }
                    if (world.getBlock(wx, y - 1, wz) == 0) {
                        addFace(vertices, texCoords, normals, alphas, indices, index, wx, y, wz, type, 3);
                        index += 4;
                    }
                    if (world.getBlock(wx, y, wz + 1) == 0) {
                        addFace(vertices, texCoords, normals, alphas, indices, index, wx, y, wz + 1, type, 4);
                        index += 4;
                    }
                    if (world.getBlock(wx, y, wz - 1) == 0) {
                        addFace(vertices, texCoords, normals, alphas, indices, index, wx, y, wz, type, 5);
                        index += 4;
                    }
                }
            }
        }

        if (!vertices.isEmpty()) {
            mesh = new Mesh(
                    toFloatArray(vertices),
                    toFloatArray(texCoords),
                    toFloatArray(normals),
                    toFloatArray(alphas),
                    toIntArray(indices)
            );
        } else {
            LOGGER.warn("Empty mesh for chunk at {}", pos);
            mesh = null;
        }
    }

    private void addFace(List<Float> vertices, List<Float> texCoords, List<Float> normals,
                         List<Float> alphas, List<Integer> indices, int index, int x, int y, int z, byte type, int face) {
        float uMin, uMax;
        float alpha = (type == 4) ? 0.5f : 1.0f;
        if (type == 1 && face == 2) { // Grass top
            uMin = 0.0f;  // 0/64
            uMax = 0.25f; // 16/64
        } else if (type == 3) { // Sand
            uMin = 0.5f;  // 32/64
            uMax = 0.75f; // 48/64
        } else if (type == 4) { // Water
            uMin = 0.75f; // 48/64
            uMax = 1.0f;  // 64/64
        } else { // Stone (type 2) or grass sides/bottom
            uMin = 0.25f; // 16/64
            uMax = 0.5f;  // 32/64
        }

        switch (face) {
            case 0:
                vertices.addAll(List.of((float)x, (float)y, (float)z, (float)x, (float)(y+1), (float)z,
                        (float)x, (float)(y+1), (float)(z+1), (float)x, (float)y, (float)(z+1)));
                normals.addAll(List.of(1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f));
                break;
            case 1:
                vertices.addAll(List.of((float)x, (float)y, (float)(z+1), (float)x, (float)(y+1), (float)(z+1),
                        (float)x, (float)(y+1), (float)z, (float)x, (float)y, (float)z));
                normals.addAll(List.of(-1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f));
                break;
            case 2:
                vertices.addAll(List.of((float)x, (float)y, (float)(z+1), (float)(x+1), (float)y, (float)(z+1),
                        (float)(x+1), (float)y, (float)z, (float)x, (float)y, (float)z));
                normals.addAll(List.of(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f));
                break;
            case 3:
                vertices.addAll(List.of((float)x, (float)y, (float)z, (float)(x+1), (float)y, (float)z,
                        (float)(x+1), (float)y, (float)(z+1), (float)x, (float)y, (float)(z+1)));
                normals.addAll(List.of(0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f));
                break;
            case 4:
                vertices.addAll(List.of((float)x, (float)y, (float)z, (float)(x+1), (float)y, (float)z,
                        (float)(x+1), (float)(y+1), (float)z, (float)x, (float)(y+1), (float)z));
                normals.addAll(List.of(0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f));
                break;
            case 5:
                vertices.addAll(List.of((float)(x+1), (float)y, (float)z, (float)x, (float)y, (float)z,
                        (float)x, (float)(y+1), (float)z, (float)(x+1), (float)(y+1), (float)z));
                normals.addAll(List.of(0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f));
                break;
        }

        texCoords.addAll(List.of(uMin, 0f, uMin, 1f, uMax, 1f, uMax, 0f));
        alphas.addAll(List.of(alpha, alpha, alpha, alpha));
        indices.addAll(List.of(index, index + 1, index + 2, index, index + 2, index + 3));
    }

    private float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }

    private int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void cleanup() {
        if (mesh != null) {
            mesh.cleanup();
            mesh = null;
        }
    }
}