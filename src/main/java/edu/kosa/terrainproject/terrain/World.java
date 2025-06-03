package edu.kosa.terrainproject.terrain;

import edu.kosa.terrainproject.noise.FbmGenerator;
import edu.kosa.terrainproject.noise.NoiseConfig;
import edu.kosa.terrainproject.noise.NoiseVariant;
import edu.kosa.terrainproject.noise.PerlinNoiseGenerator;

import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<ChunkPos, Chunk> chunks;
    private FbmGenerator terrainFbm;
    private TerrainConfig config;
    private final Map<ChunkPos, Integer> waterSurfaceHeights;

    public World(TerrainConfig config) {
        this.chunks = new HashMap<>();
        this.waterSurfaceHeights = new HashMap<>();
        this.config = config;
        this.terrainFbm = new FbmGenerator(new PerlinNoiseGenerator(config.seed, config.scale));
    }

    public void regenerate(TerrainConfig newConfig) {
        chunks.clear();
        waterSurfaceHeights.clear();
        this.config = newConfig;
        this.terrainFbm = new FbmGenerator(new PerlinNoiseGenerator(newConfig.seed, newConfig.scale));
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        return chunks.computeIfAbsent(pos, p -> new Chunk(p, new PerlinNoiseGenerator(config.seed, config.scale), config, this));
    }

    public byte getBlock(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE);
        int localX = Math.floorMod(x, Chunk.SIZE);
        int localZ = Math.floorMod(z, Chunk.SIZE);
        Chunk chunk = getChunk(chunkX, chunkZ);
        return chunk.getBlock(localX, y, localZ);
    }

    public int getTerrainHeight(int worldX, int worldZ) {
        NoiseConfig noiseConfig = new NoiseConfig(config.seed, config.scale, config.octaves, config.persistence, config.lacunarity);
        NoiseVariant noiseVariant = NoiseVariant.valueOf(config.noiseType.toUpperCase());
        double noiseValue = terrainFbm.generate(worldX, worldZ, noiseConfig, noiseVariant);
        int height = (int) Math.floor(noiseValue * config.heightScale + config.baseHeight);
        return Math.max(0, Math.min(Chunk.SIZE - 1, height));
    }

    public int getWaterSurfaceHeight(ChunkPos pos) {
        return waterSurfaceHeights.getOrDefault(pos, config.sandHeightThreshold);
    }

    public void setWaterSurfaceHeight(ChunkPos pos, int height) {
        waterSurfaceHeights.put(pos, height);
    }
}