/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.experiments;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.HPYP;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.Word;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.ByteInDelLikelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.ByteWordIterator;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Distribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.UniformIntegerDistribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author nicholasbartlett
 */
public class ByteWordExperiment {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        HPYP hpyp = new HPYP(new ByteWordUniformDistribution());
//        hpyp.like = new ByteInDelLikelihood();

        File in = new File("/Users/nicholasbartlett/Desktop/small_test_data.txt");

        int trainingSize = 2000;
        Word[] test = new Word[1000];

        ByteWordIterator bwi = new ByteWordIterator(in);

        int d = 2;
        Word[] context = new Word[2];
        Word w;
        int index = 0;
        while (bwi.hasNext()) {
            if (trainingSize-- > 0) {
                hpyp.seatInit(context, w = new Word(bwi.next()));

                for (int i = 1; i < d; i++) {
                    context[i - 1] = context[i];
                }
                context[d - 1] = w;
            } else {
                if (index < test.length) {
                    test[index++] = w = new Word(bwi.next());

                    for (int i = 1; i < d; i++) {
                        context[i - 1] = context[i];
                    }
                    context[d - 1] = w;



                } else {
                    break;
                }

            }
        }

        for (int i = 0; i < 100; i++) {
            hpyp.sample(false);
            System.out.println(hpyp.score());
        }


    }

    public static class ByteWordUniformDistribution implements Distribution<int[]> {

        private int[] counts = new int[256];

        @Override
        public double logProbability(int[] type) {
            for (int i : type) {
                if (i != 0 && i != 1) {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            return -Math.log(256d);
        }

        @Override
        public int[] generate() {
            int b = (int) (256 * Util.rng.nextDouble());

            int[] sample = new int[8];
            int index = 7;
            while (b > 0) {
                sample[index--] = (b & 1);
                b >>= 1;
            }

            return sample;
        }

        @Override
        public void incrementObservationCount(int[] observation) {
            int value = 0;
            int power = 1;

            for (int i = 7; i > -1; i--) {
                if (observation[i] == 1) {
                    value += power;
                }
                power <<= 1;
            }

            counts[value]++;
        }

        @Override
        public void decrementObservationCount(int[] observation) {
            int value = 0;
            int power = 1;

            for (int i = 7; i > -1; i--) {
                if (observation[i] == 1) {
                    value += power;
                }
                power <<= 1;
            }
        
            counts[value]--;
        }

        @Override
        public double score() {
            double score = 0d;
            for (int i : counts) {
                score -= (double) i * Math.log(256d);
            }
            return score;
        }

        @Override
        public void sample() {
        }
    }
}
