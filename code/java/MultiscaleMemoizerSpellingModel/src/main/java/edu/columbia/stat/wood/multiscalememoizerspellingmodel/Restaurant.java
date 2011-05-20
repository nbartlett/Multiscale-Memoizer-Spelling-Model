/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableInt;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Pair;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.HashSet;

/**
 *
 * @author nicholasbartlett
 */

public class Restaurant extends HashMap<Word, Restaurant> {

    public Restaurant parent;
    public HashSet<Table> tables;
    public MutableDouble discount;
    public MutableDouble concentration;
    public int customers;
    
    public static int m = 10;

    public Restaurant(Restaurant parent, MutableDouble concentration, MutableDouble discount) {
        this.parent = parent;
        this.concentration = concentration;
        this.discount = discount;
        tables = new HashSet<Table>();
        customers = 0;
    }

    public Restaurant get(int index, Word[] context, MutableDouble[] concentrations, MutableDouble[] discounts) {
        if (index > -1 && context[index] != null) {
            Restaurant child = get(context[index]);
            if (child == null) {
                int i = index < discounts.length ? index : (discounts.length - 1);
                child = new Restaurant(this, concentrations[i], discounts[i]);
                put(context[index], child);
            }
            return child.get(index - 1, context, concentrations, discounts);
        } else {
            return this;
        }
    }

    public void seatWithParameter(Table childTable) {
        customers++;
        if (tables == null || tables.isEmpty()) {
            if (tables == null) {
                tables = new HashSet<Table>();
            }

            Table newTable = new Table();
            newTable.parameter = childTable.parameter;
            newTable.tables.add(childTable);
            childTable.parent = newTable;
            tables.add(newTable);
            parent.seatWithParameter(newTable);
        } else {
            ArrayList<Table> candidateList = new ArrayList<Table>();
            double tw = 0d;
            double d = discount.doubleValue();
            double c = concentration.doubleValue();

            for (Table t : tables) {
                if (t.parameter.equals(childTable.parameter)) {
                    candidateList.add(t);
                    tw += (double) t.size() - d;
                }
            }
            tw += (d * (double) tables.size() + c) * parent.parameterProb(childTable.parameter);

            double cuSum = 0d;
            double r = Util.rng.nextDouble();

            for (Table t : candidateList) {
                cuSum += ((double) t.size() - d) / tw;
                if (cuSum > r) {
                    t.tables.add(childTable);
                    childTable.parent = t;
                    return;
                }
            }

            assert r >= cuSum;

            Table newTable = new Table();
            newTable.parameter = childTable.parameter;
            newTable.tables.add(childTable);
            childTable.parent = newTable;
            tables.add(newTable);
            parent.seatWithParameter(newTable);
        }
    }
    
    public double parameterProb(Word parameter) {
        double prob = 0d;
        
        double c = concentration.doubleValue();
        double d = discount.doubleValue();
        
        double denom = (double) customers + c;
        
        for (Table table : tables) {
            if (table.parameter.equals(parameter)) prob += ((double) table.size() - d) / denom;
        }
        
        return prob + (d * (double) tables.size() + c) * parent.parameterProb(parameter) / denom;
    }

    public void initSeatDatum(Datum datum) {
        customers++;
        if (tables == null || tables.isEmpty()) {
            if (tables == null) {
                tables = new HashSet<Table>();
            }

            Table newTable = new Table();
            newTable.parameter = datum.word;
            newTable.seat(datum);
            tables.add(newTable);
            parent.seatWithParameter(newTable);
        } else {
            ArrayList<Table> candidateList = new ArrayList<Table>();
            double tw = 0d;
            double d = discount.doubleValue();
            double c = concentration.doubleValue();

            for (Table t : tables) {
                if (t.parameter.equals(datum.word)) {
                    candidateList.add(t);
                    tw += (double) t.size() - d;
                }
            }
            tw += (d * (double) tables.size() + c) * parent.parameterProb(datum.word);

            double cuSum = 0d;
            double r = Util.rng.nextDouble();

            for (Table t : candidateList) {
                cuSum += ((double) t.size() - d) / tw;
                if (cuSum > r) {
                    t.seat(datum);
                    return;
                }
            }
            
            assert r >= cuSum;

            Table newTable = new Table();
            newTable.parameter = datum.word;
            newTable.seat(datum);
            tables.add(newTable);
            parent.seatWithParameter(newTable);
        }
    }

