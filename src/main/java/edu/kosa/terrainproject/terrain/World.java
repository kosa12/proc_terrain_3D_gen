package edu.kosa.terrainproject.terrain;

import edu.kosa.terrainproject.noise.PerlinNoise;

import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<ChunkPos, Chunk> chunks;
    private PerlinNoise noise;
    private TerrainConfig config;
    private final Map<ChunkPos, Integer> waterSurfaceHeights; // Store water surface heights

    public World(TerrainConfig config) {
        this.chunks = new HashMap<>();
        this.waterSurfaceHeights = new HashMap<>();
        this.config = config;
        this.noise = new PerlinNoise(config.seed, config.scale);
    }

    public void regenerate(TerrainConfig newConfig) {
        chunks.clear();
        waterSurfaceHeights.clear();
        this.config = newConfig;
        this.noise = new PerlinNoise(newConfig.seed, newConfig.scale);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        return chunks.computeIfAbsent(pos, p -> new Chunk(p, noise, config, this)); // Pass World instance
    }

    public byte getBlock(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE);
        int localX = Math.floorMod(x, Chunk.SIZE);
        int localZ = Math.floorMod(z, Chunk.SIZE);
        Chunk chunk = getChunk(chunkX, chunkZ);
        return chunk.getBlock(localX, y, localZ);
    }

    // Compute terrain height without generating chunks
    public int getTerrainHeight(int worldX, int worldZ) {
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
        return Math.max(0, Math.min(Chunk.SIZE - 1, height));
    }

    // Get or set water surface height for a chunk
    public int getWaterSurfaceHeight(ChunkPos pos) {
        return waterSurfaceHeights.getOrDefault(pos, (int) config.baseHeight + 2); // Default above base height
    }

    public void setWaterSurfaceHeight(ChunkPos pos, int height) {
        waterSurfaceHeights.put(pos, height);
    }
}