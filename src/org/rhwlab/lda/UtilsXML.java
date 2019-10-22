/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import org.jdom2.Element;

/**
 *
 * @author gevirl
 */
public class UtilsXML {
    public static int[][] intArray(Element ele)throws Exception {
        ArrayList<int[]> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(ele.getTextTrim()));
        String line = reader.readLine();
        while (line != null){
            list.add(asIntVector(line));
            line = reader.readLine();
        }
        reader.close();
        int r=0;
        int[][] ret = new int[list.size()][];
        for (int[] v : list){
            ret[r] = v;
            ++r;
        }
        return ret;
    }
    public static double[][] doubleArray(Element ele)throws Exception {
        ArrayList<double[]> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(ele.getTextTrim()));
        String line = reader.readLine();
        while (line != null){
            list.add(asDoubleVector(line));
            line = reader.readLine();
        }
        reader.close();
        int r=0;
        double[][] ret = new double[list.size()][];
        for (double[] v : list){
            ret[r] = v;
            ++r;
        }
        return ret;
    }    
    public static int[] intVector(Element ele){
        return asIntVector(ele.getTextTrim());
    }
    public static Element asElement(String id,int[][] v){
        Element ret = new Element(id);
        for (int r=0 ; r<v.length ; ++r){
            ret.addContent(asString(v[r])+"\n");
        }
        return ret;
    }    
    public static Element asElement(String id,double[][] v){
        Element ret = new Element(id);
        for (int r=0 ; r<v.length ; ++r){
            ret.addContent(asString(v[r])+"\n");
        }
        return ret;
    }     
    public static Element asElement(String id,int[] v){
        Element ret = new Element(id);
        ret.addContent(asString(v));
        return ret;
    }
    static private String asString(int[] v){
        StringBuilder builder = new StringBuilder();
        builder.append(v[0]);
        for (int i=1 ; i<v.length ; ++i){
            builder.append(",");
            builder.append(v[i]);
        } 
        return builder.toString();
    }
    static private String asString(double[] v){
        StringBuilder builder = new StringBuilder();
        builder.append(v[0]);
        for (int i=1 ; i<v.length ; ++i){
            builder.append(",");
            builder.append(v[i]);
        } 
        return builder.toString();
    }    
    static private int[] asIntVector(String s){
        String[] tokens = s.split(",");
        int[] ret = new int[tokens.length];
        for (int i=0 ; i<ret.length ; ++i){
            ret[i] = Integer.valueOf(tokens[i]);
        }
        return ret;
    }
    static private double[] asDoubleVector(String s){
        String[] tokens = s.split(",");
        double[] ret = new double[tokens.length];
        for (int i=0 ; i<ret.length ; ++i){
            ret[i] = Double.valueOf(tokens[i]);
        }
        return ret;
    }    
}
