/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.Word;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 * @author nicholasbartlett
 */
public class FileWordIterator implements Iterator<int[]> {

    private Scanner scanner;
    private int[] next;

    public FileWordIterator(File f) throws FileNotFoundException, IOException {
        scanner = new Scanner(new BufferedInputStream(new FileInputStream(f)));
        scanner.useDelimiter("[^a-zA-Z'0-9]");
        update();
    }

    private void update() {
        next = null;
        while (scanner.hasNext() && next == null) {
            String n = scanner.next();
            if (n.length() > 0) {
                next = convert(n.replaceAll("[ 0-9']+","").toLowerCase());
                if (next.length == 0) {
                    next = null;
                }
            } else {
                next = null;
            }
        }
    }

    public int[] convert(String lowCaseWord) {
        byte[] byteWord = lowCaseWord.getBytes();
        int[] word = new int[byteWord.length];
        for (int i = 0; i < word.length; i++) {
            word[i] = (int) byteWord[i] - 97;
        }
        return word;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public int[] next() {
        int[] n = next;
        update();
        return n;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws IOException {
        if (scanner != null) {
            scanner.close();
        }
    }

    /*public static void main(String[] args) throws FileNotFoundException, IOException {
        FileWordIterator fwi = new FileWordIterator(new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/alice_in_wonderland.txt"));
        PrintStream out = new PrintStream(new FileOutputStream(new File("/Users/nicholasbartlett/Documents/np_bayes/data/alice_in_wonderland/aiw_spelling_corrupted_10.txt")));
        
        int i = 0;
        Word w;
        while(fwi.hasNext()) {
            i++;
            w = new Word(fwi.next());
            if (Util.rng.nextDouble() < 0.1) w.corrupt();
            if (i == 0) out.print(w.toString()); 
            else out.print(" " + w.toString());
        }
        System.out.println(i);
        fwi.close();
        out.close();
    }*/
}
