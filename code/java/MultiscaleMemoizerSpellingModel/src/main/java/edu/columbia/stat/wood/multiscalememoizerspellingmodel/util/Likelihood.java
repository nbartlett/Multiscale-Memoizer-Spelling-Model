/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.HPYP;

/**
 *
 * @author nicholasbartlett
 */
public interface Likelihood {

    double logProb(int[] reference, int[] read);
    
    void sample (HPYP hpyp);

}
