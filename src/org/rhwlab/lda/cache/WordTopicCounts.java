/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.util.concurrent.Callable;

/**
 *
 * @author gevirl
 */
public class WordTopicCounts implements Callable<int[][]>{
    int[][] z;
    int nVocab;
    int nTopics;
    int[][] docs;
    
    public WordTopicCounts(int[][] z, int nVocab, int nTopics, int[][] docs){
        this.z = z;
        this.nTopics = nTopics;
        this.nVocab = nVocab;
        this.docs = docs;
    }
    
    public int[][] getCounts(){
        return wordTopicCounts(z, nVocab, nTopics, docs);
    }
    @Override
    public int[][] call() throws Exception {
        System.out.printf("WordTopicCounts %s started\n",this.toString());
        int[][] ret =wordTopicCounts(z,nVocab,nTopics,docs);
        System.out.printf("WordTopicCounts %s finished\n",this.toString());
        return ret;
    }
    
    static public int[][] wordTopicCounts(int[][] z, int nVocab, int nTopics, int[][] docs) {
        int[][] nw = new int[nVocab][nTopics];
        wordTopicCounts(z, docs, nw);
        return nw;
    }

    // compute the word,topic marginals of a given iteration of topic choices
    static public void wordTopicCounts(int[][] z, int[][] docs, int[][] nw) {
        for (int d = 0; d < z.length; ++d) {
            for (int w = 0; w < z[d].length; ++w) {
                ++nw[docs[d][w]][z[d][w]];
            }
        }
    }    
}
