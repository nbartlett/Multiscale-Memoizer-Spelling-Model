/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.experiments;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.Word;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp.HPYP;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.FileWordIterator;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.UniformIntegerDistribution;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

/**
 *
 * @author nicholasbartlett
 */
public class CharacterHPYPTest {

    public static HashMap<Word, Integer> dictionary = new HashMap<Word, Integer>();
    public static HashMap<Integer, Word> r_dictionary = new HashMap<Integer, Word>();

    // usage: depth corrupted_file out_file [original_file]
    public static void main(String[] args) throws FileNotFoundException, IOException {
        FileWordIterator fwi = null;
        FileWordIterator fwi_aiw = null;

        HPYP hpyp;
        int[] test;
        int[] testContext;
        int[] context = new int[0];
        
        int depth = Integer.parseInt(args[0]);
        
        File aiw = args.length > 3 ? new File(args[3]) : null;

        try {
            //fwi = new FileWordIterator(new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/aiw_spelling_corrupted_10.txt"));
            fwi = new FileWordIterator(new File(args[1]));

            Word word;
            Integer translation;

            while (fwi.hasNext()) {
                word = new Word(fwi.next());
                translation = dictionary.get(word);

                if (translation == null) {
                    translation = dictionary.size();
                    dictionary.put(word, translation);
                    r_dictionary.put(translation, word);
                }
            }
            
            if (aiw != null) {
                fwi.close();
                fwi = new FileWordIterator(aiw);
                while (fwi.hasNext()) {
                    word = new Word(fwi.next());
                    translation = dictionary.get(word);

                    if (translation == null) {
                        translation = dictionary.size();
                        dictionary.put(word, translation);
                        r_dictionary.put(translation, word);
                    }
                }
            }
            
            fwi.close();
            System.out.println(dictionary.size());

            //fwi = new FileWordIterator(new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/aiw_spelling_corrupted_10.txt"));
            fwi = new FileWordIterator(new File(args[1]));
            if (aiw != null)
                fwi_aiw = new FileWordIterator(aiw);
            
            
            hpyp = new HPYP(new UniformIntegerDistribution(1000000));

            int w;

            int trainingSize = 20000;
            test = new int[9759];
            testContext = new int[test.length];
            int j = 0;

            while (fwi.hasNext()) {

                if (trainingSize-- > 0) {
                    
                    hpyp.seat(context, w = dictionary.get(new Word(fwi.next())));

                    if (context.length < depth) {
                        int[] newContext = new int[context.length + 1];
                        System.arraycopy(context, 0, newContext, 0, context.length);
                        
                        if (aiw == null)
                            newContext[context.length] = w;
                        else 
                            newContext[context.length] = dictionary.get(new Word(fwi_aiw.next()));
                        
                        context = newContext;
                    } else {
                        for (int i = 1; i < depth; i++) {
                            context[i - 1] = context[i];
                        }
                        if (aiw == null)
                            context[depth - 1] = w;
                        else 
                            context[depth - 1] = dictionary.get(new Word(fwi_aiw.next()));
                    }
                } else {
                    if (aiw != null)
                        testContext[j] = dictionary.get(new Word(fwi_aiw.next()));
                    test[j++] = dictionary.get(new Word(fwi.next()));
                }
            }
        } finally {
            if (fwi != null) {
                fwi.close();
            }
        }

        //PrintStream ps = new PrintStream(new FileOutputStream(new File("/Users/nicholasbartlett/Desktop/aiw_spelling_corrupted_10_5.out")));
        PrintStream ps = new PrintStream(new FileOutputStream(new File(args[2])));
        if (aiw == null) {
            for (int i = 0; i < 2000; i++) {
                hpyp.sample();
                ps.print(hpyp.score());
                ps.print(", ");
                hpyp.score(context, test, depth, ps);
            }
        } else {
            for (int i = 0; i < 2000; i++) {
                int[] cxt = new int[depth];
                System.arraycopy(context,0,cxt,0,depth);
                
                hpyp.sample();
                ps.print(hpyp.score());
                ps.print(", ");
                
                int ind = 0;
                for (int word : test) {
                    ps.print(hpyp.prob(context, word) + ", ");
                    
                    for (int j = 1; j < depth; j++)
                        cxt[j-1] = cxt[j];
                    cxt[depth-1] = testContext[ind++];
                }
                ps.println();
            }
        }
    }

/*    private static class D implements Distribution<Integer> {

        private HPYP hpyp = new HPYP(new UniformIntegerDistribution(27,1));

        @Override
        public double logProbability(Integer type) {
            return hpyp.logProbability(r_dictionary.get(type).value);
        }

        @Override
        public Integer generate() {
            return dictionary.get(new Word(hpyp.generate()));
        }


        @Override
        public void incrementObservationCount(Integer observation) {
            hpyp.incrementObservationCount(r_dictionary.get(observation).value);
        }

        @Override
        public void decrementObservationCount(Integer observation) {
            hpyp.decrementObservationCount(r_dictionary.get(observation).value);
        }

        @Override
        public double score() {
            return hpyp.score();
        }

        @Override
        public void sample() {
            hpyp.sample();
        }
    }*/
}
