/**
 * @file src/util/MathUtils.java
 * @brief Collection of numerical helper methods used by the simulator.
 * @details Offers mean, variance, standard deviation, and max calculations for
 *          {@link java.util.List} collections of doubles. Primarily consumed by
 *          {@link core.StatisticsCollector} and {@link util.HurstEstimator}.
 */
package util;

import java.util.List;

/**
 * @class MathUtils
 * @brief Static utilities for common statistical calculations.
 */
public final class MathUtils {

    private MathUtils() {}

    /**
     * @brief Computes the arithmetic mean of a list of doubles.
     * @param values List of values.
     * @return Mean, or 0 if the list is empty.
     */
    public static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.size();
    }

    /**
     * @brief Computes the sample variance of a list of doubles.
     * @param values List of values.
     * @return Variance, or 0 if the list is empty or has one element.
     */
    public static double variance(List<Double> values) {
        if (values == null || values.size() < 2) return 0.0;
        double mean = mean(values);
        double sumSq = 0.0;
        for (double v : values) {
            double diff = v - mean;
            sumSq += diff * diff;
        }
        return sumSq / (values.size() - 1);
    }

    /**
     * @brief Computes the standard deviation from a list of values.
     * @param values List of data points.
     * @return Standard deviation.
     */
    public static double stdDev(List<Double> values) {
        return Math.sqrt(variance(values));
    }

    /**
     * @brief Finds the maximum value in a list.
     * @param values List of doubles.
     * @return Maximum value, or 0 if empty.
     */
    public static double max(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : values)
            if (v > max) max = v;
        return max;
    }
}
