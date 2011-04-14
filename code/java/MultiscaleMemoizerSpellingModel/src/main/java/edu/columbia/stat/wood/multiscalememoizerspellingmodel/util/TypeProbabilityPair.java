/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public class TypeProbabilityPair {
    public int type;
    public double probability;

    public TypeProbabilityPair(int type, double probability) {
        this.type = type;
        this.probability = probability;
    }

    public TypeProbabilityPair(){};
}
