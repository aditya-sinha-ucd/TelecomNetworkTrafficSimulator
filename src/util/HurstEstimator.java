/**
 * @file src/util/HurstEstimator.java
 * @brief Computes the Hurst exponent H using classical Rescaled Range (R/S) analysis.
 *
 * @details This implementation fixes segmentation, cumulative deviations,
 *          averaging across windows, and linear regression. It produces
 *          stable and correct H estimates for long-memory processes such as FGN.
 */

package util;

import java.util.ArrayList;
import java.util.List;

/**
 * @class HurstEstimator
 * @brief Static utility class implementing Hurst exponent estimation.
 *
 * @details Uses the classical R/S method:
 *          - Split the series into segments of size N_k
 *          - Compute R/S for each segment
 *          - Average R/S over segments
 *          - Regress log(R/S) against log(N_k)
 */
public final class HurstEstimator {

    private HurstEstimator() { }

    /**
     * @brief Estimate H using the Rescaled-Range (R/S) analysis.
     *
     * @param data Time-series values (double).
     * @return Estimated Hurst exponent in (0,1). Returns 0.5 if insufficient data.
     */
    public static double estimateHurst(List<Double> data) {
        if (data == null || data.size() < 20)
            return 0.5; // fallback for tiny samples

        int n = data.size();

        // Choose segment sizes (N_k)
        List<Integer> segmentSizes = computeSegmentSizes(n);

        List<Double> logN = new ArrayList<>();
        List<Double> logRS = new ArrayList<>();

        for (int size : segmentSizes) {
            List<double[]> segments = splitIntoSegments(data, size);
            if (segments.isEmpty()) continue;

            double avgRS = 0.0;
            int validSegments = 0;

            // Compute R/S for every segment of this size
            for (double[] segment : segments) {
                double rs = computeRS(segment);
                if (rs > 0) {
                    avgRS += rs;
                    validSegments++;
                }
            }

            if (validSegments == 0) continue;

            avgRS /= validSegments;

            logN.add(Math.log(size));
            logRS.add(Math.log(avgRS));
        }

        if (logN.size() < 2) return 0.5;

        return linearRegressionSlope(logN, logRS);
    }



    /* ------------------------------------------------------------
     *  Helper functions
     * ------------------------------------------------------------ */

    /** Generates reasonable segment sizes: n/2, n/4, n/8, ... */
    private static List<Integer> computeSegmentSizes(int n) {
        List<Integer> sizes = new ArrayList<>();
        int size = n / 2;

        while (size >= 8) {
            sizes.add(size);
            size /= 2;
        }
        return sizes;
    }

    /** Splits data into consecutive windows of length size. */
    private static List<double[]> splitIntoSegments(List<Double> data, int size) {
        List<double[]> segments = new ArrayList<>();

        for (int start = 0; start + size <= data.size(); start += size) {
            double[] seg = new double[size];
            for (int i = 0; i < size; i++)
                seg[i] = data.get(start + i);
            segments.add(seg);
        }
        return segments;
    }

    /** Computes classical R/S value for a segment. */
    private static double computeRS(double[] segment) {
        int n = segment.length;

        // Mean
        double mean = 0;
        for (double v : segment) mean += v;
        mean /= n;

        // Cumulative dev
        double[] cum = new double[n];
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (segment[i] - mean);
            cum[i] = sum;
        }

        double range = max(cum) - min(cum);

        // Standard deviation
        double var = 0;
        for (double v : segment)
            var += Math.pow(v - mean, 2);

        var /= n;
        double std = Math.sqrt(var);

        return (std > 0) ? (range / std) : 0;
    }

    private static double max(double[] v) {
        double m = v[0];
        for (double x : v) if (x > m) m = x;
        return m;
    }

    private static double min(double[] v) {
        double m = v[0];
        for (double x : v) if (x < m) m = x;
        return m;
    }

    /** Simple linear regression slope: slope = cov(x,y)/var(x) */
    private static double linearRegressionSlope(List<Double> x, List<Double> y) {
        int n = x.size();
        double sumX = 0, sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
        }

        double meanX = sumX / n;
        double meanY = sumY / n;

        double num = 0, den = 0;
        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - meanX;
            double dy = y.get(i) - meanY;
            num += dx * dy;
            den += dx * dx;
        }

        double slope = (den > 0) ? (num / den) : 0.5;

        // H is theoretically in (0,1)
        return Math.min(1.0, Math.max(0.0, slope));
    }
}
