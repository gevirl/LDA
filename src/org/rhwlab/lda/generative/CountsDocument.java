/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author gevirl
 */
// generate a random document
// theta is the distribution of topics in the document types
// phi are the topic/word distibutions  - topics X vocab
public class CountsDocument implements Document {
    int nWords;
    int docType;
    
    TreeMap<Integer,Integer> map = new TreeMap<>();  // vocab,count
    
    public CountsDocument(int nWords,int thetaIndex){
        this.nWords = nWords;
        this.docType = thetaIndex;
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
             
            int topic = sample(rnd,thetaCDF); // pick a topic
            int vocab = sample(rnd,phiCDF[topic]);  // pick a vocab
            ++counts[vocab];
        }
        
        for (int v =0 ; v<counts.length ; ++v){
            if (counts[v] > 0){
                map.put(v,counts[v]);
            }
        }        
        return map;
    } 
    
  
    // form the cdf from a discrete pdf
    static double[] cdf(double[] pdf){
        double[] cdf = new double[pdf.length];

        cdf[0] = pdf[0];
        for (int i=1 ; i<cdf.length ; ++i){
            cdf[i] = cdf[i-1] + pdf[i];
        }
        return cdf;
    }
    
    static int sample(Random rnd,double[] cdf){
        double max = cdf[cdf.length-1];
        double r = max*rnd.nextDouble();
        for (int i=0 ; i<cdf.length ; ++i){
            if (r < cdf[i]){
                return i;
            }
        }
        return cdf.length-1;
    }

    @Override
    public String identity() {
        return String.format("%d,%d", nWords,docType);
    }

    @Override
    public double[] getTopicDist(double[][] theta) {
        return theta[docType];
    }

    @Override
    public int getWordCount() {
        return nWords;
    }

    @Override
    public List<Integer> getType() {
        List<Integer> ret = new ArrayList<>();
        ret.add(this.docType);
        return ret;
    }
}
