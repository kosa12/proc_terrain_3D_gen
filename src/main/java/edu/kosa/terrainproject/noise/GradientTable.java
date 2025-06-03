package edu.kosa.terrainproject.noise;

public final class GradientTable {
    private static final double[][] GRADIENTS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
    };

    public double[] getGradient(int hash) {
        return GRADIENTS[hash & (GRADIENTS.length - 1)];
    }

    public double dot(int hash, double x, double y) {
        double[] gradient = getGradient(hash);
        return gradient[0] * x + gradient[1] * y;
    }
}