package edu.kosa.terrainproject.noise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FbmGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FbmGenerator.class);
    private final NoiseGenerator noiseGenerator;

    public FbmGenerator(NoiseGenerator noiseGenerator) {
        this.noiseGenerator = noiseGenerator;
        LOGGER.debug("Initialized FbmGenerator with noise generator: {}", noiseGenerator.getClass().getSimpleName());
    }

    public double generate(double x, double y, NoiseConfig config, NoiseVariant variant) {
        return variant.apply(this, x, y, config.octaves, config.persistence, config.lacunarity);
    }

    public double standardFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += noiseGenerator.noise(x * frequency, y * frequency) * amplitude;
            maxValue += total;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total;
    }

    public double ridgedFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        double weight = 1;

        for (int i = 0; i < octaves; i++) {
            double noiseVal = Math.abs(noiseGenerator.noise(x * frequency, y * frequency));
            noiseVal = 1 - noiseVal;
            noiseVal *= weight;
            weight = Math.min(noiseVal * 2, 1);
            total += noiseVal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    public double billowyFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            double noiseVal = Math.abs(noiseGenerator.noise(x * frequency, y * frequency));
            total += noiseVal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    public double hybridFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        double weight = 1;

        total = noiseGenerator.noise(x * frequency, y * frequency) * amplitude;
        maxValue = amplitude;
        amplitude *= persistence;
        frequency *= lacunarity;

        for (int i = 1; i < octaves; i++) {
            double noiseVal = noiseGenerator.noise(x * frequency, y * frequency);
            noiseVal *= weight;
            weight = Math.min(noiseVal * 2, 1);
            total += noiseVal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total;
    }
}