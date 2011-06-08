/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class UniformIntegerDistribution implements Distribution<Integer> {

    private int alphabetSize;
    private int leftShift;

    public UniformIntegerDistribution(int alphabetSize, int leftShift) {
        this.alphabetSize = alphabetSize;
        this.leftShift = leftShift;
    }

    public UniformIntegerDistribution(int alphabetSize) {
        this(alphabetSize, 0);
    }

    public double logProbability(Integer type) {
        type = type + leftShift;
        if (type < alphabetSize && type >= 0) {
            return Math.log(1.0 / alphabetSize);
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public Integer generate() {
        return (int) (Util.rng.nextDouble() * alphabetSize) - leftShift;
    }

    public double score() {
        return 0;
    }

    public void incrementObservationCount(Integer type) {}

    public void decrementObservationCount(Integer type) {}

    public void sample() {}
}
