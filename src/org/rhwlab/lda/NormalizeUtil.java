/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

/**
 *
 * @author gevirl
 */
public class NormalizeUtil {

    public static double[][] normRows(double[][] x){
        double[][] ret = new double[x.length][];
        for (int i=0 ; i<x.length ; ++i){
            ret[i] = normLength(x[i]);
        }
        return ret;
    }
    public static double[] normLength(double[] x) {
        double[] ret = new double[x.length];
        double sum = 0.0;
        for (int i = 0; i < x.length; ++i) {
            sum = sum + x[i] * x[i];
        }
        double d = Math.sqrt(sum);
        for (int i = 0; i < x.length; ++i) {
            ret[i] = x[i]/d;
        }
        return ret;
    }
}
