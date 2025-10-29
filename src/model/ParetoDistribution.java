package model;

import java.util.Random;

public class ParetoDistribution implements Distribution {

    private final double shape;
    private final double scale;
    private final Random rng;

    public ParetoDistribution(double shape, double scale, Long seed) {
        if (shape <= 0 || scale <= 0)
            throw new IllegalArgumentException("Pareto: shape and scale must be positive.");
        this.shape = shape;
        this.scale = scale;
        this.rng = (seed == null) ? new Random() : new Random(seed);
    }

    @Override
    public double draw() {
        double u = rng.nextDouble();
        if (u <= 1e-12) u = 1e-12;
        return scale / Math.pow(u, 1.0 / shape);
    }
}