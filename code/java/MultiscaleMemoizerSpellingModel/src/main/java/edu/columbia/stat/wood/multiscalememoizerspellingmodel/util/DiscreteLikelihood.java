/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public abstract class DiscreteLikelihood {

    public abstract double logLikelihood(int parameter, int observation);

    public abstract int generate(int parameter);

    public abstract int[] generate(int paramater, int count);
    
}
