/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

/**
 *
 * @author gevirl
 */
public class PointEstimates {
    public double[][] docEst;
    public double[][] wordEst;
    
    public PointEstimates(double[][] docs,double[][] words){
        this.docEst = docs;
        this.wordEst = words;
    }
}
