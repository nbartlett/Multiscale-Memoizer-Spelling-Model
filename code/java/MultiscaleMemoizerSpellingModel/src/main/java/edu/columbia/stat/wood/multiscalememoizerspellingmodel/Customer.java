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
public interface Customer {
    
    public double logLikelihood(Word parameter, Likelihood like);

}
