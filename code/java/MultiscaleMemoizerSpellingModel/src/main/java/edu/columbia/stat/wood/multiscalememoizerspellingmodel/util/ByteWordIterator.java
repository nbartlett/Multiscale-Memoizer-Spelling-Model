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
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nicholasbartlett
 */
public class ByteWordIterator implements Iterator<int[]> {

    BufferedInputStream bis;
    int nextByte;
    
    public ByteWordIterator(File f) throws FileNotFoundException, IOException {
        bis = new BufferedInputStream(new FileInputStream(f));
        nextByte = bis.read();
    }
    
    @Override
    public boolean hasNext() {
        return nextByte > -1;
    }

    @Override
    public int[] next() {
        int[] next = new int[8];
        for (int i = 7; i > -1; i--) {
            next[i] = nextByte & 1;
            nextByte >>= 1;
        }

        assert nextByte == 0;
        
        try {
            nextByte = bis.read();
        } catch (IOException ex) {
            Logger.getLogger(ByteWordIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File f = new File("/Users/nicholasbartlett/Desktop/small_test_data.txt");
        
        ByteWordIterator bwi = new ByteWordIterator(f);
        
        while (bwi.hasNext()) {
            System.out.println(bwi.nextByte + ", " + Arrays.toString(bwi.next()));
        }   
    }    
}
