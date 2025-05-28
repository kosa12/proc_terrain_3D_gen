package terrain;

public class Chunk {
    public static final int SIZE = 16;
    private final byte[][][] blocks; // 3D array: x, y, z (0=air, 1=grass, 2=stone)
    private final ChunkPos pos;
    private final PerlinNoise noise;

    public Chunk(ChunkPos pos, PerlinNoise noise) {
        this.pos = pos;
        this.noise = noise;
        this.blocks = new byte[SIZE][SIZE][SIZE];
        generateTerrain();
    }

    private void generateTerrain() {
        // Generate heightmap using noise
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                // World coordinates for noise input
                double worldX = (pos.getX() * SIZE + x) * 0.1;
                double worldZ = (pos.getZ() * SIZE + z) * 0.1;
                // Get height from noise (scaled to 0-15)
                double height = noise.fbm(worldX, worldZ, 4, 0.5, 2.0) * SIZE;
                int yHeight = (int) Math.floor(height);

                // Fill blocks based on height
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
}
