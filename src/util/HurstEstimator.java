package util;

import java.util.List;

/**
 * Estimates the Hurst exponent (H) of a time series.
 * <p>
 * The Hurst exponent measures the degree of self-similarity or
 * long-range dependence in the simulated traffic.
 * <p>
 * Common interpretation:
 *  - H ≈ 0.5 → random (no correlation, Poisson-like)
 *  - H > 0.5 → persistent / self-similar (bursty traffic)
 *  - H < 0.5 → anti-persistent (alternating patterns)
 * <p>
 * This implementation uses a simple Rescaled Range (R/S) method.
 */
public class HurstEstimator {

    /**
     * Estimates the Hurst exponent using the R/S analysis.
     *
     * @param data list of recorded activity rates (0–1)
     * @return estimated Hurst exponent in range [0, 1]
     */
    public static double estimateHurst(List<Double> data) {
        if (data == null || data.size() < 10) return 0.5; // fallback

        int n = data.size();
        int numSegments = Math.max(4, n / 10); // divide data into segments

        double[] rsValues = new double[numSegments];
        double[] logN = new double[numSegments];

        for (int seg = 1; seg <= numSegments; seg++) {
            int segmentLength = n / seg;
            if (segmentLength < 5) break;

            double mean = 0;
            for (double v : data.subList(0, segmentLength)) mean += v;
            mean /= segmentLength;

            // cumulative deviations
            double cumulative = 0;
            double min = 0, max = 0;
            for (int i = 0; i < segmentLength; i++) {
                cumulative += (data.get(i) - mean);
                if (cumulative > max) max = cumulative;
                if (cumulative < min) min = cumulative;
            }
            double range = max - min;

            // standard deviation of segment
            double variance = 0;
            for (int i = 0; i < segmentLength; i++)
                variance += Math.pow(data.get(i) - mean, 2);
            variance /= segmentLength;
            double std = Math.sqrt(variance);

            rsValues[seg - 1] = std > 0 ? range / std : 0;
            logN[seg - 1] = Math.log(segmentLength);
        }

        // Compute slope of log(R/S) vs log(N)
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int count = numSegments;
        for (int i = 0; i < count; i++) {
            if (rsValues[i] <= 0) continue;
            double x = logN[i];
            double y = Math.log(rsValues[i]);
            sumX += x; sumY += y; sumXY += x * y; sumXX += x * x;
        }
        double slope = (count * sumXY - sumX * sumY) / (count * sumXX - sumX * sumX);

        // The slope ≈ Hurst exponent
        return Math.min(1.0, Math.max(0.0, slope));
    }
}
