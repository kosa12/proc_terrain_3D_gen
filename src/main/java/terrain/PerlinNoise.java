package terrain;

import java.util.Random;

public class PerlinNoise {
    private final int[] permutation;
    private final double scale;

    public PerlinNoise(long seed, double scale) {
        this.scale = scale;
        // Initialize permutation table (0-255, doubled to 512 for wraparound)
        permutation = new int[512];
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        // Shuffle using seed
        Random rand = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        // Double the permutation table
        for (int i = 0; i < 256; i++) {
            permutation[i] = permutation[i + 256] = p[i];
        }
    }

    // Linear interpolation
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // Smoothstep function (6t^5 - 15t^4 + 10t^3)
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Gradient vectors (8 directions for 2D)
    private double[] getGradient(int hash) {
        // Use hash to select one of 8 unit vectors
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

    // Compute dot product of gradient and distance vector
    private double gradDot(int hash, double x, double y) {
        double[] gradient = getGradient(hash);
        return gradient[0] * x + gradient[1] * y;
    }

    // 2D Perlin noise at (x, y)
    public double noise(double x, double y) {
        // Scale coordinates
        x *= scale;
        y *= scale;

        // Grid cell coordinates
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        // Fractional parts
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        // Fade curves
        double u = fade(xf);
        double v = fade(yf);

        // Hash values for the four corners
        int aa = permutation[permutation[xi] + yi];
        int ab = permutation[permutation[xi] + yi + 1];
        int ba = permutation[permutation[xi + 1] + yi];
        int bb = permutation[permutation[xi + 1] + yi + 1];

        // Gradient contributions
        double g00 = gradDot(aa, xf, yf);
        double g10 = gradDot(ba, xf - 1, yf);
        double g01 = gradDot(ab, xf, yf - 1);
        double g11 = gradDot(bb, xf - 1, yf - 1);

        // Interpolate along x
        double x1 = lerp(g00, g10, u);
        double x2 = lerp(g01, g11, u);

        // Interpolate along y
        return lerp(x1, x2, v);
    }

    // Layered noise (Fractal Brownian Motion)
    public double fbm(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double maxValue = 0; // For normalization

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        // Normalize to [0, 1]
        return (total / maxValue + 1) / 2;
    }
}