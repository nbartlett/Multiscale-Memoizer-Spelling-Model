/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.HPYP;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class InDelLikelihood implements Likelihood {

    public static int maxSizeLookup = 1000000;
    private Key key = new Key();
    private KeySum keySum = new KeySum();
    private HashMap<Key, Double> logProbLookup = new HashMap<Key, Double>(2000000);
    private HashMap<KeySum, Double> probSumLookup = new HashMap<KeySum, Double>(2000000);
    private int alphabetSize;
    public double lambda_i = 0.2;
    public double lambda_d = 0.05;
    public double lambda_s = 0.2;
    private int maxEdits;
    private Random rng;

    public InDelLikelihood(int maxEdits, int alphabetSize, Random rng) {
        this.maxEdits = maxEdits;
        this.rng = rng;
        this.alphabetSize = alphabetSize;
    }

    @Override
    public void sample(HPYP hpyp) {
        double std = 0.07;
        int numberProposals = 5;
        double current;
        double score = hpyp.score();
        double pscore;

        for (int i = 0; i < numberProposals; i++) {
            current = lambda_i;
            lambda_i += std * Util.rng.nextGaussian();

            if (lambda_i >= 0.99 || lambda_i <= 0.01) {
                lambda_i = current;
                continue;
            }

            clear();
            pscore = hpyp.score();
            if (Util.rng.nextDouble() <= Math.exp(pscore - score)) {
                score = pscore;
            } else {
                lambda_i = current;
            }
        }

        for (int i = 0; i < numberProposals; i++) {
            current = lambda_s;
            lambda_s += std * Util.rng.nextGaussian();

            if (lambda_s >= 0.99 || lambda_s <= 0.01) {
                lambda_s = current;
                continue;
            }

            clear();
            pscore = hpyp.score();
            if (Util.rng.nextDouble() <= Math.exp(pscore - score)) {
                score = pscore;
            } else {
                lambda_s = current;
            }
        }

        for (int i = 0; i < numberProposals; i++) {
            current = lambda_d;
            lambda_d += std * Util.rng.nextGaussian();

            if (lambda_d >= 0.99 || lambda_d <= 0.01) {
                lambda_d = current;
                continue;
            }

            clear();
            pscore = hpyp.score();
            if (Util.rng.nextDouble() <= Math.exp(pscore - score)) {
                score = pscore;
            } else {
                lambda_d = current;
                clear();
            }
        }
    }

    public void clear() {
        logProbLookup.clear();
        probSumLookup.clear();
    }

    public int lookupSize() {
        return logProbLookup.size();
    }
    private double[][][] probs = new double[100][100][25];

    @Override
    public double logProb(int[] reference, int[] read) {

        key._1 = reference;
        key._2 = read;
        Double value = logProbLookup.get(key);

        if (value == null) {
            if (logProbLookup.size() > maxSizeLookup) {
                cleanLogProb();
            }

            Key key = new Key();
            key._1 = reference;
            key._2 = read;

            for (int refConsumed = 0; refConsumed <= reference.length; refConsumed++) {

                int minReadIndex = Math.max(refConsumed - maxEdits, 0);
                int maxReadIndex = Math.min(refConsumed + maxEdits, read.length);

                for (int readConsumed = minReadIndex; readConsumed <= maxReadIndex; readConsumed++) {
                    for (int edits = 0; edits <= maxEdits; edits++) {
                        if (refConsumed == 0 && readConsumed == 0 && edits == 0) {
                            probs[0][0][0] = 1d;
                        } else {
                            boolean i_a, d_a, s_a, m_a;
                            if (edits > 0) {
                                i_a = readConsumed > 0;
                                d_a = refConsumed > 0;
                                s_a = i_a && d_a;
                            } else {
                                i_a = false;
                                d_a = false;
                                s_a = false;
                            }
                            m_a = readConsumed > 0 && refConsumed > 0 && reference[refConsumed - 1] == read[readConsumed - 1];

                            double p = 0d;
                            if (s_a) {
                                p += lambda_s / (double) alphabetSize * probs[refConsumed - 1][readConsumed - 1][edits - 1];
                            }
                            if (i_a) {
                                p += lambda_i / (double) alphabetSize * probs[refConsumed][readConsumed - 1][edits - 1];

                            }
                            if (d_a) {
                                p += lambda_d * probs[refConsumed - 1][readConsumed][edits - 1];
                            }
                            if (m_a) {
                                p += probs[refConsumed - 1][readConsumed - 1][edits];
                            }

                            probs[refConsumed][readConsumed][edits] = p;
                        }
                    }
                }
            }

            double p = 0d;
            for (int edits = 0; edits <= maxEdits; edits++) {
                p += probs[reference.length][read.length][edits];
            }

            logProbLookup.put(key, value = new Double(Math.log(p / probSum(reference))));
        }

        return value;

    }
    private double[][] probSum = new double[100][25];

    public double probSum(int[] reference) {

        keySum.key = reference;
        Double value = probSumLookup.get(keySum);

        if (value == null) {

            if (probSumLookup.size() > maxSizeLookup) {
                cleanProbSum();
            }

            KeySum key = new KeySum();
            key.key = reference;

            for (int refConsumed = 0; refConsumed <= reference.length; refConsumed++) {
                for (int edits = 0; edits <= maxEdits; edits++) {
                    double p = 0d;
                    if (refConsumed == 0 && edits == 0) {
                        p = 1d;
                    } else {
                        if (refConsumed > 0) {
                            p += probSum[refConsumed - 1][edits];
                        }
                        if (refConsumed > 0 && edits > 0) {
                            p += (lambda_s + lambda_d) * probSum[refConsumed - 1][edits - 1];
                        }
                        if (edits > 0) {
                            p += lambda_i * probSum[refConsumed][edits - 1];
                        }
                    }
                    probSum[refConsumed][edits] = p;
                }
            }

            double p = 0d;
            for (int edits = 0; edits <= maxEdits; edits++) {
                p += probSum[reference.length][edits];
            }

            probSumLookup.put(key, value = new Double(p));
        }

        return value;
    }

    public void cleanLogProb() {
        logProbLookup.clear();
        /*ArrayList<Entry<Key, DoubleTouchesPair>> list = new ArrayList<Entry<Key, DoubleTouchesPair>>(logProbLookup.entrySet());
        Collections.sort(list, new Comp());
        
        int size = logProbLookup.size();
        int targetSize = maxSizeLookup / 2;
        Iterator<Entry<Key, DoubleTouchesPair>> iterator = list.iterator();
        while (size-- > targetSize) {
        logProbLookup.remove(iterator.next().getKey());
        }
        
        while (iterator.hasNext()) {
        iterator.next().getValue().touches = 0;
        }*/
    }

    public void cleanProbSum() {
        probSumLookup.clear();
        /*ArrayList<Entry<KeySum, DoubleTouchesPair>> list = new ArrayList<Entry<KeySum, DoubleTouchesPair>>(probSumLookup.entrySet());
        Collections.sort(list, new Comp());
        
        int size = probSumLookup.size();
        int targetSize = maxSizeLookup / 2;
        Iterator<Entry<KeySum, DoubleTouchesPair>> iterator = list.iterator();
        while (size-- > targetSize) {
        probSumLookup.remove(iterator.next().getKey());
        }
        
        while (iterator.hasNext()) {
        iterator.next().getValue().touches = 0;
        }*/
    }

    private class Key {

        public int[] _1;
        public int[] _2;

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Arrays.hashCode(this._1);
            hash = 167 * hash + Arrays.hashCode(this._2);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else if (o.getClass() == getClass()) {
                return Arrays.equals(((Key) o)._1, _1) && Arrays.equals(((Key) o)._2, _2);
            } else {
                return false;
            }
        }
    }

    private class KeySum {

        public int[] key;

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else if (o.getClass() == getClass()) {
                return Arrays.equals(((KeySum) o).key, key);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Arrays.hashCode(this.key);
            return hash;
        }
    }


    /*
     * 
     * private double phi_m(int ref, int read) {
    if (ref < alphabetSize && ref == read) {
    return 1d;
    } else {
    return 0d;
    }
    }
    
    private double phi_i(int read) {
    if (read < alphabetSize) {
    return 1d / (double) alphabetSize;
    } else {
    return 0d;
    }
    }
    
    private double phi_d(int ref) {
    if (ref < alphabetSize) {
    return 1d / (double) alphabetSize;
    } else {
    return 0d;
    }
    }
    
    private double phi_s(int ref, int read) {
    if (ref < alphabetSize && read < alphabetSize) {
    return 1d / (double) alphabetSize;
    } else {
    return 0d;
    }
    }
    
    private byte[][][] logProbContains = new byte[25][25][10];
    private double[][][] logProbs = new double[25][25][10];
    private byte[][] logSumContains;
    private double[][] logSums;
    
    public double logProb(int[] reference, int[] read) {
    key._1 = reference;
    key._2 = read;
    DoubleTouchesPair logProb = logProbLookup.get(key);
    
    if (logProb == null) {
    
    if (logProbLookup.size() > maxSizeLookup) {
    cleanLogProb();
    }
    
    Key key = new Key();
    key._1 = reference;
    key._2 = read;
    
    //logProbContains = new byte[reference.length + 1][read.length + 1][maxEdits + 1];
    //logProbs = new double[reference.length + 1][read.length + 1][maxEdits + 1];
    //System.arraycopy(zeros_b,0,logProbContains,0,(reference.length + 1)*(read.length + 1)*(maxEdits + 1));
    //System.arraycopy(zeros_d,0,logProbs,0,(reference.length + 1)*(read.length + 1)*(maxEdits + 1));
    for (int i = 0; i < 25; i++) {
    for (int j = 0; j < 25; j++) {
    Arrays.fill(logProbContains[i][j], (byte) 0);
    //Arrays.fill(logProbs[i][j], 0);
    }
    }
    
    logProbLookup.put(key, logProb = new DoubleTouchesPair(logProb(reference, read, maxEdits, 0, 0, 0) - logSumProb(reference, maxEdits)));
    }
    logProb.touches++;
    
    return logProb.d;
    }
    
    
     * private double logProb(int[] reference, int[] read, int maxEdits, int refIndex, int readIndex, int edits) {
    if (logProbContains[refIndex][readIndex][edits] == 1) {
    return logProbs[refIndex][readIndex][edits];
    } else {
    boolean consumedRef = refIndex == reference.length;
    boolean consumedRead = readIndex == read.length;
    
    double logProb;
    if (consumedRef && consumedRead) {
    logProb = 0d;
    } else {
    logProb = Double.NEGATIVE_INFINITY;
    
    boolean s_a, i_a, d_a, m_a;
    if (edits < maxEdits) {
    s_a = !consumedRef && !consumedRead;
    i_a = !consumedRead;
    d_a = !consumedRef;
    } else {
    s_a = false;
    i_a = false;
    d_a = false;
    }
    m_a = !consumedRef && !consumedRead && phi_m(reference[refIndex], read[readIndex]) > 0d;
    
    if (m_a) {
    logProb = addLogs(logProb, Math.log(phi_m(reference[refIndex], read[readIndex])) + logProb(reference, read, maxEdits, refIndex + 1, readIndex + 1, edits));
    }
    
    if (s_a) {
    logProb = addLogs(logProb, Math.log(lambda_s) + Math.log(phi_s(reference[refIndex], read[readIndex])) + logProb(reference, read, maxEdits, refIndex + 1, readIndex + 1, edits + 1));
    }
    
    if (i_a) {
    logProb = addLogs(logProb, Math.log(lambda_i) + Math.log(phi_i(read[readIndex])) + logProb(reference, read, maxEdits, refIndex, readIndex + 1, edits + 1));
    }
    
    if (d_a) {
    logProb = addLogs(logProb, Math.log(lambda_d) + Math.log(phi_d(reference[refIndex])) + logProb(reference, read, maxEdits, refIndex + 1, readIndex, edits + 1));
    }
    }
    
    logProbContains[refIndex][readIndex][edits] = 1;
    logProbs[refIndex][readIndex][edits] = logProb;
    return logProb;
    }
    }
    
     * 
    private double logSumProb(int[] reference, int maxEdits) {
    keySum.key = reference;
    keySum.maxEdits = maxEdits;
    DoubleTouchesPair logSum = logSumLookup.get(keySum);
    
    if (logSum == null) {
    if (logSumLookup.size() > maxSizeLookup) {
    cleanLogSum();
    }
    
    KeySum keySum = new KeySum();
    keySum.key = reference;
    keySum.maxEdits = maxEdits;
    
    logSumContains = new byte[reference.length + 1][maxEdits + 1];
    logSums = new double[reference.length + 1][maxEdits + 1];
    
    logSumLookup.put(keySum, logSum = new DoubleTouchesPair(logSumProb(reference, maxEdits, alphabetSize, 0, 0)));
    }
    logSum.touches++;
    
    return logSum.d;
    }
    
    private double logSumProb(int[] reference, int maxEdits, int alphabetSize, int refIndex, int edits) {
    if (logSumContains[refIndex][edits] == 1) {
    return logSums[refIndex][edits];
    } else {
    boolean consumedRef = refIndex == reference.length;
    double logSum;
    if (consumedRef) {
    logSum = 0d;
    } else {
    logSum = Double.NEGATIVE_INFINITY;
    }
    
    boolean s_a, i_a, d_a, m_a;
    if (edits < maxEdits) {
    s_a = !consumedRef;
    i_a = true;
    d_a = !consumedRef;
    } else {
    s_a = false;
    i_a = false;
    d_a = false;
    }
    m_a = !consumedRef;
    
    if (m_a) {
    logSum = addLogs(logSum, logSumProb(reference, maxEdits, alphabetSize, refIndex + 1, edits));
    }
    
    if (s_a) {
    logSum = addLogs(logSum, Math.log(lambda_s) + logSumProb(reference, maxEdits, alphabetSize, refIndex + 1, edits + 1));
    }
    
    if (i_a) {
    logSum = addLogs(logSum, Math.log(lambda_i) + logSumProb(reference, maxEdits, alphabetSize, refIndex, edits + 1));
    }
    
    if (d_a) {
    logSum = addLogs(logSum, Math.log(lambda_d) + Math.log(phi_d(reference[refIndex])) + logSumProb(reference, maxEdits, alphabetSize, refIndex + 1, edits + 1));
    }
    
    logSumContains[refIndex][edits] = 1;
    logSums[refIndex][edits] = logSum;
    return logSum;
    }
    }
    
    public ArrayList<int[]> samples(int[] reference, int sampleCount) {
    logSumProb(reference, maxEdits);
    ArrayList<int[]> list = new ArrayList<int[]>(sampleCount);
    for (int i = 0; i < sampleCount; i++) {
    list.add(sample(reference, maxEdits, rng));
    }
    return list;
    }
    
    private int[] sample(int[] reference, int maxEdits, Random rng) {
    int[] rawSample = new int[reference.length + maxEdits];
    int sampleLength = sample(reference, maxEdits, rng, 0, 0, rawSample, 0);
    int[] sample = new int[sampleLength];
    System.arraycopy(rawSample, 0, sample, 0, sampleLength);
    return sample;
    }
    
    private int sample(int[] reference, int maxEdits, Random rng, int refIndex, int edits, int[] sample, int sampleIndex) {
    boolean consumedRef = refIndex == reference.length;
    double log_r = Math.log(rng.nextDouble());
    
    double logCuSum;
    double logDenom = logSums[refIndex][edits];
    
    if (consumedRef) {
    logCuSum = -logDenom;
    } else {
    logCuSum = Double.NEGATIVE_INFINITY;
    }
    
    if (logCuSum > log_r) {
    return sampleIndex;
    } else {
    boolean s_a, i_a, d_a, m_a;
    if (edits < maxEdits) {
    s_a = !consumedRef;
    i_a = true;
    d_a = !consumedRef;
    } else {
    s_a = false;
    i_a = false;
    d_a = false;
    }
    m_a = !consumedRef;
    
    if (m_a) {
    logCuSum = addLogs(logCuSum, logSums[refIndex + 1][edits] - logDenom);
    if (logCuSum > log_r) {
    sample[sampleIndex] = sampleMatch(reference[refIndex]);
    return sample(reference, maxEdits, rng, refIndex + 1, edits, sample, sampleIndex + 1);
    }
    }
    
    if (s_a) {
    logCuSum = addLogs(logCuSum, Math.log(lambda_s) + logSums[refIndex + 1][edits + 1] - logDenom);
    if (logCuSum > log_r) {
    sample[sampleIndex] = sampleSubstitution(reference[refIndex]);
    return sample(reference, maxEdits, rng, refIndex + 1, edits + 1, sample, sampleIndex + 1);
    }
    }
    
    if (i_a) {
    logCuSum = addLogs(logCuSum, Math.log(lambda_i) + logSums[refIndex][edits + 1] - logDenom);
    if (logCuSum > log_r) {
    sample[sampleIndex] = sampleInsertion();
    return sample(reference, maxEdits, rng, refIndex, edits + 1, sample, sampleIndex + 1);
    }
    }
    
    if (d_a) {
    logCuSum = addLogs(logCuSum, Math.log(lambda_d) + Math.log(phi_d(reference[refIndex])) + logSums[refIndex + 1][edits + 1] - logDenom);
    if (logCuSum > log_r) {
    return sample(reference, maxEdits, rng, refIndex + 1, edits + 1, sample, sampleIndex);
    }
    }
    
    throw new RuntimeException("If we get here something has gone wrong."
    + "  All probability mass should be exhausted before here");
    }
    }
    
    private int sampleMatch(int ref) {
    if (ref < alphabetSize) {
    return ref;
    } else {
    throw new IllegalArgumentException("Reference character must be less than alphabet size");
    }
    }
    
    private int sampleSubstitution(int reference) {
    if (reference < alphabetSize) {
    double cuSum = 0d;
    double r = rng.nextDouble();
    for (int i = 0; i < alphabetSize; i++) {
    cuSum += phi_s(reference, i);
    if (cuSum > r) {
    return i;
    }
    }
    throw new RuntimeException("If we get here something has gone wrong."
    + "  All probability mass should be exhausted before here");
    } else {
    throw new IllegalArgumentException("Reference character must be less than alphabet size");
    }
    }
    
    private int sampleInsertion() {
    double cuSum = 0d;
    double r = rng.nextDouble();
    for (int i = 0; i < alphabetSize; i++) {
    cuSum += phi_i(i);
    if (cuSum > r) {
    return i;
    }
    }
    throw new RuntimeException("should never get to here");
    }
    
    public static final double addLogs(double logA, double logB) {
    if (Double.isInfinite(logA) && Double.isInfinite(logB)) {
    if (logA < 0d && logB < 0d) {
    return Double.NEGATIVE_INFINITY;
    } else {
    throw new RuntimeException("basically shouldn't happen");
    }
    } else if (logA > logB) {
    return Math.log(1.0 + Math.exp(logB - logA)) + logA;
    } else {
    return Math.log(1.0 + Math.exp(logA - logB)) + logB;
    }
    }
    
    
     */
    public static void main(String[] args) {
        int[] ref = new int[]{0, 1, 2, 3};

        int[][] r = new int[33][];

        // match
        r[0] = new int[]{0, 1, 2, 3};

        // deletions
        r[1] = new int[]{1, 2, 3};
        r[2] = new int[]{0, 2, 3};
        r[3] = new int[]{0, 1, 3};
        r[4] = new int[]{0, 1, 2};

        // substitutions
        r[5] = new int[]{1, 1, 2, 3};
        r[6] = new int[]{2, 1, 2, 3};
        r[7] = new int[]{3, 1, 2, 3};

        r[8] = new int[]{0, 0, 2, 3};
        r[9] = new int[]{0, 2, 2, 3};
        r[10] = new int[]{0, 3, 2, 3};

        r[11] = new int[]{0, 1, 0, 3};
        r[12] = new int[]{0, 1, 1, 3};
        r[13] = new int[]{0, 1, 3, 3};

        r[14] = new int[]{0, 1, 2, 0};
        r[15] = new int[]{0, 1, 2, 1};
        r[16] = new int[]{0, 1, 2, 2};

        //insertions
        r[17] = new int[]{0, 0, 1, 2, 3};
        r[18] = new int[]{1, 0, 1, 2, 3};
        r[19] = new int[]{2, 0, 1, 2, 3};
        r[20] = new int[]{3, 0, 1, 2, 3};

        r[21] = new int[]{0, 1, 1, 2, 3};
        r[22] = new int[]{0, 2, 1, 2, 3};
        r[23] = new int[]{0, 3, 1, 2, 3};

        r[24] = new int[]{0, 1, 0, 2, 3};
        r[25] = new int[]{0, 1, 2, 2, 3};
        r[26] = new int[]{0, 1, 3, 2, 3};

        r[27] = new int[]{0, 1, 2, 0, 3};
        r[28] = new int[]{0, 1, 2, 1, 3};
        r[29] = new int[]{0, 1, 2, 3, 3};

        r[30] = new int[]{0, 1, 2, 3, 0};
        r[31] = new int[]{0, 1, 2, 3, 1};
        r[32] = new int[]{0, 1, 2, 3, 2};



        double s1 = 0d, s2 = 0d;


        InDelLikelihood like = new InDelLikelihood(5, 4, Util.rng);

        for (int[] read : r) {
            s1 += Math.exp(like.logProb(ref, read));
            System.out.println(s1);
        }




        System.out.println("S1 = " + s1);
        System.out.println("S2 = " + s2);

        //int[] read0 = new int[]{1, 2, 3, 0, 0};
        //int[] read1 = new int[]{1, 2, 2, 3, 0, 0, 1};
        //int[] read2 = new int[]{1, 0, 1, 3, 0};


        /*
        System.out.println(Math.exp(like.logProb(ref, read0)));
        System.out.println(Math.exp(like.logProb(ref, read1)));
        System.out.println(Math.exp(like.logProb(ref, read2)));
        
        System.out.println(like.logProb2(ref, read0));
        System.out.println(like.logProb2(ref, read1));
        System.out.println(like.logProb2(ref, read2));*/




        //InDelLikelihood like = new InDelLikelihood(5, 26, new Random());
        //int[] ref = new int[]{0, 1, 2, 3, 4, 5};


        int[] read = new int[5];

        for (int i = 0; i < 60000; i++) {
            read[0] = i / 10000;
            read[1] = (i % 10000) / 1000;
            read[2] = (i % 1000) / 100;
            read[3] = (i % 100) / 10;
            read[4] = (i % 10);

            like.logProb(read, ref);
        }


        for (int i = 0; i < 60000; i++) {
            read[0] = i / 10000;
            read[1] = (i % 10000) / 1000;
            read[2] = (i % 1000) / 100;
            read[3] = (i % 100) / 10;
            read[4] = (i % 10);

            like.logProb(read, ref);
        }


        read = new int[]{19, 8, 13, 24};
        ref = new int[]{19, 8, 13, 24};

        System.out.println(like.logProb(ref, read));



        /*
        System.out.println(like.logSumProb(ref, 15));
        System.out.println(Math.exp(like.logSumProb(ref, 15)));
        
        System.out.println();
        
        for (int i = 0; i < like.logSums.length; i++) {
        System.out.println(Arrays.toString(like.logSums[i]));
        }
        System.out.println();
        
        ArrayList<int[]> samples = like.samples(ref, 100);
        for (int[] sample : samples) {
        System.out.println(Arrays.toString(sample) + ", " + Math.exp(like.logProb(ref, sample)));
        }
         */
    }
}
