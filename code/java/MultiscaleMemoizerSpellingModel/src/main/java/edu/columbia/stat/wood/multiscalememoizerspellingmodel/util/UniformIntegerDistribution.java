/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.Random;

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
        this.alphabetSize = alphabetSize;
        this.leftShift = 0;
    }

    @Override
    public double logProbability(Integer type) {
        type = type + leftShift;
        if (type < alphabetSize && type >= 0) {
            return Math.log(1d / (double) alphabetSize);
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public Integer generate() {
        return (int) (Util.rng.nextDouble() * alphabetSize) - leftShift;
    }

    @Override
    public double score() {
        return 0d;
    }

    @Override
    public void incrementObservationCount(Integer type) {}

    @Override
    public void decrementObservationCount(Integer type) {}

    @Override
    public void sample() {}
}
