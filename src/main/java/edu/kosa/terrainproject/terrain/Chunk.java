package edu.kosa.terrainproject.terrain;

import edu.kosa.terrainproject.graphics.Mesh;
import edu.kosa.terrainproject.noise.PerlinNoise;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public static final int SIZE = 16;
    private final byte[][][] blocks;
    private final ChunkPos pos;
    private final PerlinNoise noise;
    private final TerrainConfig config;
    private Mesh mesh;

    public Chunk(ChunkPos pos, PerlinNoise noise, TerrainConfig config) {
        this.pos = pos;
        this.noise = noise;
        this.config = config;
        this.blocks = new byte[SIZE][SIZE][SIZE];
        generateTerrain();
    }

    private void generateTerrain() {
        int waterCount = 0; // Debug: count water blocks
        // First pass: compute terrain heights and find average water height
        int[][] terrainHeights = new int[SIZE][SIZE];
        double totalWaterHeight = 0;
        int waterPoints = 0;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = pos.getX() * SIZE + x;
                int worldZ = pos.getZ() * SIZE + z;
                double noiseValue = switch (config.noiseType) {
                    case "Ridged" ->
                            noise.ridgedFbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                    case "Billowy" ->
                            noise.billowyFbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                    case "Hybrid" ->
                            noise.hybridFbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                    default -> noise.fbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                };
                int height = (int) Math.floor(noiseValue * config.heightScale + config.baseHeight);
                height = Math.max(0, Math.min(SIZE - 1, height));
                terrainHeights[x][z] = height;

                double waterNoiseValue = noise.waterFbm(worldX, worldZ);
                if (waterNoiseValue > 0.7) {
                    totalWaterHeight += height;
                    waterPoints++;
                }
            }
        }

        // Calculate water surface height (average of terrain heights in water regions)
        int waterSurfaceHeight = waterPoints > 0 ? (int) Math.round(totalWaterHeight / waterPoints) : 6; // Fallback to sand threshold
        waterSurfaceHeight = Math.max(1, Math.min(SIZE - 1, waterSurfaceHeight)); // Ensure valid height

        // Second pass: place blocks
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = pos.getX() * SIZE + x;
                int worldZ = pos.getZ() * SIZE + z;
                int height = terrainHeights[x][z];

                double waterNoiseValue = noise.waterFbm(worldX, worldZ);
                boolean isWater = waterNoiseValue > 0.7;
                int waterDepth = isWater ? 3 : 0; // Fixed depth of 3 blocks for lakes
                int lakeBedHeight = isWater ? waterSurfaceHeight - waterDepth : -1;

                boolean isSandBiome = height <= 6 && !isWater;
                for (int y = 0; y < SIZE; y++) {
                    if (isWater && y <= waterSurfaceHeight && y > lakeBedHeight) {
                        blocks[x][y][z] = 4; // Water
                        waterCount++;
                    } else if (isWater && y <= lakeBedHeight && y >= 0) {
                        blocks[x][y][z] = 3; // Sand for lake bed
                    } else if (isSandBiome && y <= config.sandHeightThreshold) {
                        blocks[x][y][z] = 3; // Sand
                    } else if (y < height) {
                        blocks[x][y][z] = 2; // Stone
                    } else if (y == height && !isWater) {
                        blocks[x][y][z] = 1; // Grass
                    } else {
                        blocks[x][y][z] = 0; // Air
                    }
                }
            }
        }
        System.out.println("Chunk at " + pos + ": " + waterCount + " water blocks placed, water surface height: " + waterSurfaceHeight);
    }

    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
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
            for (int y = 0; y < SIZE; y++) {
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
            System.err.println("Warning: Empty mesh for chunk at " + pos);
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