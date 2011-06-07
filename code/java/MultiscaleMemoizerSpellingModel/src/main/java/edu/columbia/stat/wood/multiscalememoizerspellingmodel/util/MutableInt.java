/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class MutableInt implements Comparable<MutableInt> {

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

    public int compareTo(MutableInt mi) {
    	return value - mi.value;
    }
}
