/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class Word {

    public int[] value;

    public Word(int[] value) {
        this.value = value;
    }

    public int[] getValue() {
        return value;
    }

    @Override
    public int hashCode() {
    	return Arrays.hashCode(value);
    }
    
    public void corrupt() {
        if (value.length > 1) {
            if (Util.rng.nextDouble() < 0.1) {
                int[] newValue = new int[value.length - 1];
                int remove = (int) (Util.rng.nextDouble() * value.length);
                System.arraycopy(value,0,newValue,0,remove);
                System.arraycopy(value,remove+1, newValue,remove, value.length - 1 - remove);
                value = newValue;
                return;
            }
        }
        
        if (Util.rng.nextDouble() < 0.5) {
            int[] newValue = new int[value.length + 1];
            int insert = (int) (Util.rng.nextDouble() * value.length);
            System.arraycopy(value,0,newValue,0,insert);
            newValue[insert] = (int) (Util.rng.nextDouble() * 26d);
            System.arraycopy(value,insert,newValue,insert+1,value.length - insert);
            value = newValue;
            return;
        } else {
            value[(int) (Util.rng.nextDouble() * value.length)] = (int) (Util.rng.nextDouble() * 26d);   
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o.getClass() == getClass()) {
            return Arrays.equals(((Word) o).value, value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String str = "";
        for (int i : value) {
            str += (char) (i + 97);
        }
        return str;
    }
}
