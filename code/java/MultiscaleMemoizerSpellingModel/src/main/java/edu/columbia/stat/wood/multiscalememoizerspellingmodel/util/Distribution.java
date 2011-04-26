/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.Random;


/**
 *
 * @author nicholasbartlett
 */
public interface Distribution<E> {
    double logProbability(E type);

    E generate();

    void incrementObservationCount(E type);

    void decrementObservationCount(E type);

    double score();

    void sample();
}
