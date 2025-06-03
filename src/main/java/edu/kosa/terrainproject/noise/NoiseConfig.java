package edu.kosa.terrainproject.noise;

public final class NoiseConfig {
    public final long seed;
    public final double scale;
    public final int octaves;
    public final double persistence;
    public final double lacunarity;

    public NoiseConfig(long seed, double scale, int octaves, double persistence, double lacunarity) {
        this.seed = seed;
        this.scale = scale;
        this.octaves = octaves;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
    }

    public static NoiseConfig forTerrain(long seed, double scale) {
        return new NoiseConfig(seed, scale, 5, 0.5, 2.6); // Defaults from TerrainConfig
    }

    public static NoiseConfig forWater(long seed) {
        return new NoiseConfig(seed, 0.02, 4, 0.4, 2.0); // Defaults from waterFbm
    }
}