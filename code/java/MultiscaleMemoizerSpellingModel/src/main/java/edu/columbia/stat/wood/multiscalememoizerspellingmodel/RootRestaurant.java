/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Distribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Pair;
import java.util.ArrayList;

/**
 *
 * @author nicholasbartlett
 */
@SuppressWarnings("serial")
public class RootRestaurant extends Restaurant {

    public Distribution<int[]> baseDistribution;

    public RootRestaurant(Distribution<int[]> baseDistribution){
        super(null,null,null);
        this.baseDistribution = baseDistribution;
    }

    @Override
    public Restaurant get(int index, Word[] context, MutableDouble[] concentrations, MutableDouble[] discounts) {
        throw new RuntimeException("not supported");
    }
    
    @Override
    public void seatWithParameter(Table childTable) {
        baseDistribution.incrementObservationCount(childTable.parameter.value);
    }
    
    @Override
    public double parameterProb(Word parameter) {
        return Math.exp(baseDistribution.logProbability(parameter.value));
    }
    
    @Override
    public void seat(Customer customer, Likelihood like) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void initSeatDatum(Datum datum) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void unseat(Table childTable, Table table) {
        assert table == null;
        baseDistribution.decrementObservationCount(childTable.parameter.value);
    }
    
    /*
    @Override
    public ArrayList<Word> generateParameters(int n) {
        ArrayList<Word> sample = new ArrayList<Word>(n);
        generateParameters(sample, 0,n);
        return sample;
    }

    @Override
    public void generateParameters(ArrayList<Word> sample, double r, int n) {
        while (n > 0) {
            sample.add(new Word(baseDistribution.generate()));
            n--;
        }
    }*/

    @Override
    public void sample(Likelihood like, boolean onlyDatum) {
        baseDistribution.sample();
    }

    @Override
    public double score(Likelihood like) {
        return baseDistribution.score();
    }

    @Override
    public void parentParamsAndWeights(Customer customer, Likelihood like, ArrayList<Pair<Word,Double>> logWeightsParams, MutableDouble log_tw, double log_scalar, int m, Table emptyTable){
        double log_w;
        log_scalar -= Math.log(m);
        Word word;
        
        assert !Double.isInfinite(log_scalar);
        
        if (emptyTable != null) {
            m -= 1;
            log_w = log_scalar + customer.logLikelihood(emptyTable.parameter, like);
            logWeightsParams.add(new Pair<Word, Double>(emptyTable.parameter, log_w));
            log_tw.addLogs(log_w);
        }
        
        for (int i = 0; i < m; i++) {
            log_w = log_scalar + customer.logLikelihood(word = new Word(baseDistribution.generate()), like);
            logWeightsParams.add(new Pair<Word, Double>(word, log_w));
            log_tw.addLogs(log_w);
        }
    }

    @Override
    public void logProbability(MutableDouble logProbability, int[] read, Likelihood like, double log_scalar) {
        logProbability.addLogs(log_scalar + baseDistribution.logProbability(read));
    }
}
