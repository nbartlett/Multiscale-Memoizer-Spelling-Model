/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.hpyp;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Pair;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */
public class Restaurant extends HashMap<Integer, Restaurant> {

    private int customers, tables;
    private Restaurant parent;
    private MutableDouble concentration, discount;
    private HashMap<Integer, TSA> tableArrangements;

    public Restaurant(Restaurant parent, MutableDouble concentration, MutableDouble discount) {
        this.parent = parent;
        this.concentration = concentration;
        this.discount = discount;

        tableArrangements = new HashMap<Integer, TSA>();
        customers = 0;
        tables = 0;
    }

    public boolean hasNoCustomers() {
        return customers == 0;
    }

    public int generate(Random rng) {
        double cuSum = 0d;
        double r = rng.nextDouble();
        double d = discount.doubleValue();
        double denom = (double) customers + concentration.doubleValue();

        TSA tsa;
        for (Entry<Integer, TSA> entry : tableArrangements.entrySet()) {
            tsa = entry.getValue();
            cuSum += ((double) tsa.customers - d * (double) tsa.tables) / denom;
            if (cuSum > r) {
                return entry.getKey();
            }
        }

        return parent.generate(rng);
    }

    public double probability(int type) {
        double pp = parent.probability(type);

        if (customers == 0) {
            return pp;
        } else {
            TSA tsa = tableArrangements.get(type);
            double tc = 0d, tt = 0d, d = discount.doubleValue(), c = concentration.doubleValue();

            if (tsa != null) {
                tc = tsa.customers;
                tt = tsa.tables;
            }

            return (tc - d * tt + pp * (d * (double) tables + c)) / ((double) customers + c);
        }
    }

    public void seat(int type, Random rng) {
        double pp = parent.probability(type);

        TSA tsa = tableArrangements.get(type);
        if (tsa == null) {
            tsa = new TSA();
            tableArrangements.put(type, tsa);
        }

        if (tsa.seat(pp, concentration.doubleValue(), discount.doubleValue(), tables, rng)) {
            tables++;
            parent.seat(type, rng);
        }
        customers++;
    }

    public void unseat(int type, Random rng) {
        TSA tsa = tableArrangements.get(type);

        assert tsa != null : "tsa should not be null if trying to unseat someone";

        if (tsa.unseat(rng)) {
            tables--;
            parent.unseat(type, rng);
        }

        customers--;
    }

    public void sampleSeatingArrangements(Random rng) {

        Pair<int[], int[]> randomCustomerOrder = randomCustomerOrder();

        if (randomCustomerOrder == null) {
            return;
        }

        int[] types = randomCustomerOrder.first();
        int[] tables = randomCustomerOrder.second();

        TSA tsa;
        double pp;
        int type;

        for (int i = 0; i < types.length; i++) {
            type = types[i];
            tsa = tableArrangements.get(type);

            if (tsa.unseat(tables[i])) {
                this.tables--;
                parent.unseat(type, rng);
            }
            customers--;

            pp = parent.probability(type);

            if (tsa.seat(pp, concentration.doubleValue(), discount.doubleValue(), this.tables, rng)) {
                this.tables++;
                parent.seat(type, rng);
            }
            customers++;
        }

        assert checkCounts();
    }

    public double score() {
        double score = 0.0, d = discount.doubleValue(), c = concentration.doubleValue();

        for (TSA tsa : tableArrangements.values()) {
            score += tsa.score(d);
        }

        for (int table = 1; table < tables; table++) {
            score += Math.log((double) table * d + c);
        }

        for (int customer = 1; customer < customers; customer++) {
            score -= Math.log((double) customer + c);
        }

        return score;
    }

    public boolean checkCounts() {
        int c = 0, t = 0;

        for (TSA tsa : tableArrangements.values()) {
            if (!tsa.checkCounts()) {
                return false;
            }
            c += tsa.customers;
            t += tsa.tables;
        }

        assert c == customers : "customer count incorrect : c = " + c + " : customers = " + customers;
        assert t == tables : "table count incorrect : t = " + t + " : tables = " + tables;

        return c == customers && t == tables;
    }

    public void removeZeros() {
        for (TSA tsa : tableArrangements.values()) {
            tsa.removeZeros();
        }
    }

    @Override
    public String toString() {
        String toStr = "Concentration: " + concentration.doubleValue() + "\n"
                + "Discount: " + discount.doubleValue() + "\n";
        for (Entry<Integer, TSA> entry : tableArrangements.entrySet()) {
            if (entry.getValue().customers != 0) {
                toStr = toStr + entry.getKey() + "->" + Arrays.toString(entry.getValue().sa) + "\n";
            }
        }
        return toStr;
    }

    private int customersToSample() {
        int customersToSample = 0;

        for (TSA tsa : tableArrangements.values()) {
            if (tsa.customers > 1) {
                customersToSample += tsa.customers;
            }
        }

        return customersToSample;
    }

    private Pair<int[], int[]> randomCustomerOrder() {
        int customersToSample = customersToSample();

        if (customersToSample == 0) {
            return null;
        }

        int[] randomOrder = Util.sampleWithoutReplacement(customersToSample);
        int[] types = new int[customersToSample];
        int[] tables = new int[customersToSample];

        int type;
        int randomIndex;
        int index = 0;
        int[] sa;
        TSA tsa;

        for (Entry<Integer, TSA> entry : tableArrangements.entrySet()) {

            type = entry.getKey();
            tsa = entry.getValue();

            if (tsa.customers > 1) {
                sa = tsa.sa;
                for (int table = 0; table < sa.length; table++) {
                    for (int cust = 0; cust < sa[table]; cust++) {
                        randomIndex = randomOrder[index++];
                        types[randomIndex] = type;
                        tables[randomIndex] = table;
                    }
                }
            }
        }

        assert index == customersToSample : "index = " + index + " : customers to sample = " + customersToSample;
        return new Pair(types, tables);
    }
}
