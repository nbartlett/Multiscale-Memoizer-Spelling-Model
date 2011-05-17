/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.Word;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author nicholasbartlett
 */
public class SMSReader {

    private BufferedInputStream bis;
    
    public SMSReader(File f) throws FileNotFoundException {
        bis = new BufferedInputStream(new FileInputStream(f));
    }
    
    private boolean readToStart() throws IOException {
        String read = "";
        while (!read.endsWith("<text>")){
            int b = bis.read();
            if ((b = bis.read()) > -1 ) {
                read += (char) bis.read();
            } else {
                return false;
            }
        }
        return true;
    }
    
    private String readToEnd() throws IOException {
        String text = "";
        while (!text.endsWith("</text>")){
            text += (char) bis.read();
        }
        return text.substring(0,text.length()-7);
    }
    
    public void read() throws IOException {
        String text;
        boolean available;
        do {
            available = readToStart();
            text = readToEnd();
            System.out.println(text);
        } while (available);
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        //SMSReader sms = new SMSReader(new File("/Users/nicholasbartlett/Documents/np_bayes/data/sms/smsCorpus_en_2011.05.11.xml"));
        SMSReader sms = new SMSReader(new File("/Users/nicholasbartlett/Documents/np_bayes/data/sms/test.xml"));
        //sms.read();
        
        sms.read();
    }
}
