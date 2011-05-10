/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.experiments;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.HPYP;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.Word;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.FileWordIterator;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.UniformIntegerDistribution;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author nicholasbartlett
 */
public class SpellingErrorAIW {

    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        FileWordIterator fwi = new FileWordIterator(new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/alice_in_wonderland.txt"));

        Word[] context = new Word[2];

        HPYP hpyp = new HPYP(new edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp.HPYP(new UniformIntegerDistribution(27, 1)));
        int trainingSize = 20000;
        Word w;
        Word[] test = new Word[9760];
        int k = 0;
        while (fwi.hasNext()) {
            if (trainingSize-- > 0) {
                hpyp.seatInit(context, w = new Word(fwi.next()));

                for (int j = 1; j < context.length; j++) {
                    context[j-1] = context[j];
                }
                context[context.length - 1] = w;

            } else {
                test[k++] = new Word(fwi.next());
            }
        }

        File out = new File("/Users/nicholasbartlett/Documents/np_bayes/Multiscale_Memoizer_Spelling_Model/output/aiw_spelling.out");
        PrintStream ps = new PrintStream(new FileOutputStream(out));

        for (int i = 0; i < 200; i++) {
            hpyp.sample();
            System.out.println(hpyp.score());
            ps.print(hpyp.score());
            ps.print(", ");
            hpyp.score(context, test, 2, ps);
        }
    }
}
