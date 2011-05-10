/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class InDelLikelihood implements Likelihood {

    private Key key = new Key();
    private KeySum keySum = new KeySum();

    private HashMap<Key,Double> logProbLookup = new HashMap<Key,Double>();
    private HashMap<KeySum,Double> logSumLookup = new HashMap<KeySum,Double>();

    private int alphabetSize = 26;

    private double lambda_i = 0.001;
    private double lambda_d = 0.001;
    private double lambda_s = 0.001;

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

    public double logProb(int[] reference, int[] read) {
        key._1 = reference;
        key._2 = read;
        Double logProb = logProbLookup.get(key);

        if (logProb == null) {

            if (logProbLookup.size() > 1000000) {
                logProbLookup.clear();
            }

            Key key = new Key();
            key._1 = reference;
            key._2 = read;

            logProbContains = new byte[reference.length + 1][read.length + 1][maxEdits + 1];
            logProbs = new double[reference.length + 1][read.length + 1][maxEdits + 1];
            
            logProbLookup.put(key, logProb = new Double(logProb(reference, read, maxEdits, 0, 0, 0) - logSumProb(reference, maxEdits)));
        }

        return logProb.doubleValue();
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
        Double logSum = logSumLookup.get(keySum);

        if (logSum == null) {
            if (logSumLookup.size() > 1000000) {
                logSumLookup.clear();
            }

            KeySum keySum = new KeySum();
            keySum.key = reference;
            keySum.maxEdits = maxEdits;

            logSumContains = new byte[reference.length + 1][maxEdits + 1];
            logSums = new double[reference.length + 1][maxEdits + 1];

            logSumLookup.put(keySum, logSum = logSumProb(reference, maxEdits, alphabetSize, 0, 0));
        }

        return logSum.doubleValue();
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

    public void clear() {
        logProbLookup = new HashMap<Key,Double>();
        logSumLookup = new HashMap<KeySum,Double>();
    }

    private class Key {
        public int[] _1;
        public int[] _2;

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + Arrays.hashCode(this._1);
            hash = 83 * hash + Arrays.hashCode(this._2);
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
    }

    public static void main(String[] args) {
        int[] ref = new int[]{1, 2, 3, 4, 5, 6, 7};
        int[] r2 = new int[]{1, 1, 1, 1, 1, 1, 1};
        int[] read = new int[]{1, 2, 3, 4};

        InDelLikelihood like = new InDelLikelihood(10, new Random());


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
    }
}
