/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

/**
 *
 * @author nicholasbartlett
 */

public abstract class ProbabilityModel<O, P> implements Distribution<O> {

    public Distribution<P> prior;

    public ProbabilityModel(Distribution<P> prior) {
        this.prior = prior;
    }

    public abstract void incrementObservationCount(O observation);

    public abstract void decrementObservationCount(O observation);

    public abstract double logLikelihood(P parameter);

    public abstract P getParameter();

    public abstract void setParameter(P e);

    public abstract void sampleParameters();
    
}
