/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel;

import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.InDelLikelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.Likelihood;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableDouble;
import edu.columbia.stat.wood.multiscalememoizerspellingmodel.util.MutableInt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 *
 * @author nicholasbartlett
 */
public class Table extends Customer {

    public Table parent;
    public Word parameter;
    public HashSet<Table> tables;
    public HashMap<Datum, MutableInt> data;

    public Table () {
        tables = new HashSet<Table>();
        data = new HashMap<Datum, MutableInt>();
    }

    public int size() {
        int s = tables.size();
        for (MutableInt count : data.values()) {
            s += count.intValue();
        }
        return s;
    }

    public void seat(Customer customer) {
        if (customer.getClass() == getClass()) {
            Table table = (Table) customer;
            table.parent = this;
            tables.add(table);
            table.updateParameter(parameter);
        } else {
            Datum datum = (Datum) customer;
            MutableInt count = data.get(datum);
            if (count == null) {
                count = new MutableInt(0);
                data.put(datum, count);
            }
            count.increment();
        }
    }

    @Override
    public double logLikelihood(Word parameter, Likelihood like) {
        double logLike = 0d;
        
        for (Table table : tables) {
            logLike += table.logLikelihood(parameter, like);
        }

        for (Entry<Datum, MutableInt> entry : data.entrySet()) {
            logLike += entry.getKey().logLikelihood(parameter, like) * (double) entry.getValue().intValue();
        }

        return logLike;
    }
    
    public void updateParameter(Word parameter) {        
        this.parameter = parameter;
        for (Table table : tables) {
            table.updateParameter(parameter);
        }
    }

    @Override
    public String toString() {        
        String str = "";
        
        for (int i : parameter.value) {
            str += (char) (i + 97);
        }
        str += " : ";

        for (Table table : tables) {
            str += " ";
            for (int i : table.parameter.value) {
                str += (char) (i + 97);
            }
            str += ",";
        }

        for (Entry<Datum, MutableInt> entry : data.entrySet()) {
            str += " " + entry.getKey() + "(" + entry.getValue().intValue() + ")";
        }

        return str;
    }
    
    public boolean checkParameterValueConsistency(Word word) {
        boolean p = true;
        if (parent != null) p = parent.checkParameterValueConsistency(word);
        return p && parameter.equals(word);
    }
}
