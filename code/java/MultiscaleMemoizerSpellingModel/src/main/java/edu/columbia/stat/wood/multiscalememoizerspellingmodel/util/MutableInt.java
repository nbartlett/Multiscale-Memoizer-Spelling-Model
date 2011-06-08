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
    
    public static MutableInt[] toMutableArray(int[] ints) {
    	MutableInt[] mints = new MutableInt[ints.length];
    	for (int i = 0; i < mints.length; i++)
    		mints[i] = new MutableInt(ints[i]);
    	return mints;
    }


    public int intValue() {
        return value;
    }

    public int compareTo(MutableInt mi) {
    	return value - mi.value;
    }
}