    public void seat(Customer customer, Likelihood like) {
        
        ArrayList<Pair<Table, Double>> logWeightsTables = new ArrayList<Pair<Table, Double>>();
        MutableDouble log_tw = new MutableDouble(Double.NEGATIVE_INFINITY);
        double d = discount.doubleValue();
        double c = concentration.doubleValue();
        Table emptyTable = null;

        double log_w;
        double log_denom = Math.log((double) customers + c);
        for (Table table : tables) {
            if (table.size() > 0) {
                log_w = Math.log((double) table.size() - d) - log_denom + customer.logLikelihood(table.parameter, like);
                log_tw.addLogs(log_w);
                logWeightsTables.add(new Pair(table, log_w));
            } else {
                if (emptyTable != null) {
                    throw new RuntimeException("should only be one empty table at most");
                }
                emptyTable = table;
            }
        }

        if (emptyTable != null) {
            tables.remove(emptyTable);
        }

        ArrayList<Pair<Word, Double>> logWeightsParams = new ArrayList<Pair<Word, Double>>();
        double logProbPrior = Math.log(d * (double) tables.size() + c) - log_denom;
        parent.parentParamsAndWeights(customer, like, logWeightsParams, log_tw, logProbPrior, m, emptyTable);

        assert log_tw.doubleValue() > Double.NEGATIVE_INFINITY;
        assert !Double.isNaN(log_tw.doubleValue());

        double log_r = Math.log(Util.rng.nextDouble());
        double cuSum = Double.NEGATIVE_INFINITY;

        double log_total_weight = log_tw.doubleValue();
        for (Pair<Table, Double> pair : logWeightsTables) {
            cuSum = addLogs(cuSum, pair.second() - log_total_weight);
            if (cuSum > log_r) {
                pair.first().seat(customer);                                              
                customers++;
                return;
            }
        }

        for (Pair<Word, Double> pair : logWeightsParams) {
            cuSum = addLogs(cuSum, pair.second() - log_total_weight);
            if (cuSum > log_r) {                
                Table table = new Table();
                table.parameter = pair.first();
                tables.add(table);
                table.seat(customer);
                parent.seatWithParameter(table);
                customers++;
                return;
            }
        }
        
        /*assert (cuSum - .00001 < 0) && (cuSum + .00001 > 0) && ret == true;
        
        if (ret) 
            return;*/
              
        System.out.println("cuSum = " + cuSum);
        System.out.println("log_tw = " + log_tw);
        System.out.println("log _r = " + log_r);
        throw new RuntimeException("Should never get down to here");
    }
    
    public int differentFromParameter() {
        int diff = 0;
        for (Table table : tables) {
            for (Table child : table.tables) {
                if (!child.parameter.equals(table.parameter))
                    diff++;
            }
        }
        return diff;
    }
    
    public void parentParamsAndWeights(Customer customer, Likelihood like, ArrayList<Pair<Word,Double>> logWeightsParams, MutableDouble log_tw, double log_scalar, int m, Table emptyTable){
        double d = discount.doubleValue();
        double c = concentration.doubleValue();
        
        double log_w;
        double log_denom = Math.log((double) customers + c);
        for (Table table : tables) {
            log_w = log_scalar + Math.log((double) table.size() - d) - log_denom + customer.logLikelihood(table.parameter, like);
            logWeightsParams.add(new Pair(table.parameter, log_w));
            log_tw.addLogs(log_w);
        }

        parent.parentParamsAndWeights(customer, like, logWeightsParams, log_tw, log_scalar + Math.log(d * (double) tables.size() + c) - log_denom, m, emptyTable);
    }

    public void unseat(Table childTable, Table table) {
        table.tables.remove(childTable);
        customers--;
        if (table.size() == 0) {
            tables.remove(table);
            parent.unseat(table, table.parent);
        }
    }

    /*
    public ArrayList<Word> generateParameters(int n) {
        ArrayList<Word> sample = new ArrayList<Word>(n);
        generateParameters(sample, Util.rng.nextDouble() / (double) n, n);
        return sample;
    }

    public void generateParameters(ArrayList<Word> sample, double r, int n) {
        double cuSum = 0d;
        double d = discount.doubleValue();
        double c = concentration.doubleValue();
        double constant = 1d / (double) n;

        for (Table table : tables) {
            cuSum += ((double) table.size() - d) / ((double) customers + c);

            assert cuSum < 1d;

            while (cuSum > r) {
                sample.add(table.parameter);
                n--;
                r += constant;
            }
        }

        if (n > 0) {
            double probThis = ((double) customers - d * (double) tables.size()) / ((double) customers + c);
            double probParent = (d * (double) tables.size() + c) / ((double) customers + c);
            parent.generateParameters(sample, (r - probThis) / probParent, n);
        }
    }*/

    public void sample(Likelihood like, boolean onlyDatum) {

        HashSet<Table> copyTables = (HashSet<Table>) tables.clone();
        for (Table table : copyTables) {
            if (!onlyDatum) {
                HashSet<Table> copyTableTables = (HashSet<Table>) table.tables.clone();
                for (Table childTable : copyTableTables) {
                    table.tables.remove(childTable);
                    customers--;
                    if (table.size() == 0) {
                        parent.unseat(table, table.parent);
                    }
                    seat(childTable, like);
                }
            }

            HashMap<Datum, MutableInt> copyData = (HashMap<Datum, MutableInt>) table.data.clone();
            for (Entry<Datum, MutableInt> entry : copyData.entrySet()) {
                int cnt = entry.getValue().intValue();
                Datum datum = entry.getKey();
                for (int i = 0; i < cnt; i++) {
                    if (table.data.get(datum).intValue() == 1)
                        table.data.remove(datum);
                    else
                        entry.getValue().decrement();
                    
                    customers--;
                    
                    if (table.size() == 0) 
                        parent.unseat(table, table.parent);

                    seat(entry.getKey(), like);
                }
            }
        }
    }

