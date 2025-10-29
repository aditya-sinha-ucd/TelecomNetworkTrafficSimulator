package util;

import java.util.List;

/**
 * A small collection of numerical helper methods used
 * throughout the simulator for statistical calculations.
 */
public final class MathUtils {

    private MathUtils() {}

    /**
     * Computes the arithmetic mean of a list of doubles.
     *
     * @param values the list of values
     * @return the mean, or 0 if the list is empty
     */
    public static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.size();
    }

    /**
     * Computes the sample variance of a list of doubles.
     *
     * @param values the list of values
     * @return variance, or 0 if the list is empty or has one element
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
     * Computes the standard deviation from a list of values.
     *
     * @param values list of data points
     * @return standard deviation
     */
    public static double stdDev(List<Double> values) {
        return Math.sqrt(variance(values));
    }

    /**
     * Finds the maximum value in a list.
     *
     * @param values list of doubles
     * @return maximum value, or 0 if empty
     */
    public static double max(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : values)
            if (v > max) max = v;
        return max;
    }
}
