/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class Pair<E,F> {

    private E first;
    private F second;
    
    public Pair(E e, F f) {
        first = e;
        second = f;
    }
    
    public E first() {
        return first;
    }
    
    public F second() {
        return second;
    }
}
