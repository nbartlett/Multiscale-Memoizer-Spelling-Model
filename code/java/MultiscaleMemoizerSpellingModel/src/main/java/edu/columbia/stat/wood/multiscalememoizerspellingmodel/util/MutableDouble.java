/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class MutableDouble {

    private double value;

    public MutableDouble(double value) {
        this.value = value;
    }

    public double doubleValue() {
        return value;
    }

    public void set(double value) {
        this.value = value;
    }

    public void plusEquals(double adjustment) {
        value += adjustment;
    }

    public void timesEquals(double factor) {
        value *= factor;
    }
}
