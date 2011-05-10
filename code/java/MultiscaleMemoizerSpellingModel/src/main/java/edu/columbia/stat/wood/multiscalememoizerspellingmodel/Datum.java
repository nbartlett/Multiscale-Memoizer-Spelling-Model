/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.InDelLikelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;

/**
 *
 * @author nicholasbartlett
 */
public class Datum extends Customer {

    public Word word;

    public Datum(Word value) {
        word = value;
    }

    @Override
    public double logLikelihood(Word parameter, Likelihood like) {
        return like.logProb(parameter.value, word.value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o.getClass() == getClass()) {
            return ((Datum) o).word.equals(word);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + word.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return word.toString();
    }
}
