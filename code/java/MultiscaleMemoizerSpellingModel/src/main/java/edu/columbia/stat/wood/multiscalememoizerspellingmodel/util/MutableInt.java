/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class MutableInt implements Comparable {

    private int value;

    public MutableInt (int value) {
        this.value = value;
    }

    public void set(int value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public int intValue() {
        return value;
    }

    @Override
    public int compareTo(Object t) {
        if (t.getClass() != getClass()) {
            throw new IllegalArgumentException("must only compare to other mutable ints");
        } else {
            MutableInt mi = (MutableInt) t;
            return value - mi.value;
        }
    }
}
