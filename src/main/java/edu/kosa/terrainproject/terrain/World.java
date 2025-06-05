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
    private FbmGenerator regionFbm;
    private TerrainConfig config;
    private final Map<ChunkPos, Integer> waterSurfaceHeights;

    public World(TerrainConfig config) {
        this.chunks = new HashMap<>();
        this.waterSurfaceHeights = new HashMap<>();
        this.config = config;
        this.terrainFbm = new FbmGenerator(new PerlinNoiseGenerator(config.seed, config.scale));
        this.regionFbm = new FbmGenerator(new PerlinNoiseGenerator(config.seed + 2, config.regionScale));
    }

    public void regenerate(TerrainConfig newConfig) {
        chunks.clear();
        waterSurfaceHeights.clear();
        this.config = newConfig;
        this.terrainFbm = new FbmGenerator(new PerlinNoiseGenerator(newConfig.seed, newConfig.scale));
        this.regionFbm = new FbmGenerator(new PerlinNoiseGenerator(newConfig.seed + 2, newConfig.regionScale));
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

    private double lerp(double a, double b, double t) {
        return a + (b - a) * Math.max(0, Math.min(1, t));
    }

    private double grassSmoothstep(double t) {
        t = Math.max(0, Math.min(1, t));
        double smooth = t * t * (3 - 2 * t);
        return smooth * smooth;
    }

    public int getTerrainHeight(int worldX, int worldZ) {
        NoiseConfig noiseConfig = new NoiseConfig(config.seed, config.scale, config.octaves, config.persistence, config.lacunarity);
        NoiseVariant noiseVariant = NoiseVariant.valueOf(config.noiseType.toUpperCase());
        double noiseValue = terrainFbm.generate(worldX, worldZ, noiseConfig, noiseVariant);
        noiseValue = (noiseValue + 1) / 2; // Normalize to [0, 1]

        // Region-based height modification for grass biome
        NoiseConfig regionConfig = new NoiseConfig(config.seed + 2, config.regionScale, 3, 0.5, 2.0);
        double regionValue = regionFbm.generate(worldX, worldZ, regionConfig, NoiseVariant.STANDARD);
        double effectiveHeightScale = config.heightScale;

        // Only modify height for grass biome (above sandHeightThreshold + biomeBlendRange and not water)
        int baseHeight = (int) Math.floor(noiseValue * config.heightScale + config.baseHeight);
        boolean isWater = regionFbm.generate(worldX, worldZ, NoiseConfig.forWater(config.seed + 1), NoiseVariant.STANDARD) > 0.6;
        if (!isWater && baseHeight > config.sandHeightThreshold + config.biomeBlendRange) {
            // Smooth transition between flat and mountainous areas lol
            double transitionStart = config.flatThreshold - config.transitionRange / 2;
            double transitionEnd = config.flatThreshold + config.transitionRange / 2;
            if (regionValue < transitionStart) {
                effectiveHeightScale = config.flatHeightScale;
            } else if (regionValue > transitionEnd) {
                effectiveHeightScale = config.heightScale * config.mountainAmplifier;
            } else {
                // Custom grassSmoothstep for ultra-smooth grass-to-grass transitions
                double t = (regionValue - transitionStart) / config.transitionRange;
                t = grassSmoothstep(t);
                effectiveHeightScale = lerp(config.flatHeightScale, config.heightScale * config.mountainAmplifier, t);
            }
        }

        int height = (int) Math.floor(noiseValue * effectiveHeightScale + config.baseHeight);
        return Math.max(0, Math.min(config.maxHeight - 1, height));
    }

    public int getWaterSurfaceHeight(ChunkPos pos) {
        return waterSurfaceHeights.getOrDefault(pos, config.sandHeightThreshold);
    }


    public void setWaterSurfaceHeight(ChunkPos pos, int height) {
        waterSurfaceHeights.put(pos, height);
    }
}