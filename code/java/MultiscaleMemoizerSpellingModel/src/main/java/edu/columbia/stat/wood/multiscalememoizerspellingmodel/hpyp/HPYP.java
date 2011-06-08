/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Distribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.UniformIntegerDistribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 *
 * @author nicholasbartlett
 */
public class HPYP implements Distribution<int[]>{

    private Restaurant ecr, root;
    private MutableDouble[] discounts;
    private MutableDouble[] concentrations;

    public HPYP(Distribution<Integer> baseDistribution) {
        concentrations = MutableDouble.toMutableArray(new double[]{20, 10, 5, 1, 1, 1, 1, 1});
        discounts = MutableDouble.toMutableArray(new double[]{.1, .3, .5, .6, .7, .8, .9, .9});

        root = new RootRestaurant(baseDistribution);
        ecr = new Restaurant(root, this.concentrations[0], this.discounts[0]);
    }

    public HPYP(MutableDouble[] discounts, MutableDouble[] concentrations, Distribution<Integer> baseDistribution) {
        this.discounts = discounts;
        this.concentrations = concentrations;        
        root = new RootRestaurant(baseDistribution);
        ecr = new Restaurant(root, this.concentrations[0], this.discounts[0]);
    }

    public double logProbability(int[] word) {
        double logProb = 0d;

        int[] context;
        for (int i = 0; i < word.length; i++) {
            context = new int[i];
            System.arraycopy(word,0,context,0,i);
            logProb += Math.log(get(context).probability(word[i]));
        }
        logProb += Math.log(get(word).probability(-1));
        
        return logProb;
    }

    public int[] generate() {
        int maxWordLength = 1000;

        int[] rawSample = new int[maxWordLength];
        int[] context;
        
        int c;
        int i = 0;
        while (i < maxWordLength) {
            context = new int[i];
            System.arraycopy(rawSample, 0, context, 0, i);

            c = get(context).generate(Util.rng);

            if (c > -1) {
                rawSample[i++] = c;
            } else {
                break;
            }
        }

        if (i == maxWordLength) {
            System.out.println();
            System.out.print("HIT MAX WORD LENGTH");
            System.out.println();
        }

        int[] sample = new int[i];
        System.arraycopy(rawSample, 0, sample, 0, i);
        return sample;
    }

    public void incrementObservationCount(int[] word) {
        int[] context;
        for (int i = 0; i < word.length; i++) {
            context = new int[i];
            System.arraycopy(word,0,context,0,i);
            get(context).seat(word[i], Util.rng);
        }
        get(word).seat(-1, Util.rng);
    }

    public void decrementObservationCount(int[] word) {
        int[] context;
        for (int i = 0; i < word.length; i++) {
            context = new int[i];
            System.arraycopy(word,0,context,0,i);
            get(context).unseat(word[i], Util.rng);
        }
        get(word).unseat(-1,Util.rng);
    }

    public void sample() {
        sampleSeatingArrangements();
        sampleHyperParams(0.07, 0.7);
    }

    public void unseat(int[] context, int type){
        get(context).unseat(type, Util.rng);
    }

    public void seat(int[] context, int type){
        get(context).seat(type, Util.rng);
    }

    public void sampleSeatingArrangements() {
        sampleSeatingArrangements(ecr);
    }

    public double sampleHyperParams(double stdDiscounts, double stdConcentrations) {
        double[] currentScore = scoreByDepth();

        // get the current values
        double[] currentDiscounts = new double[discounts.length];
        double[] currentConcentrations = new double[concentrations.length];
        for (int i = 0; i < discounts.length; i++) {
            currentDiscounts[i] = discounts[i].doubleValue();
            currentConcentrations[i] = concentrations[i].doubleValue();
        }

        // make proposals for discounts
        for (int i = 0; i < discounts.length; i++) {
            discounts[i].plusEquals(stdDiscounts * Util.rng.nextGaussian());
            if (discounts[i].doubleValue() >= 1.0 || discounts[i].doubleValue() <= 0.0) {
                discounts[i].set(currentDiscounts[i]);
            }
        }

        // get score given proposals
        double[] afterScore = scoreByDepth();

        // choose to accept or reject each proposal
        for (int i = 0; i < discounts.length; i++) {
            if (Util.rng.nextDouble() < Math.exp(afterScore[i] - currentScore[i])) {
                currentScore[i] = afterScore[i];
            } else {
                discounts[i].set(currentDiscounts[i]);
            }
        }

        //make proposals for concentrations
        for (int i = 0; i < concentrations.length; i++) {
            concentrations[i].plusEquals(stdConcentrations * Util.rng.nextGaussian());
            if (concentrations[i].doubleValue() <= 0.0) {
                concentrations[i].set(currentConcentrations[i]);
            }
        }

        // get score given proposals
        afterScore = scoreByDepth();

        // choose to accept or reject each proposal
        double score = 0.0;
        for (int i = 0; i < concentrations.length; i++) {
            double r = Math.exp(afterScore[i] - currentScore[i]);

            if (Util.rng.nextDouble() < r) {
                score += afterScore[i];
            } else {
                score += currentScore[i];
                concentrations[i].set(currentConcentrations[i]);
            }
        }

        return score + root.score();
    }

