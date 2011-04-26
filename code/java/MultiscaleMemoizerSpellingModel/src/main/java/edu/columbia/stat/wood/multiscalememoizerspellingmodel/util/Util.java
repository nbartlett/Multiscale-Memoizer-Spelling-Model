/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class Util {
    public static Random rng = new Random();

    public static int[] sampleWithoutReplacement(int n) {
        int[] sample = new int[n];

        for (int i = 0; i < n; i++) {
            sample[i] = i;
        }

        int currentValue;
        int sampledIndex;
        for (int i = 0; i < n; i++) {
            currentValue = sample[i];
            sampledIndex = i + (int) (rng.nextDouble() * (n - i));
            sample[i] = sample[sampledIndex];
            sample[sampledIndex] = currentValue;
        }
        return sample;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(sampleWithoutReplacement(10)));
    }
}


