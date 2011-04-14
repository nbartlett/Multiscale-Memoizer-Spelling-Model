/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.CountingIntegerMap;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableInt;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 *
 * @author nicholasbartlett
 */
public class Table extends HashSet<Table> {

    public Restaurant restaurant;
    public Table parent;
    public MutableInt dish;
    public CountingIntegerMap observations;

    public Table(Restaurant restaurant) {
        this.restaurant = restaurant;
        observations = new CountingIntegerMap();
    }
    
    @Override
    public int size() {
        return super.size() + observations.size();
    }

    public void seat(Table table) {
        table.parent = this;
        table.dish = dish;
        add(table);
    }

    public void unseat(Table table) {
        if (!remove(table)) {
            throw new IllegalArgumentException("This table is not sitting here");
        }

        table.parent = null;
        table.dish = null;

        if (size() == 0) {
            if (parent != null) {
                parent.unseat(this);
            }
            restaurant.unseat(dish, this);
        }
    }

    public void seat(int observation) {
        observations.increment(observation);
    }

    public void unseat(int observation) {
        if (observations.decrement(observation).intValue() == 0) {
            observations.remove(observation);
            if (size() == 0) {
                if (parent != null) {
                    parent.unseat(this);
                }
                restaurant.unseat(dish, this);
            }
        }
    }
/*
    public void logProbabilitySubtree(double[] probabilities, int[] parameters, Likelihood likelihood) {
        for (Table table : this) {
            table.logProbabilitySubtree(probabilities, parameters, likelihood);
        }

        for (Entry<Integer, MutableInt> entry : observations.entrySet()) {
            int i = 0;
            int observation = entry.getKey();
            int multiplicity = entry.getValue().intValue();
            for (int p : parameters) {
                probabilities[i++] +=  likelihood.logProbability(observation, p) * multiplicity;
            }
        }
    }
 * 
 */
}
