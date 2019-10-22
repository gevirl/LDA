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
public class DocumentTopicCounts implements Callable<int[][]> {
    int[][] z;
    int nTopics;

    
    public DocumentTopicCounts(int[][] z,int nTopics){
        this.z = z;
        this.nTopics = nTopics;

    }
    @Override
    public int[][] call() throws Exception {
        return getCounts();
    }
    
    public int[][] getCounts(){
        int[][] nd = new int[z.length][nTopics];
        for (int d = 0; d < z.length; ++d) {
            for (int w = 0; w < z[d].length; ++w) {
                ++nd[d][z[d][w]];
            }
        }
        return nd;        
    }
    static int[][] documentTopicCounts(int[][] z,int nTopics) {
        return new DocumentTopicCounts(z,nTopics).getCounts();
    }
}
