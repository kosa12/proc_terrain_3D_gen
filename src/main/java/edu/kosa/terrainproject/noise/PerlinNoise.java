package edu.kosa.terrainproject.noise;

import java.util.Random;

public class PerlinNoise {
    private final int[] permutation;
    private final int[] waterPermutation;
    private final double scale;

    public PerlinNoise(long seed, double scale) {
        this.scale = scale;
        permutation = new int[512];
        waterPermutation = new int[512];
        int[] p = new int[256];
        int[] wp = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = wp[i] = i;
        }
        Random rand = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = wp[i];
            wp[i] = wp[j];
            wp[j] = temp;
        }
        for (int i = 0; i < 256; i++) {
            permutation[i] = permutation[i + 256] = p[i];
            waterPermutation[i] = waterPermutation[i + 256] = wp[i]; // Fill water permutation
        }
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double[] getGradient(int hash) {
        int h = hash & 7;
        return switch (h) {
            case 0 -> new double[]{1, 0};
            case 1 -> new double[]{-1, 0};
            case 2 -> new double[]{0, 1};
            case 3 -> new double[]{0, -1};
            case 4 -> new double[]{1, 1};
            case 5 -> new double[]{-1, 1};
            case 6 -> new double[]{1, -1};
            case 7 -> new double[]{-1, -1};
            default -> new double[]{0, 0};
        };
    }

    private double gradDot(int hash, double x, double y) {
        double[] gradient = getGradient(hash);
        return gradient[0] * x + gradient[1] * y;
    }

    private double waterNoise(double x, double y) {
        x *= 0.02; // Lower scale for large lakes
        y *= 0.02;

        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double u = fade(xf);
        double v = fade(yf);

        // Use waterPermutation instead of permutation
        int aa = waterPermutation[waterPermutation[xi] + yi];
        int ab = waterPermutation[waterPermutation[xi] + yi + 1];
        int ba = waterPermutation[waterPermutation[xi + 1] + yi];
        int bb = waterPermutation[waterPermutation[xi + 1] + yi + 1];

        double g00 = gradDot(aa, xf, yf);
        double g10 = gradDot(ba, xf - 1, yf);
        double g01 = gradDot(ab, xf, yf - 1);
        double g11 = gradDot(bb, xf - 1, yf - 1);

        double x1 = lerp(g00, g10, u);
        double x2 = lerp(g01, g11, u);

        return lerp(x1, x2, v);
    }

    public double noise(double x, double y) {
        x *= scale;
        y *= scale;

        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double u = fade(xf);
        double v = fade(yf);

        int aa = permutation[permutation[xi] + yi];
        int ab = permutation[permutation[xi] + yi + 1];
        int ba = permutation[permutation[xi + 1] + yi];
        int bb = permutation[permutation[xi + 1] + yi + 1];

        double g00 = gradDot(aa, xf, yf);
        double g10 = gradDot(ba, xf - 1, yf);
        double g01 = gradDot(ab, xf, yf - 1);
        double g11 = gradDot(bb, xf - 1, yf - 1);

        double x1 = lerp(g00, g10, u);
        double x2 = lerp(g01, g11, u);

        return lerp(x1, x2, v);
    }

    public double fbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        double value = (total / maxValue + 1) / 2;
        return value;
    }

    public double waterFbm(double x, double y) {
        int octaves = 4;
        double persistence = 0.4;
        double lacunarity = 2.0;
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += waterNoise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        double value = (total / maxValue + 1) / 2;
        return value;
    }

    public double ridgedFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        double weight = 1;

        for (int i = 0; i < octaves; i++) {
            double noiseVal = Math.abs(noise(x * frequency, y * frequency));
            noiseVal = 1 - noiseVal;
            noiseVal *= weight;
            weight = Math.min(noiseVal * 2, 1);
            total += noiseVal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        double value = (total / maxValue);
        return value;
    }

    public double billowyFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            double noiseVal = Math.abs(noise(x * frequency, y * frequency));
            total += noiseVal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        double value = (total / maxValue);
        return value;
    }

    public double hybridFbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0;
        double weight = 1;

        total = noise(x * frequency, y * frequency) * amplitude;
        maxValue = amplitude;
        amplitude *= persistence;
        frequency *= lacunarity;

        for (int i = 1; i < octaves; i++) {
            double noiseVal = noise(x * frequency, y * frequency);
            noiseVal *= weight;
            weight = Math.min(noiseVal * 2, 1);
            total += noiseVal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        double value = (total / maxValue + 1) / 2;
        return value;
    }
}