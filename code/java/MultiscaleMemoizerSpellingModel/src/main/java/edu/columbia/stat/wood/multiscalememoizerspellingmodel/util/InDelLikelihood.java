/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class InDelLikelihood implements Likelihood {

    @Override
    public double probability(int observation, int parameter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int generate(int parameter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // states are 0 - begin
    //            1 - match
    //            2 - insert
    //            3 - delete
    //            4 - switch
    //            5 - end
    public double[][] transitionMatrix;

    private void initTransitionMatrix() {
        transitionMatrix = new double[6][];
        transitionMatrix[0] = new double[]{0d, 0.25, 0.25, 0.25, 0.25, 0d};
        transitionMatrix[1] = new double[]{0d, 0.5, 0.1, 0.1, 0.2, 0.1};
        transitionMatrix[2] = new double[]{0d, 0.65, 0.1, 0d, 0.1, 0.15};
        transitionMatrix[3] = new double[]{0d, 0.65, 0d, 0.1, 0.1, 0.15};
        transitionMatrix[4] = new double[]{0d, 0.5, 0.1, 0.1, 0.2, 0.1};
        transitionMatrix[5] = new double[]{0d, 0d, 0d, 0d, 0d, 1d};
    }

    // Using the HMM model for sequence alignment just as a start.  The emission
    // model for each state will be uniform over all types.
    public double probability(byte[] read, byte[] reference, int n) {
        initTransitionMatrix();
        return memoizedProbability(read, reference, n);
    }

    private double memoizedProbability(byte[] read, byte[] reference, int n) {
        return memoizedProbability(read, reference, n, read.length - 1, reference.length - 1, 5, new HashMap<Key,Double>());
    }

    private double memoizedProbability(byte[] read,
            byte[] reference,
            int state_index,
            int read_index,
            int reference_index,
            int state,
            HashMap<Key, Double> probs) {
            
        Key key = new Key(state_index, read_index, reference_index, state);
        Double prob = probs.get(key);

        if (prob == null) {
            prob = probability(read, reference, state_index, read_index, reference_index, state, probs);
            probs.put(key, prob);
        }

        return prob;
    }

    private double probability(byte[] read,
            byte[] reference,
            int state_index,
            int read_index,
            int reference_index,
            int state,
            HashMap<Key,Double> probs) {

        if (reference_index > state_index) {
            return 0d;
        } else if (read_index > state_index) {
            return 0d;
        } else if (reference_index < -1) {
            return 0d;
        } else if (read_index < -1) {
            return 0d;
        } else if (state_index < -1) {
            return 0d;
        } else if (read_index == -1 && reference_index == -1) {
            if (state_index == -1 && state == 0) {
                return 1d;
            } else {
                return 0d;
            }
        } else {
            double prob = 0d;
            if (state == 0) {
                return 0d;
            } else if (state == 5) {
                for (int i = 0; i < 6; i++) {
                    prob += memoizedProbability(read, reference, state_index - 1, read_index, reference_index, i, probs) * transitionMatrix[i][state];
                }
                return prob;
            } else {
                double emissionProbability = emissionProbability(read, read_index, reference, reference_index, state);
                if (emissionProbability > 0d) {
                    switch (state) {
                        case 1:
                            for (int i = 0; i < 6; i++) {
                                prob += memoizedProbability(read, reference, state_index - 1, read_index - 1, reference_index - 1, i, probs) * transitionMatrix[i][state];
                            }
                            return prob * emissionProbability;
                        case 2:
                            for (int i = 0; i < 6; i++) {
                                prob += memoizedProbability(read, reference, state_index - 1, read_index - 1, reference_index, i, probs) * transitionMatrix[i][state];
                            }
                            return prob * emissionProbability;
                        case 3:
                            for (int i = 0; i < 6; i++) {
                                prob += memoizedProbability(read, reference, state_index - 1, read_index, reference_index - 1, i, probs) * transitionMatrix[i][state];
                            }
                            return prob * emissionProbability;
                        case 4:
                            for (int i = 0; i < 6; i++) {
                                prob += memoizedProbability(read, reference, state_index - 1, read_index - 1, reference_index - 1, i, probs) * transitionMatrix[i][state];
                            }
                            return prob * emissionProbability;
                        default:
                            throw new RuntimeException("should be no other states");
                    }
                } else {
                    return 0d;
                }
            }
        }
        
    }

    private double emissionProbability(byte[] read, int read_index, byte[] reference, int reference_index, int state) {
        if (reference_index == -1) {
            if (state == 2) {
                return 1d;
            } else {
                return 0d;
            }
        } else if (read_index == -1) {
            if (state == 3) {
                return 1d;
            } else {
                return 0d;
            }
        } else if (state == 1) {
            if (read[read_index] == reference[reference_index]) {
                return 1d;
            } else {
                return 0d;
            }
        } else if (state == 4) {
            if (read[read_index] != reference[reference_index]) {
                return 1d;
            } else {
                return 0d;
            }
        } else {
            return 1d;
        }
    }

    public static void main(String[] args) {
        InDelLikelihood like = new InDelLikelihood();
        byte[] b1 = new byte[]{6, 4, 3, 7, 2, 1, 5, 3, 2};
        byte[] b2 = new byte[]{6, 4, 2, 7, 2, 1, 2, 2, 3, 1};
        byte[] b3 = new byte[]{6, 4, 3, 7};
        byte[] b4 = new byte[]{6, 4, 9};

        System.out.println(like.probability(b1, b2, 9));
    }

    // private class just for memoization purposes
    private class Key {

        private int state_index;
        private int read_index;
        private int reference_index;
        private int state;

        public Key(int state_index, int read_index, int reference_index, int state){
            this.state_index = state_index;
            this.read_index = read_index;
            this.reference_index = reference_index;
            this.state = state;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 31 * hash + this.state_index;
            hash = 31 * hash + this.read_index;
            hash = 31 * hash + this.reference_index;
            hash = 31 * hash + this.state;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else if (o.getClass() == getClass()) {
                Key cast_object = (Key) o;
                return cast_object.read_index == read_index && cast_object.state == state && cast_object.state_index == state_index && cast_object.reference_index == reference_index;
            } else {
                return false;
            }
        }
    }
}
