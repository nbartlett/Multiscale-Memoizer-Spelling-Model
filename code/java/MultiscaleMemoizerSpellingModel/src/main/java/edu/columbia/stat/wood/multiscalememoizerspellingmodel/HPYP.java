/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Distribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.FileWordIterator;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.InDelLikelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableInt;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.UniformIntegerDistribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class HPYP {

    public MutableDouble[] concentrations;
    public MutableDouble[] discounts;
    public RootRestaurant root;
    public static Restaurant ecr;
    public Likelihood like;

    public HPYP(Distribution<int[]> baseDistribution) {
        MutableDouble c0 = new MutableDouble(20);
        MutableDouble c1 = new MutableDouble(10);
        MutableDouble c2 = new MutableDouble(10);
        MutableDouble c3 = new MutableDouble(10);

        MutableDouble d0 = new MutableDouble(.1);
        MutableDouble d1 = new MutableDouble(.3);
        MutableDouble d2 = new MutableDouble(.7);
        MutableDouble d3 = new MutableDouble(.9);

        concentrations = new MutableDouble[]{c0, c1, c2, c3};
        discounts = new MutableDouble[]{d0, d1, d2, d3};

        root = new RootRestaurant(baseDistribution);
        ecr = new Restaurant(root, concentrations[0], discounts[0]);

        like = new InDelLikelihood(2,26, Util.rng);
    }

    public Restaurant get(Word[] context) {
        if (context == null) {
            return ecr;
        } else {
            return ecr.get(context.length - 1, context, concentrations, discounts);
        }
    }

    public void seat(Word[] context, Word observation) {
        get(context).seat(new Datum(observation), like);
    }

    public void seatInit(Word[] context, Word observation) {
        get(context).initSeatDatum(new Datum(observation));
    }

    public double score() {
        return ecr.score(like) + root.score(like);
    }
    
    private double[] scoreByDepth() {
        double[] score = new double[discounts.length];
        ecr.score(like, score, 0);
        return score;
    }
    
    public double sampleDiscountsConcentrations() {
        double stdDiscounts = 0.07;
        double stdConcentrations = 0.7;
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

        return score + root.score(like);
    }

    public void score(Word[] context, Word[] testSequence, int depth, PrintStream ps) throws Exception {
        Word[] cxt = new Word[depth];
        System.arraycopy(context, 0, cxt, 0, depth < context.length ? depth : context.length);

        try {
            for (Word testWord : testSequence) {
                ps.print(Math.exp(logProbability(cxt, testWord)));
                ps.print(", ");

                for (int i = 1; i < depth; i++) {
                    cxt[i - 1] = cxt[i];
                }
                cxt[depth - 1] = testWord;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public double misspelledWords(HashSet<Word> lookup) {
        int mispelledTables = 0;
        
        for (Table table : ecr.tables) {
            if (!lookup.contains(table.parameter)) {
                mispelledTables++;
            }
        }
        
        return (double) mispelledTables / (double) ecr.tables.size();
    }


    public void sample(boolean onlyDatum) {
        root.sample(like, onlyDatum);
        sample(ecr,onlyDatum);
        root.sample(like, onlyDatum);
    }

    public double logProbability(Word[] context, Word observation) {
        MutableDouble logProbability = new MutableDouble(Double.NEGATIVE_INFINITY);
        get(context).logProbability(logProbability, observation.value, like, 0d);
        return logProbability.doubleValue();
    }

    public boolean checkCustomerCounts() {
        return checkCustomerCounts(ecr);
    }

    private boolean checkCustomerCounts(Restaurant r) {
        for (Restaurant c : r.values()) {
            if (!checkCustomerCounts(c)) {
                return false;
            }
        }

        return r.checkCustomerCounts();
    }

    private void sample(Restaurant r, boolean onlyDatum) {
        for (Restaurant c : r.values()) {
            sample(c, onlyDatum);
        }
        r.sample(like,onlyDatum);
    }

    private double score(Restaurant r) {
        double s = r.score(like);
        for (Restaurant c : r.values()) {
            s += score(c);
        }
        return s;
    }

    @Override
    public String toString() {
        return ecr.toString();
    }

    public static void main(String[] args) throws IOException, Exception {
        File in;
        File out;
        int trainingSize;
        Word[] test;
        boolean sampleLike = true;
        if (args.length == 0) {
            //in = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/multiscale_parsed_aiw.txt");
            in = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/aiw_spelling_corrupted_10.txt");
            //in = new File("/Users/nicholasbartlett/Desktop/small_test_data.txt");
            out = new File("/Users/nicholasbartlett/Documents/np_bayes/Multiscale_Memoizer_Spelling_Model/output/aiw_spelling.out");
            trainingSize = 2000;
            test = new Word[0];
        } else {
            in = new File(args[0]);
            out = new File(args[1]);
            trainingSize = Integer.parseInt(args[2]);
            test = new Word[Integer.parseInt(args[3])];
            sampleLike = Boolean.parseBoolean(args[4]);
        }

        int d = 2;
        Word[] context = new Word[2];

        HPYP hpyp = new HPYP(new edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp.HPYP(new UniformIntegerDistribution(27, 1)));

        FileWordIterator fwi = null;
        try {
            fwi = new FileWordIterator(in);

            Word w;
            int i = 0;
            while (fwi.hasNext()) {
                trainingSize--;
                if (trainingSize >= 0) {
                    hpyp.seatInit(context, w = new Word(fwi.next()));
                    for (int j = 1; j < d; j++) {
                        context[j - 1] = context[j];
                    }
                    context[d - 1] = w;
                } else {
                    if (i < test.length) test[i++] = new Word(fwi.next());
                    else break;
                }
            }
        } finally {
            if (fwi != null) {
                fwi.close();
            }
        }

        System.out.println(hpyp.score());
        System.out.println(hpyp.root.score(hpyp.like));
        System.out.println(Arrays.toString(hpyp.scoreByDepth()));
        
        for (int j = 0; j < 100; j++) {
            hpyp.sample(false);
            System.out.println(j);//+ ", " + hpyp.score());
        }
    }
}
