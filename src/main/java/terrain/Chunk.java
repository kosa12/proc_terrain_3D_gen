package terrain;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public static final int SIZE = 16;
    private final byte[][][] blocks; // 3D array: x, y, z (0=air, 1=grass, 2=stone)
    private final ChunkPos pos;
    private final PerlinNoise noise;
    private Mesh mesh;

    public Chunk(ChunkPos pos, PerlinNoise noise) {
        this.pos = pos;
        this.noise = noise;
        this.blocks = new byte[SIZE][SIZE][SIZE];
        generateTerrain();
    }

    private void generateTerrain() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                double worldX = (pos.getX() * SIZE + x) * 0.1;
                double worldZ = (pos.getZ() * SIZE + z) * 0.1;
                double height = noise.fbm(worldX, worldZ, 4, 0.5, 2.0) * SIZE;
                int yHeight = (int) Math.floor(height);

                for (int y = 0; y < SIZE; y++) {
                    if (y < yHeight) {
                        blocks[x][y][z] = 2; // Stone
                    } else if (y == yHeight) {
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
            return 0; // Air for out-of-bounds
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
                    if (type == 0) continue; // Skip air

                    int wx = pos.getX() * SIZE + x;
                    int wy = y;
                    int wz = pos.getZ() * SIZE + z;

                    // Check all six faces
                    // +X face
                    if (world.getBlock(wx + 1, wy, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx + 1, wy, wz, type, 0);
                        index += 4;
                    }
                    // -X face
                    if (world.getBlock(wx - 1, wy, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz, type, 1);
                        index += 4;
                    }
                    // +Y face (top)
                    if (world.getBlock(wx, wy + 1, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy + 1, wz, type, 2);
                        index += 4;
                    }
                    // -Y face
                    if (world.getBlock(wx, wy - 1, wz) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz, type, 3);
                        index += 4;
                    }
                    // +Z face
                    if (world.getBlock(wx, wy, wz + 1) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz + 1, type, 4);
                        index += 4;
                    }
                    // -Z face
                    if (world.getBlock(wx, wy, wz - 1) == 0) {
                        addFace(vertices, texCoords, normals, indices, index, wx, wy, wz, type, 5);
                        index += 4;
                    }
                }
            }
        }

        mesh = new Mesh(
                toFloatArray(vertices),
                toFloatArray(texCoords),
                toFloatArray(normals),
                toIntArray(indices)
        );
    }

    private void addFace(List<Float> vertices, List<Float> texCoords, List<Float> normals,
                         List<Integer> indices, int index, int x, int y, int z, byte type, int face) {
        float uMin = (type == 1 && face == 2) ? 0f : 0.5f; // Green for grass top, gray otherwise
        float uMax = (type == 1 && face == 2) ? 0.5f : 1f;

        // Define vertices and normals based on face direction
        switch (face) {
            case 0: // +X
                vertices.addAll(List.of((float)x, (float)y, (float)z, (float)x, (float)(y+1), (float)z,
                        (float)x, (float)(y+1), (float)(z+1), (float)x, (float)y, (float)(z+1)));
                normals.addAll(List.of(1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f));
                break;
            case 1: // -X
                vertices.addAll(List.of((float)x, (float)y, (float)(z+1), (float)x, (float)(y+1), (float)(z+1),
                        (float)x, (float)(y+1), (float)z, (float)x, (float)y, (float)z));
                normals.addAll(List.of(-1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f));
                break;
            case 2: // +Y
                vertices.addAll(List.of((float)x, (float)y, (float)(z+1), (float)(x+1), (float)y, (float)(z+1),
                        (float)(x+1), (float)y, (float)z, (float)x, (float)y, (float)z));
                normals.addAll(List.of(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f));
                break;
            case 3: // -Y
                vertices.addAll(List.of((float)x, (float)y, (float)z, (float)(x+1), (float)y, (float)z,
                        (float)(x+1), (float)y, (float)(z+1), (float)x, (float)y, (float)(z+1)));
                normals.addAll(List.of(0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f));
                break;
            case 4: // +Z
                vertices.addAll(List.of((float)x, (float)y, (float)z, (float)(x+1), (float)y, (float)z,
                        (float)(x+1), (float)(y+1), (float)z, (float)x, (float)(y+1), (float)z));
                normals.addAll(List.of(0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f));
                break;
            case 5: // -Z
                vertices.addAll(List.of((float)(x+1), (float)y, (float)z, (float)x, (float)y, (float)z,
                        (float)x, (float)(y+1), (float)z, (float)(x+1), (float)(y+1), (float)z));
                normals.addAll(List.of(0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f));
                break;
        }

        // Texture coordinates
        texCoords.addAll(List.of(uMin, 0f, uMin, 1f, uMax, 1f, uMax, 0f));

        // Indices for two triangles
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