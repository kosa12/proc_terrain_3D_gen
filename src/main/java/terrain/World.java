package terrain;

import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<ChunkPos, Chunk> chunks;
    private final PerlinNoise noise;

    public World(long seed) {
        this.chunks = new HashMap<>();
        this.noise = new PerlinNoise(seed, 0.05);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        return chunks.computeIfAbsent(pos, p -> new Chunk(p, noise));
    }

    public byte getBlock(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE);
        int localX = Math.floorMod(x, Chunk.SIZE);
        int localZ = Math.floorMod(z, Chunk.SIZE);
        Chunk chunk = getChunk(chunkX, chunkZ);
        return chunk.getBlock(localX, y, localZ);
    }
}