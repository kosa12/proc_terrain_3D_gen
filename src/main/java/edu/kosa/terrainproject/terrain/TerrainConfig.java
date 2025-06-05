package edu.kosa.terrainproject.terrain;

public class TerrainConfig {
    public float scale = 0.055f; // Perlin noise scale
    public int octaves = 3; // FBM octaves
    public float persistence = 0.58f; // FBM persistence
    public float lacunarity = 1.3f; // FBM lacunarity
    public float heightScale = 8.5f; // Height multiplier
    public float baseHeight = 1.6f; // Base height
    public int sandHeightThreshold = 5; // Max y for sand blocks
    public long seed;// World seed
    public String noiseType = "Standard"; // Standard, Ridged, Billowy, Hybrid
    public final double regionScale = 0.015; // Low frequency for large regions
    public final double flatThreshold = 0.3; // Noise value below which terrain is flat (0–1)
    public final double flatHeightScale = 1.5; // Reduced height scale for flat areas
    public final double mountainAmplifier = 2.5; // Height multiplier for mountain
    public final double transitionRange = 0.6; // Noise range for smooth transition (0–1)
    public final int maxHeight = 128; // New max height for terrain (increased from 15)
    public final double biomeBlendRange = 0.3; // Noise range for biome transitions (grass to sand/water)

    public TerrainConfig(long initialSeed) {
        this.seed = initialSeed;
    }
}