/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import org.rhwlab.lda.cache.DocumentTopicCounts;
import org.rhwlab.lda.cache.PointEstimates;
import org.rhwlab.lda.cache.WordTopicCounts;

/**
 *
 * @author gevirl
 */
public class TopicHistogramEstimator extends PointEstimatorDistBase {

    int nTopics;
    int nVocab;
    int[][] docs;
    int[][][] hists;

    public TopicHistogramEstimator(int[][] docs, int nVocab, int nTopics) {
        super();
        this.nTopics = nTopics;
        this.nVocab = nVocab;
        this.docs = docs;
        hists = new int[docs.length][][];
        for (int i = 0; i < hists.length; ++i) {
            hists[i] = new int[docs[i].length][];
            for (int j = 0; j < hists[i].length; ++j) {
                hists[i][j] = new int[nTopics];
            }
        }
    }

    /*
    public TopicHistogramEstimator(ZDirectory zDir) throws Exception {
        super(zDir);
        zHist = zDir.zHistogram();
        nVocab = zDir.getLDA().getVocabSize();
        nTopics = zDir.getLDA().getTopicsSize();
        docs = zDir.getLDA().getDocuments();
    }
     */
    @Override
    public PointEstimates getEstimates(String stat) throws Exception {

        int[][] modes = getModes();
        
        int[][] nw = WordTopicCounts.wordTopicCounts(modes, nVocab, nTopics, docs);
        double[][] wordEst = new double[nVocab][nTopics];
        for (int i = 0; i < nVocab; ++i) {
            for (int j = 0; j < nTopics; ++j) {
                wordEst[i][j] = nw[i][j];
            }
        }

        int[][] nd = DocumentTopicCounts.documentTopicCounts(modes, nTopics);
        double[][] docEst = new double[nd.length][nTopics];
        for (int i = 0; i < nd.length; ++i) {
            for (int j = 0; j < nTopics; ++j) {
                docEst[i][j] = nd[i][j];
            }
        }
        return new PointEstimates(docEst, wordEst);
    }

    @Override
    public String getLabel() {
        return "topic";
    }


    @Override
    public void add(Object obj) {
        int[][] z;
        if (obj instanceof MultiThreadLDA) {
            z = ((MultiThreadLDA) obj).getZ();
        } else {
            z = (int[][]) obj;
        }

        for (int i = 0; i < z.length; ++i) {
            for (int j = 0; j < z[i].length; ++j) {
                ++hists[i][j][z[i][j]];
            }
        }
    }

    public int[][] getModes() {
        int[][] modes = new int[hists.length][];  // doc,word -> topic (mode)
        for (int d = 0; d < hists.length; ++d) {
            modes[d] = new int[hists[d].length];
            for (int w = 0; w < hists[d].length; ++w) {
                int max = 0;
                int mode = -1;
                for (int t = 0; t < hists[d][w].length; ++t) {
                    if (hists[d][w][t] > max) {
                        max = hists[d][w][t];
                        mode = t;
                    }
                }
                if (mode == -1){
                    int asiudfhsduhfuisd=0;
                }
                modes[d][w] = mode;
            }
        }
        return modes;
    }
}
