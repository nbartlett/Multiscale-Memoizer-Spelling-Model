/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static void main(String[] args) throws FileNotFoundException, IOException {
/*        FileWordIterator fwi = new FileWordIterator(new File(args[0]));
        int i = 0;
        //System.out.print(new Word(fwi.next()));
        while(fwi.hasNext()) {
            fwi.next();
            i++;
            //System.out.print(" " + new Word(fwi.next()));
        }
        System.out.println(i);
        fwi.close();*/
        
        System.out.println("testing something");
        
        Integer _1 = new Integer(1);
        Integer _2 = new Integer(2);
        Integer _3 = _1;
        Integer _4 = _3;
       
        System.out.println(_1);
        System.out.println(_2);
        System.out.println(_3);
        System.out.println(_4);
        
        _3 = _2;
        
        System.out.println(_1);
        System.out.println(_2);
        System.out.println(_3);
        System.out.println(_4);
        
        
    }
}
