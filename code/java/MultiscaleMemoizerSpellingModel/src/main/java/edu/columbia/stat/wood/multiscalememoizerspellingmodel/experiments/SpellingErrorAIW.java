/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.experiments;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.HPYP;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.Word;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Distribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.FileWordIterator;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.InDelLikelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.UniformIntegerDistribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */
public class SpellingErrorAIW {

    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        File in;
        File out;
        File aiw;
        int trainingSize;
        Word[] test;
        boolean sampleLike;
        boolean sampleDiscConc;
        int iterations;
        
        if (args.length == 0) {
            in = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/aiw_spelling_corrupted_10.txt");
            out = new File("/Users/nicholasbartlett/Documents/np_bayes/Multiscale_Memoizer_Spelling_Model/output/aiw_spelling.out");
            aiw = new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/alice_in_wonderland.txt");
            trainingSize = 20000;
            test = new Word[0];
            sampleLike = true;
            sampleDiscConc = true;
            iterations = 10;
        } else {
            in = new File(args[0]);
            out = new File(args[1]);
            aiw = new File(args[2]);
            
            trainingSize = Integer.parseInt(args[3]);
            test = new Word[Integer.parseInt(args[4])];
            sampleLike = Boolean.parseBoolean(args[5]);
            iterations = Integer.parseInt(args[6]);
            sampleDiscConc = Boolean.parseBoolean(args[7]);
        }

        int d = 2;
        Word[] context = new Word[2];

        HPYP hpyp = new HPYP(new edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp.HPYP(new UniformIntegerDistribution(27, 1)));
        HashSet<Word> trueWords;
        
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
            
            fwi.close();
            
            fwi = new FileWordIterator(aiw);
            trueWords = new HashSet<Word>();
            
            while (fwi.hasNext()) {
                trueWords.add(new Word(fwi.next()));
            }                    
        } finally {
            if (fwi != null) {
                fwi.close();
            }
        }  
        
        PrintStream ps = new PrintStream(new FileOutputStream(out));
        long start_time;
        
        for (int i = 1; i < iterations; i++) {
            start_time = System.currentTimeMillis();           
            
            if (sampleDiscConc) {
                hpyp.sampleDiscountsConcentrations();
            }
           
            if (sampleLike) {
                if (i >= 100 && i % 5 == 0) {
                    hpyp.like.sample(hpyp);
                }
            }
            
            if (i % 10 == 0) {
                hpyp.sample(false);
            } else {
                hpyp.sample(true);
            }

            System.out.println(i + "," + (double)(System.currentTimeMillis() - start_time)/1000 + ", " + hpyp.score() + ", " + hpyp.misspelledWords(trueWords) + ", " + ((InDelLikelihood)hpyp.like).lambda_i + ", " + ((InDelLikelihood)hpyp.like).lambda_s + ", " + ((InDelLikelihood)hpyp.like).lambda_d);
            
            ps.print(hpyp.score() + ", ");
            hpyp.score(context, test, d, ps);
            ps.println();
        }
        ps.close();        
    }
}
