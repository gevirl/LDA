/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import smile.math.distance.Distance;
import smile.math.distance.JensenShannonDistance;

/**
 *
 * @author gevirl
 */
public class PhiComparison {
    Distance measure;
    double[][] phi1;
    double[][] phi2;
    TopicMatch[] matches;
    
    public PhiComparison(double[][] phi1, double[][] phi2) {
        this(phi1,phi2,new JensenShannonDistance());
        
    }
    public PhiComparison(double[][] phi1, double[][] phi2,Distance measure) {
        this.phi1 = phi1;
        this.phi2 = phi2;
        this.measure = measure;
        matches = matches(distance());
    }
    public Distance getMeasure(){
        return measure;
    }
 /*   
    public TreeMap<Double,int[]> orderPairs(double[][] jsd){
        TreeMap<Double,int[]> ret = new TreeMap<>();
        for (int row=0 ; row<jsd.length ; ++row){
            for (int col=0 ; col<jsd[row].length ; ++col){
                int[] indexes = new int[2];
                indexes[0] = row;
                indexes[1] = col;
                double d = jsd[row][col];
                if (ret.get(d)!=null){
                    int usdifsuifh=0;
                }
                ret.put(jsd[row][col],indexes);
            }
        }
        return ret;
    }
*/
    public TopicMatch[] matches(double[][] jsd){
        
        TopicMatch[] ret = new TopicMatch[jsd.length];
        for (int row=0 ; row<jsd.length ; ++row){
            double min = Double.MAX_VALUE;
            int index = -1;
            for (int col=0 ; col<jsd[row].length ; ++col){
                if (jsd[row][col] < min){
                    min = jsd[row][col];
                    index = col;
                }
            } 
            ret[row] = new TopicMatch(index,min);
            System.out.printf("%d -> %d ,%.4f\n",row,index,min);
        }
        return ret;
    }
/*    
    public int[] matches(TreeMap<Double,int[]> map){
        int count =0;
        ArrayList<int[]> list = new ArrayList<>();
        TreeSet<Integer> used0 = new TreeSet<>();
        TreeSet<Integer> used1 = new TreeSet<>();
        for (int[] pair : map.values()){
            ++count;
            if (!used0.contains(pair[0]) && !used1.contains(pair[1])){
                list.add(pair);
                used0.add(pair[0]);
                used1.add(pair[1]);
            }
            if (used0.size() == phi1.length){
                break;
            }
        }
        int[] ret = new int[list.size()];
        for (int[] pair : list){
            ret[pair[0]] = pair[1];
        }
        return ret;
    }
 */   
    public double[][] distance(){
        return distance(phi1,phi2,measure);
    }
    static public double[][] distance(double[][] phi1,double[][]phi2,Distance meas){
        double[][] ret = new double[phi1.length][phi2.length];
        
        for (int i=0 ; i<phi1.length ; ++i){
            for (int j=0 ; j<phi2.length ; ++j){
                ret[i][j] = meas.d(phi1[i],phi2[j]);
            }
        }
        return ret;
    }
    
    public int colMin(int col,double[][] v){
        int minrow=-1;
        double min = Double.MAX_VALUE;
        for (int row =0 ; row<v.length ; ++row){
            if (v[row][col]<min){
                min = v[row][col];
                minrow = row;
            }
        }
        return minrow;
    }
    public int minIndex(double[] v){
        int minIndex=-1;
        double min = Double.MAX_VALUE;   
        for (int i=0 ; i<v.length ; ++i){
            if (v[i] < min){
                min = v[i];
                minIndex = i;
            }
        }
        return minIndex;
    }
    public TopicMatch[] getMatches(){
        return this.matches;
    }
}