    public void logProbability(MutableDouble logProbability, int[] read, Likelihood like, double log_scalar) {
        double lw;
        double d = discount.doubleValue();
        double c = concentration.doubleValue();

        double log_denom = Math.log((double) customers + c);
        for (Table table : tables) {
            lw = log_scalar + Math.log((double) table.size() - d) - log_denom + like.logProb(table.parameter.value, read);
            logProbability.addLogs(lw);
        }

        assert logProbability.doubleValue() < 0d;

        parent.logProbability(logProbability, read, like, Math.log(d * (double) tables.size() + c) - log_denom + log_scalar);
    }

    public double score(Likelihood like) {
        double score = 0d;
        double d = discount.doubleValue();
        double c = concentration.doubleValue();

        int tbls = 0;
        int custs = 0;

        for (Table table : tables) {
            int cust = 0;
            for (int t = 0; t < table.tables.size(); t++) {
                if (tbls == 0 && cust == 0); else if (cust == 0) {
                    score += Math.log(d * (double) tbls + c);
                } else {
                    score += Math.log((double) cust - d);
                }
                score -= Math.log((double) custs + c);
                cust++;
                custs++;
            }

            for (Entry<Datum, MutableInt> entry : table.data.entrySet()) {
                if (tbls == 0 && cust == 0); else if (cust == 0) {
                    score += Math.log(d * (double) tbls + c);
                } else {
                    score += Math.log((double) cust - d);
                }
                score -= Math.log((double) custs + c);
                
                score += entry.getKey().logLikelihood(table.parameter, like) * (double) entry.getValue().intValue();
                cust++;
                custs++;
            }
            tbls++;
        }
                     
        for (Restaurant child : values()) {
            score += child.score(like);
        } 
        
        return score;
    }
    
    public void score(Likelihood like, double[] s, int depth) {
        double score = 0d;
        double d = discount.doubleValue();
        double c = concentration.doubleValue();

        int tbls = 0;
        int custs = 0;

        for (Table table : tables) {
            int cust = 0;
            for (int t = 0; t < table.tables.size(); t++) {
                if (tbls == 0 && cust == 0); else if (cust == 0) {
                    score += Math.log(d * (double) tbls + c);
                } else {
                    score += Math.log((double) cust - d);
                }
                score -= Math.log((double) custs + c);
                cust++;
                custs++;
            }

            for (Entry<Datum, MutableInt> entry : table.data.entrySet()) {
                if (tbls == 0 && cust == 0); else if (cust == 0) {
                    score += Math.log(d * (double) tbls + c);
                } else {
                    score += Math.log((double) cust - d);
                }
                score -= Math.log((double) custs + c);
                
                score += entry.getKey().logLikelihood(table.parameter, like) * (double) entry.getValue().intValue();
                cust++;
                custs++;
            }
            tbls++;
        }
        
        s[depth < s.length ? depth : (s.length - 1)] += score;
        for (Restaurant child : values()) {
            child.score(like, s, depth + 1);
        }
    }

    public boolean checkCustomerCounts() {
        int c = 0;

        for (Table t : tables) {
            c += t.size();
        }

        assert c == customers : "c = " + c + ", customers = " + customers;
        return c == customers;
    }

    public int dataCount() {
        int count = 0;
        for (Table t : tables) {
            for (MutableInt c : t.data.values()) {
                count += c.intValue();
            }
        }

        for (Restaurant c : values()){
            count += c.dataCount();
        }

        return count;
    }
    
    public boolean checkParameterValueConsistency(){
        if (isEmpty()) {
            for (Table table : tables) {
                if (!table.checkParameterValueConsistency(table.parameter)) return false;
            }
            return true;
        } else {
            boolean p = true;
            for (Restaurant child : this.values()) {
                if (!child.checkCustomerCounts()) return false;
            }
            return true;
        }
    }

    @Override
    public String toString() {
        String str = "";
        for (Table table : tables) {
            str += table.toString() + "\n";
        }
        return str;
    }

    private double addLogs(double logA, double logB) {
        if (Double.isInfinite(logA) && Double.isInfinite(logB)) {
            if (logA < 0d && logB < 0d) {
                return Double.NEGATIVE_INFINITY;
            } else {
                throw new RuntimeException("basically shouldn't happen");
            }
        } else if (logA > logB) {
            return Math.log(1.0 + Math.exp(logB - logA)) + logA;
        } else {
            return Math.log(1.0 + Math.exp(logA - logB)) + logB;
        }
    }
    
}
