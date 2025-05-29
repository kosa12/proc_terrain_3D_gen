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
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = pos.getX() * SIZE + x;
                int worldZ = pos.getZ() * SIZE + z;
                double noiseValue;
                switch (config.noiseType) {
                    case "Ridged":
                        noiseValue = noise.ridgedFbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                        break;
                    case "Billowy":
                        noiseValue = noise.billowyFbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                        break;
                    case "Hybrid":
                        noiseValue = noise.hybridFbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                        break;
                    default:
                        noiseValue = noise.fbm(worldX, worldZ, config.octaves, config.persistence, config.lacunarity);
                        break;
                }
                int height = (int) Math.floor(noiseValue * config.heightScale + config.baseHeight);
                height = Math.max(0, Math.min(SIZE - 1, height));

                for (int y = 0; y < SIZE; y++) {
                    if (y < height) {
                        blocks[x][y][z] = 2; // Stone
                    } else if (y == height) {
                        blocks[x][y][z] = 1; // Grass
                    } else {
                        blocks[x][y][z] = 0; // Air
                    }
                }
            }
        }
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
        List<Integer> indices = new ArrayList<>();
        int index = 0;

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte type = blocks[x][y][z];
                    if (type == 0) continue;

                    int wx = pos.getX() * SIZE + x;
                    int wy = y;
                    int wz = pos.getZ() * SIZE + z;

                    if (world.getBlock(wx + 1, wy, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx + 1, wy, wz, type, 0);
                        index += 4;
                    }
                    if (world.getBlock(wx - 1, wy, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz, type, 1);
                        index += 4;
                    }
                    if (world.getBlock(wx, wy + 1, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy + 1, wz, type, 2);
                        index += 4;
                    }
                    if (world.getBlock(wx, wy - 1, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz, type, 3);
                        index += 4;
                    }
                    if (world.getBlock(wx, wy, wz + 1) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz + 1, type, 4);
                        index += 4;
                    }
                    if (world.getBlock(wx, wy, wz - 1) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz, type, 5);
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
                    toIntArray(indices)
            );
        } else {
            System.err.println("Warning: Empty mesh for chunk at " + pos);
            mesh = null;
        }
    }

    private void addFace(List<Float> vertices, List<Float> texCoords, List<Float> normals,
                         List<Integer> indices, int index, int x, int y, int z, byte type, int face) {
        float uMin = (type == 1 && face == 2) ? 0f : 0.5f;
        float uMax = (type == 1 && face == 2) ? 0.5f : 1f;

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
}