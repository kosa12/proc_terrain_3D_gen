package edu.kosa.terrainproject.noise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerlinNoiseGenerator implements NoiseGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerlinNoiseGenerator.class);
    private final PermutationTable permutationTable;
    private final GradientTable gradientTable;
    private final double scale;

    public PerlinNoiseGenerator(long seed, double scale) {
        this.permutationTable = new PermutationTable(seed);
        this.gradientTable = new GradientTable();
        this.scale = scale;
        LOGGER.debug("Initialized PerlinNoiseGenerator with seed: {}, scale: {}", seed, scale);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    @Override
    public double noise(double x, double y) {
        x *= scale;
        y *= scale;

        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double u = fade(xf);
        double v = fade(yf);

        int aa = permutationTable.get(permutationTable.get(xi) + yi);
        int ab = permutationTable.get(permutationTable.get(xi) + yi + 1);
        int ba = permutationTable.get(permutationTable.get(xi + 1) + yi);
        int bb = permutationTable.get(permutationTable.get(xi + 1) + yi + 1);

        double g00 = gradientTable.dot(aa, xf, yf);
        double g10 = gradientTable.dot(ba, xf - 1, yf);
        double g01 = gradientTable.dot(ab, xf, yf - 1);
        double g11 = gradientTable.dot(bb, xf - 1, yf - 1);

        double x1 = lerp(g00, g10, u);
        double x2 = lerp(g01, g11, u);

        return lerp(x1, x2, v);
    }
}