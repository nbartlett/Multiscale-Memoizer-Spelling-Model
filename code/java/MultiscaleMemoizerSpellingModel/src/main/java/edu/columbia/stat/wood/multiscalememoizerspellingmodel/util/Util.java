/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class Util {
    public static Random rng = new Random(9);
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

        int[] key1 = new int[]{1,2,3};
        int[] key2 = new int[]{1,2};
        int[] key3 = new int[]{1,2,3};

        HashMap<int[], Integer> map = new HashMap<int[], Integer>();

        map.put(key1,1);
        map.put(key2,2);
        map.put(key3,3);

        System.out.println(map.get(key1));
        System.out.println(map.get(key2));
        System.out.println(map.get(key3));
        System.out.println(map.get(new int[]{1,2,3}));



        
    }
}


