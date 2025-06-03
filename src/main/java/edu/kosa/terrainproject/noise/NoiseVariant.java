package edu.kosa.terrainproject.noise;

public enum NoiseVariant {
    STANDARD {
        @Override
        public double apply(FbmGenerator fbm, double x, double y, int octaves, double persistence, double lacunarity) {
            double value = fbm.standardFbm(x, y, octaves, persistence, lacunarity);
            return (value + 1) / 2; // Normalize to [0, 1]
        }
    },
    RIDGED {
        @Override
        public double apply(FbmGenerator fbm, double x, double y, int octaves, double persistence, double lacunarity) {
            return fbm.ridgedFbm(x, y, octaves, persistence, lacunarity); // [0, 1]
        }
    },
    BILLOWY {
        @Override
        public double apply(FbmGenerator fbm, double x, double y, int octaves, double persistence, double lacunarity) {
            return fbm.billowyFbm(x, y, octaves, persistence, lacunarity); // [0, 1]
        }
    },
    HYBRID {
        @Override
        public double apply(FbmGenerator fbm, double x, double y, int octaves, double persistence, double lacunarity) {
            double value = fbm.hybridFbm(x, y, octaves, persistence, lacunarity);
            return (value + 1) / 2; // Normalize to [0, 1]
        }
    };

    public abstract double apply(FbmGenerator fbm, double x, double y, int octaves, double persistence, double lacunarity);
}