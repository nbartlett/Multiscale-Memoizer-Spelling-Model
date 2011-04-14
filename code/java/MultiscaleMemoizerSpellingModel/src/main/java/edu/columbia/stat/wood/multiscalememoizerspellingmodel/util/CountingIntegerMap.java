/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class CountingIntegerMap extends HashMap<Integer,MutableInt> {

    public MutableInt increment(Integer key) {
        MutableInt value = get(key);
        if (value == null) {
            put(key, value = new MutableInt(0));
        }
        value.increment();
        return value;
    }

    public MutableInt decrement (Integer key) {
        MutableInt value = get(key);
        value.decrement();
        return value;
    }
}
