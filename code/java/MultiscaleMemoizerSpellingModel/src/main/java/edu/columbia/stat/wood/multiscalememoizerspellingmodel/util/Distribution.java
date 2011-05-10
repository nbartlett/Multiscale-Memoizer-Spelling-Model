/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */
public interface Distribution<E> {
    
    double logProbability(E type);

    E generate();

    void incrementObservationCount(E observation);

    void decrementObservationCount(E observation);

    double score();
    void sample();
}
