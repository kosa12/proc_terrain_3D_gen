package edu.kosa.terrainproject.terrain;

public class TerrainConfig {
    public float scale = 0.078f; // Perlin noise scale
    public int octaves = 5; // FBM octaves
    public float persistence = 0.5f; // FBM persistence
    public float lacunarity = 2.6f; // FBM lacunarity
    public float heightScale = 8.7f; // Height multiplier
    public float baseHeight = 3.1f; // Base height
    public int sandHeightThreshold = 6; // Max y for sand blocks
    public long seed = 67890L; // World seed
    public String noiseType = "Standard"; // Standard, Ridged, Billowy, Hybrid

}