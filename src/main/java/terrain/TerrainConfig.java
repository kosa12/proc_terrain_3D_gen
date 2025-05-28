package terrain;

public class TerrainConfig {
    public float scale = 0.1f; // Perlin noise scale
    public int octaves = 8; // FBM octaves
    public float persistence = 0.6f; // FBM persistence
    public float lacunarity = 2.5f; // FBM lacunarity
    public float heightScale = 2.0f; // Height multiplier
    public float baseHeight = 7.0f; // Base height
    public long seed = 67890L; // World seed
}