/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableInt;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author nicholasbartlett
 */

public class Restaurant extends HashMap<Integer, Restaurant> {

    public Restaurant parent;
    public int[] edgeLabel;
    public byte depth;

    public HashMap<MutableInt, HashSet<Table>> tables;
    public Discounts discounts;

    /*
    public double logProbability(MutableInt type) {
        double p = 0d;
        double d = discount();
        
        HashSet<Table> tableSet = tables.get(type);
        if (tableSet != null) {
            for (Table table : tableSet) {
                p += ((double) table.size() - d) / (double) customerCount;
            }
        }

        p += d * (double) tableCount * Math.exp(parent.logProbability(type)) / (double) customerCount;

        return Math.log(p);
    }

    
    public MutableInt generate() {
        double d = discount();
        double cuSum = 0d;
        double r = Util.rng.nextDouble();

        for (Entry<MutableInt, HashSet<Table>> entry : tables.entrySet()) {
            MutableInt mutableInt = entry.getKey();
            HashSet<Table> tableSet = entry.getValue();
            for (Table table : tableSet) {
                cuSum += ((double) table.size() - d) / (double) customerCount;
                if (cuSum > r) {
                    return mutableInt;
                }
            }
        }
        
        return parent.generate();
    }*/

    /*
    public Table seat(Table table, Likelihood likelihood, LinkedList<double[]> list, MutableDouble r, MutableDouble cuSum) {
        // on the way up the recursion calculate log probabilities of all the dishes in this restaurant
        double[] weights = new double[tables.size()];
        int[] parameters = new int[tables.size()];

        HashSet[] tableSets = new HashSet[tables.size()];
        double d = discount();
        
        int tableCount = 0;
        int i = 0;
        for (Entry<MutableInt,HashSet<Table>> entry : tables.entrySet()) {
            HashSet<Table> tableSet = entry.getValue();
            tableSets[i] = tableSet;

            tableCount += tableSet.size();
            parameters[i] = entry.getKey().intValue();
            weights[i++] = Math.log(dishCustomerCount(tableSet) - d * (double) tableSet.size());
        }

        table.logProbabilitySubtree(weights, parameters, likelihood);
        list.push(weights);

        // now, if a table is passed down from an above restaurant it means that this table needs
        // to be seated at a new table which is in turn seated at the above table.  I will
        // assume that weights get normalize in the root (base of the recursion)

        Table parentTable = parent.seat(table, likelihood, list, r, cuSum);
        if (parentTable == null) {
            weights = list.pop();
            for (int j = 0; j < weights.length; j++) {
                cuSum.plusEquals(weights[j]);
                if (cuSum.doubleValue() > r.doubleValue()) {
                    // select from the appropriate hash setl
                }
            }
        } else {
            // insert new table with dish parentTable.dish and seat this this table 
        }





        // get the probability of table being assigned to upper level type table etc

        


        



    }
    */

    private double dishCustomerCount(HashSet<Table> tableSet) {
        int c = 0;
        for (Table t : tableSet) {
            c += t.size();
        }
        return c;
    }

    
    public void decrementObservationCount(MutableInt type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public void sample() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private double discount() {
        return discounts.get(depth, edgeLabel);
    }

    void unseat(MutableInt dish, Table aThis) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
