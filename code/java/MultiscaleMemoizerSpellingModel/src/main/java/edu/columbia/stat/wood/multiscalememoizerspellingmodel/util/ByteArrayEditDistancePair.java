/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.Arrays;

/**
 *
 * @author nicholasbartlett
 */
public class ByteArrayEditDistancePair {
    public byte[] byteArray;
    public double d;

    public ByteArrayEditDistancePair(byte[] byteArray, double d) {
        this.byteArray = byteArray;
        this.d = d;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Arrays.hashCode(this.byteArray);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o.getClass() == getClass()) {
            ByteArrayEditDistancePair cast_object = (ByteArrayEditDistancePair) o;
            return Arrays.equals(byteArray, cast_object.byteArray);
        } else {
            return false;

        }
    }
}
