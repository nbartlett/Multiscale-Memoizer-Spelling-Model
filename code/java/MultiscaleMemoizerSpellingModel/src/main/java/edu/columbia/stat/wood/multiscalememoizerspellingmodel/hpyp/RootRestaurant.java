/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Distribution;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableInt;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
@SuppressWarnings("serial")
public class RootRestaurant extends Restaurant {

    public Distribution<Integer> baseDistribution;
    public HashMap<Integer, MutableInt> customerCount;

    public RootRestaurant(Distribution<Integer> baseDistribution){
        super(null, null, null);
        this.baseDistribution = baseDistribution;
        customerCount = new HashMap<Integer, MutableInt>();
    }

    @Override
    public boolean hasNoCustomers() {
        return customerCount.isEmpty();
    }

    @Override
    public int generate(Random rng) {
        return baseDistribution.generate();
    }

    @Override
    public double probability(int type){
        return Math.exp(baseDistribution.logProbability(type));
    }

    @Override
    public void seat(int type, Random rng){
        MutableInt c = customerCount.get(type);
        if(c == null){
            c = new MutableInt(1);
            customerCount.put(type, c);
        } else {
            c.increment();
        }
    }


    @Override
    public void unseat(int type, Random rng){
        MutableInt c = customerCount.get(type);
        if (c.intValue() == 1) {
            customerCount.remove(type);
        } else {
            c.decrement();
        }
    }

    @Override
    public void sampleSeatingArrangements(Random rng){}

    @Override
    public double score(){
        double score = 0.0;
        for (Entry<Integer, MutableInt> entry : customerCount.entrySet()) {
            score += entry.getValue().intValue() * baseDistribution.logProbability(entry.getKey());
        }
        return score;
    }

    @Override
    public boolean checkCounts(){
        return true;
    }

    @Override
    public void removeZeros(){}

    @Override
    public String toString() {
        String string = "Root Restaurant : \n";
        for (Entry<Integer, MutableInt> entry : customerCount.entrySet()) {
            string += "type = " + entry.getKey() + ", count = " + entry.getValue().intValue() + "\n";
        }
        return string;
    }
}
