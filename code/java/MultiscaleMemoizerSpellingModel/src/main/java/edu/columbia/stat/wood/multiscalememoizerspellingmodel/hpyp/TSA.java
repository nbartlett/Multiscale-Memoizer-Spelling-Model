/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.stat.wood.multiscalememoizerspellingmodel.hpyp;

import java.util.Random;

/**
 *
 * @author nicholasbartlett
 */

public class TSA {

    public int customers, tables;
    public int[] sa;

    public TSA() {
        customers = 0;
        tables = 0;
    }

    public boolean seat(double pp, double concentration, double discount, int totalTables, Random rng) {
        if (customers == 0) {
            sa = new int[]{1};
            customers = 1;
            tables = 1;
            return true;
        } else {
            double tw = customers - discount * tables + pp * (discount * totalTables + concentration);
            double r = rng.nextDouble();
            double cuSum = 0.0;
            int zeroIndex = -1;

            for (int table = 0; table < sa.length; table++) {
                if (sa[table] == 0) {
                    zeroIndex = table;
                } else {
                    cuSum += (sa[table] - discount) / tw;
                    if (cuSum > r) {
                        sa[table]++;
                        customers++;
                        return false;
                    }
                }
            }

            if (cuSum <= r) {
                if (zeroIndex > -1) {
                    sa[zeroIndex] = 1;
                } else {
                    int[] newsa = new int[sa.length + 1];
                    System.arraycopy(sa, 0, newsa, 0, sa.length);
                    newsa[sa.length] = 1;

                    sa = newsa;
                }

                customers++;
                tables++;
                return true;
            }
            throw new RuntimeException("Should not make it to here.");
        }
    }

    public boolean unseat(Random rng) {
        int unseatIndex = (int) (rng.nextDouble() * customers);
        int cuSum = 0;

        assert customers > 0 : "To unseat customers there must be customers to unseat";

        for (int table = 0; table < sa.length; table++) {
            cuSum += sa[table];
            if (cuSum >= unseatIndex && cuSum > 0) {
                sa[table]--;
                customers--;
                if (sa[table] == 0) {
                    tables--;
                    return true;
                } else {
                    return false;
                }
            }
        }
        
        throw new RuntimeException("Should never get to this point");
    }

    public boolean unseat(int table) {
        sa[table]--;
        customers--;

        assert sa[table] >= 0 : "table size must be >= 0";

        if (sa[table] == 0) {
            tables--;
            return true;
        } else {
            return false;
        }
    }

    /*
    public double draw(double cuSum, double r, double discount, double concentration, double totalCustomers) {
        for (int i = 0; i < sa.length; i++) {
            cuSum += ((double) sa[i] - discount) / (totalCustomers + concentration);
            if (cuSum > r) {
                sa[i]++;
                customers++;
                break;
            }
        }
        return cuSum;
    }

    public void addNewTable() {
        int[] newsa = new int[sa.length + 1];
        System.arraycopy(sa, 0, newsa, 0, sa.length);
        newsa[sa.length] = 1;
        sa = newsa;
        customers++;
        tables++;
    }*/

    public double score(double discount) {
        double score = 0.0;

        for (int table : sa) {
            if (table > 0) {
                for (int customer = 1; customer < table; customer++) {
                    score += Math.log(customer - discount);
                }
            }
        }

        return score;
    }

    public boolean checkCounts() {
        int c = 0, t = 0;
        for (int table : sa) {
            c += table;
            if (table > 0) {
                t++;
            }
        }

        assert c == customers : "customer count is not correct : c =  " + c + " : customers = " + customers;
        assert t == tables : "table count is not correct : t = " + t + " : tables = " + tables;

        return c == customers && t == tables;
    }

    public void removeZeros() {
        assert checkCounts();

        if (sa.length != tables) {

            int[] newsa = new int[tables];
            int t = 0;
            for (int table : sa) {
                if (table > 0) {
                    newsa[t++] = table;
                }
            }
            
            sa = newsa;
        }
    }
}