    public double score() {
        return score(ecr) + root.score();
    }

    public void score(int[] context, int[] test, int depth, PrintStream ps) {
        int[] c;
        if (context.length >= depth) {
            c = new int[depth];
            System.arraycopy(context, context.length - depth, c, 0, depth);
        } else {
            c = context;
        }
        
        for (int i : test) {
            ps.print(get(c).probability(i));
            ps.print(", ");

            if (c.length < depth) {
                int[] newc = new int[c.length + 1];
                System.arraycopy(c,0,newc,0,c.length);
                newc[c.length] = i;
                c = newc;
            } else {
                for (int j = 1; j < depth; j++) {
                    c[j-1] = c[j];
                }
                c[depth-1] = i;
            }
        }
        ps.println();
    }

    public void printDiscounts() {
        System.out.print("Discounts = [" + discounts[0].doubleValue());
        for (int i = 1; i < discounts.length; i++) {
            System.out.print(", " + discounts[i].doubleValue());
        }
        System.out.println("]");
    }

    public void printConcentrations() {
        System.out.print("Concentrations = [" + concentrations[0].doubleValue());
        for (int i = 1; i < concentrations.length; i++) {
            System.out.print(", " + concentrations[i].doubleValue());
        }
        System.out.println("]");
    }
    
    private MutableDouble getDiscount(int depth) {
    	return discounts[depth < discounts.length ? depth : discounts.length - 1];
    }

    private MutableDouble getConcentration(int depth) {
    	return concentrations[depth < concentrations.length ? depth : concentrations.length - 1];
    }

    public double prob(int[] context, int w) {
        return get(context).probability(w);
    }
    
    private Restaurant get(int[] context) {
        if (context == null || context.length == 0) {
            return ecr;
        } else {
            int index = context.length - 1;
            int d = 0;
            Restaurant c, r = ecr;

            while (index > -1) {
                c = r.get(context[index]);
                if (c == null) {
                    r.put(context[index], c = new Restaurant(r, getConcentration(d + 1), getDiscount(d + 1)));
                }
                r = c;
                d++;
                index--;
            }

            return r;
        }
    }

    private boolean checkCounts(Restaurant r) {
        for (Restaurant child : r.values()) {
            if (!checkCounts(child)) {
                return false;
            }
        }

        return r.checkCounts();
    }

    private void sampleSeatingArrangements(Restaurant r) {
        ArrayList<Integer> keysToRemove = new ArrayList<Integer>();
        for (Entry<Integer, Restaurant> entry : r.entrySet()) {
            if (entry.getValue().hasNoCustomers()) {
                keysToRemove.add(entry.getKey());
            } else {
                sampleSeatingArrangements(entry.getValue());
            }
        }

        for (Integer key : keysToRemove) {
            r.remove(key);
        }

        r.sampleSeatingArrangements(Util.rng);
        r.removeZeros();
    }

    private double[] scoreByDepth() {
        double[] score = new double[discounts.length];
        scoreByDepth(ecr, 0, score);
        return score;
    }

    private void scoreByDepth(Restaurant r, int depth, double[] score) {
        for (Restaurant child : r.values()) {
            scoreByDepth(child, depth + 1, score);
        }

        score[depth < discounts.length ? depth : (discounts.length - 1)] += r.score();
    }

    private double score(Restaurant r) {
        double score = 0.0;
        for (Restaurant child : r.values()) {
            score += score(child);
        }

        return score + r.score();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        File f = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/AliceInWonderland.txt");

        HPYP hpyp = new HPYP(new UniformIntegerDistribution(256));

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            int b;
            int depth = 6;
            int[] context = new int[depth];

            for (int i = 0; i < depth; i++) {
                context[i] = bis.read();
            }

            while ((b = bis.read()) > -1) {
                hpyp.seat(context, b);
                for (int i = 1 ; i < depth; i++) {
                    context[i-1] = context[i];
                }
                context[depth-1] = b;
            }
        } finally {
            bis.close();
        }

        hpyp.checkCounts(hpyp.ecr);
        System.out.println(hpyp.score());

        System.out.println();
        for (int i = 0; i < 10; i++) {
            hpyp.sampleSeatingArrangements();
            System.out.println(hpyp.sampleHyperParams(0.07, 0.7));
            //System.out.println(hpyp.score());
            //hpyp.printDiscounts();
            //hpyp.printConcentrations();
            //System.out.println();
      }

        for (int i = 0; i < 100; i++) {
            int[] word = hpyp.generate();
            for(int j = 0; j < word.length; j++) {
                System.out.print((char) word[j]);
            }
            System.out.println();
            System.out.println();
        }


        System.out.println();

        hpyp.printDiscounts();
        hpyp.printConcentrations();
    }
}
