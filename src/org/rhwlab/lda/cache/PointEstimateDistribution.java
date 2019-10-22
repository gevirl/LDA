/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;

/**
 *
 * @author gevirl
 */
public interface PointEstimateDistribution {
    public PointEstimates getEstimates(String statistic) throws Exception ;
    public void statisticReport(File dir, String statistic, int skip,double alpha,double beta,long totalWords,int[][] docs,int lastIter,int nTopics,int nVocab) throws Exception ;
    public String getLabel();
    public void add(Object obj);
    public void add(int[][][] z);
}
