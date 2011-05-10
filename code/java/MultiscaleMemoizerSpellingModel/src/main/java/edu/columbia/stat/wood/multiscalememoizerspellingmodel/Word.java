/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

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
        int hash = 3;
        hash = 11 * hash + Arrays.hashCode(this.value);
        return hash;
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
