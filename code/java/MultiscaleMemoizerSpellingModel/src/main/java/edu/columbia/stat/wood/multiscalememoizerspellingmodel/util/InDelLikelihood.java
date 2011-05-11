/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class InDelLikelihood implements Likelihood {

    public static int maxSizeLookup = 2000000;
    
    private Key key = new Key();
    private KeySum keySum = new KeySum();

    private HashMap<Key,DoubleTouchesPair> logProbLookup = new HashMap<Key,DoubleTouchesPair>(2000000);
    private HashMap<KeySum,DoubleTouchesPair> logSumLookup = new HashMap<KeySum,DoubleTouchesPair>(2000000);

    private int alphabetSize = 26;

    private double lambda_i = 0.4;
    private double lambda_d = 0.4;
    private double lambda_s = 0.1;

    private int maxEdits;
    private Random rng;

    public InDelLikelihood(int maxEdits, Random rng) {
        this.maxEdits = maxEdits;
        this.rng = rng;
    }

    private double phi_m(int ref, int read) {
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

    private byte[][][] logProbContains;
    private double[][][] logProbs;
    private byte[][] logSumContains;
    private double[][] logSums;

    @Override
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

            logProbContains = new byte[reference.length + 1][read.length + 1][maxEdits + 1];
            logProbs = new double[reference.length + 1][read.length + 1][maxEdits + 1];
            
            logProbLookup.put(key, logProb = new DoubleTouchesPair(logProb(reference, read, maxEdits, 0, 0, 0) - logSumProb(reference, maxEdits)));
        }
        logProb.touches++;

        return logProb.d;
    }

    public int lookupSize() {
        return logProbLookup.size();
    }

    private double logProb(int[] reference, int[] read, int maxEdits, int refIndex, int readIndex, int edits) {
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

    public ArrayList<int[]> samples(int[] reference,int sampleCount) {
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
                logCuSum = addLogs(logCuSum, Math.log(lambda_s) + logSums[refIndex + 1][edits + 1] -logDenom);
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
            
            throw new RuntimeException("If we get here something has gone wrong." +
                    "  All probability mass should be exhausted before here");
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
            throw new RuntimeException("If we get here something has gone wrong." +
                    "  All probability mass should be exhausted before here");
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

    public void cleanLogProb() {
        ArrayList<Entry<Key,DoubleTouchesPair>> list = new ArrayList<Entry<Key,DoubleTouchesPair>>(logProbLookup.entrySet());
        Collections.sort(list, new Comp());
        
        int size = logProbLookup.size();
        int targetSize = maxSizeLookup / 2;
        Iterator<Entry<Key,DoubleTouchesPair>> iterator = list.iterator();
        while (size-- > targetSize) {
            logProbLookup.remove(iterator.next().getKey());
        }
        
        while(iterator.hasNext()) {
            iterator.next().getValue().touches = 0;
        }
    }
    
    public void cleanLogSum() {
        ArrayList<Entry<KeySum,DoubleTouchesPair>> list = new ArrayList<Entry<KeySum,DoubleTouchesPair>>(logSumLookup.entrySet());
        Collections.sort(list, new Comp());
        
        int size = logProbLookup.size();
        int targetSize = maxSizeLookup / 2;
        Iterator<Entry<KeySum,DoubleTouchesPair>> iterator = list.iterator();
        while (size-- > targetSize) {
            logSumLookup.remove(iterator.next().getKey());
        }   
        
        while(iterator.hasNext()) {
            iterator.next().getValue().touches = 0;
        }
    }
    
    private class Comp implements Comparator<Entry<?,DoubleTouchesPair>> {
        @Override
        public int compare(Entry<?, DoubleTouchesPair> t, Entry<?, DoubleTouchesPair> t1) {
            if (t.getValue().touches >= t1.getValue().touches) return 1;
            else return 0;
        }
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
                return Arrays.equals(((Key) o)._1,_1) && Arrays.equals(((Key) o)._2,_2);
            } else {
                return false;
            }
        }
    }

    private class KeySum {
        public int[] key;
        public int maxEdits;

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else if (o.getClass() == getClass()) {
                return Arrays.equals(((KeySum) o).key,key) && ((KeySum) o).maxEdits == maxEdits;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + Arrays.hashCode(this.key);
            hash = 89 * hash + this.maxEdits;
            return hash;
        }
    }
    
    private class DoubleTouchesPair {
        public double d;
        public int touches;
        
        public DoubleTouchesPair (double d) {
            this.d = d;
            touches = 0;
        }
    }

    public static void main(String[] args) {
        int[] ref = new int[]{1, 2, 3, 4, 5};
        //int[] read = new int[]{1, 2, 3, 4, 5, 6, 8, 9};

        InDelLikelihood like = new InDelLikelihood(5, new Random());
        
        int[] read = new int[5];
        for (int i = 0; i < 60000; i++) {
            read[0] = i / 10000;
            read[1] = (i % 10000) / 1000 ;
            read[2] = (i % 1000) / 100 ;
            read[3] = (i % 100) / 10 ;
            read[4] = (i % 10);
            
            like.logProb(read, ref);
        }
        
        
        for (int i = 0; i < 60000; i++) {
            read[0] = i / 10000;
            read[1] = (i % 10000) / 1000 ;
            read[2] = (i % 1000) / 100 ;
            read[3] = (i % 100) / 10 ;
            read[4] = (i % 10);
            
            like.logProb(read, ref);
        }
        
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
