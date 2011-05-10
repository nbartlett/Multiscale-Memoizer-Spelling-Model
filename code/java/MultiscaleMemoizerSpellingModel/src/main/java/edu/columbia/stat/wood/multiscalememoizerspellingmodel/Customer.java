/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;

/**
 *
 * @author nicholasbartlett
 */
public abstract class Customer {
    
    public abstract double logLikelihood(Word parameter, Likelihood like);

}
