/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class ByteArrayList {
    private byte[] value = new byte[256];
    private int index = 0;

    public void add(byte b) {
        value[index++] = b;
        if (index == value.length) {
            byte[] new_value = new byte[value.length + 256];
            System.arraycopy(value, 0, new_value, 0, index);
            value = new_value;
        }
    }

    public byte[] toArray() {
        byte[] arrayReturn = new byte[index];
        System.arraycopy(value, 0, arrayReturn, 0, index);
        return arrayReturn;
    }

    public byte get(int index) {
        if (index < this.index) {
            return value[index];
        } else {
            throw new IllegalArgumentException("index must be less than the length of the byte array");
        }
    }

    public int length() {
        return index;
    }
}
