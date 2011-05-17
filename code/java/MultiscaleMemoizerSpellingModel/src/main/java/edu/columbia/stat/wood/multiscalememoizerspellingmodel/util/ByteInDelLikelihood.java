/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;


/**
 *
 * @author nicholasbartlett
 */
public class ByteInDelLikelihood { //implements Likelihood{

    private double[] lambda = new double[]{0.0
                                          ,0.0
                                          ,0.0
                                          ,0.0
                                          ,0.001
                                          ,0.025
                                          ,0.05
                                          ,0.1};

    private double[] logLambda = new double[8];
    
    private double logSum = 0d;

    private byte[] logProbContains;
    private double[] logProbs;


    public ByteInDelLikelihood() {
        update();
    }

    //@Override
    public double logProb(int[] reference, int[] read) {
        assert reference.length == 8;
        assert read.length == 8;
        
        double logProb = 0d;
        for (int i = 0; i < 8; i++) {
            if (reference[i] != read[i]) logProb += Math.log(lambda[i]);
        }
        //System.out.println(logProb + ", " + logSum);
        return logProb - logSum;
    }

    public void update() {
        updateLogSum();
        updateLogLambda();
    }

    public void updateLogSum() {
        logSum = updateLogSum(0);
    }

    public void updateLogLambda() {
        for (int i = 0; i < 8; i++) {
            logLambda[i] = Math.log(lambda[i]);
        }
    }

    private double updateLogSum(int index){
        if (index < 7) {
            return Math.log(lambda[index] + 1d) + updateLogSum(index + 1);
        } else {
            return Math.log(lambda[index] + 1d);
        }
    }

    public void clear(){};
}
