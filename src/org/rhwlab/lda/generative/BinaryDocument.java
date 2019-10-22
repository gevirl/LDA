/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.util.Random;
import java.util.TreeMap;
import static org.rhwlab.lda.generative.CountsDocument.cdf;

/**
 *
 * @author gevirl
 */
// int this document each  vocabulary count is 0 or 1 only
public class BinaryDocument extends CountsDocument {
    
    public BinaryDocument(int nWords, int thetaIndex) {
        super(nWords, thetaIndex);
    }

    
    @Override
    public TreeMap<Integer,Integer>  getWordMap(double[][] theta,double[][] phi){
        int V = phi[0].length; // vocabulary size
        int K = theta[docType].length;
        
        double[] thetaCDF = cdf(theta[docType]);
        double[][] phiCDF = new double[K][V];
        for (int k=0 ; k<K ;++k){
            phiCDF[k] = cdf(phi[k]);
        }
        
        Random rnd = new Random();
        int[] counts = new int[V];
        for (int i=0 ; i<nWords ; ++i){
            while (true){
                int vocab = sample(rnd,phiCDF[sample(rnd, thetaCDF)]);
                if (counts[vocab]==0){
                    ++counts[vocab];
                    break;
                }
            }
        }
        
        for (int v =0 ; v<counts.length ; ++v){
            if (counts[v] > 0){
                map.put(v,counts[v]);
            }
        }        
        return map;
    }    
}
