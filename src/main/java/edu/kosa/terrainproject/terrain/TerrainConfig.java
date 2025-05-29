package edu.kosa.terrainproject.terrain;

public class TerrainConfig {
    public float scale = 0.115f; // Perlin noise scale
    public int octaves = 6; // FBM octaves
    public float persistence = 0.175f; // FBM persistence
    public float lacunarity = 2.8f; // FBM lacunarity
    public float heightScale = 5.8f; // Height multiplier
    public float baseHeight = 7f; // Base height
    public long seed = 67890L; // World seed
    public String noiseType = "Standard"; // Standard, Ridged, Billowy, Hybrid
}