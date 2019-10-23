/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import org.rhwlab.lda.cache.DocumentTopicCounts;
import org.rhwlab.lda.cache.PointEstimates;
import org.rhwlab.lda.cache.WordTopicCounts;
import weka.estimators.KernelEstimator;

/**
 *
 * @author gevirl
 */
public class MarginalKDE extends PointEstimatorDistBase {

    double precision;
    int[][] docs;
    KernelEstimator[][] wordKDEs;  // nVocab x nTopics
    KernelEstimator[][] docKDEs;  // nDocs x nTopics
    int nTopics;

    public MarginalKDE(int[][] docs, int nVocab, int nTopics, double p) {
        this.precision = p;
        this.docs = docs;
        this.nTopics = nTopics;

        wordKDEs = new KernelEstimator[nVocab][];
        docKDEs = new KernelEstimator[docs.length][];
        for (int w = 0; w < nVocab; ++w) {
            wordKDEs[w] = new KernelEstimator[nTopics];
            for (int t = 0; t < nTopics; ++t) {
                wordKDEs[w][t] = new KernelEstimator(p);
            }
        }
        for (int d = 0; d < docs.length; ++d) {
            docKDEs[d] = new KernelEstimator[nTopics];
            for (int t = 0; t < nTopics; ++t) {
                docKDEs[d][t] = new KernelEstimator(p);
            }
        }
    }

    /*    
    public MarginalKDE(ZDirectory zDir,double p)throws Exception {
        super(zDir);
        this.precision = p;
        wordKDEs = zDir.wordKDEs(precision);
        docKDEs = zDir.docKDEs(precision);
    }
     */
    @Override
    public String getLabel() {
        return "kde";
    }

    @Override
    public PointEstimates getEstimates(String statistic) throws Exception {
        if (statistic.equalsIgnoreCase("mean")) {
            return new PointEstimates(getMean(docKDEs), getMean(wordKDEs));
        } else if (statistic.equalsIgnoreCase("mode")) {
            return new PointEstimates(getMode(docKDEs), getMode(wordKDEs));
        }
        return null;
    }

    public double[][] getMean(KernelEstimator[][] kdes) throws Exception {
        double[][] ret = new double[kdes.length][kdes[0].length];
        for (int i = 0; i < kdes.length; ++i) {
            for (int j = 0; j < kdes[0].length; ++j) {
                
                KernelEstimator kde = kdes[i][j];
                int nKernels = kde.getNumKernels();
                double[] mu = kde.getMeans();

                double pTotal = 0.0;
                double[] p = new double[nKernels];
                for (int n = 0; n < nKernels; ++n) {
                    p[n] = kde.getProbability(mu[n]);
                    pTotal = pTotal + p[n];
                }
                double mean = 0.0;
                for (int n = 0; n < nKernels; ++n) {
                    mean = mean + mu[n] * p[n] / pTotal;
                }

                ret[i][j] = mean;
            }
        }
        return ret;
    }

    public double[][] getMode(KernelEstimator[][] kdes) throws Exception {
        double[][] ret = new double[kdes.length][kdes[0].length];
        for (int i = 0; i < kdes.length; ++i) {
            for (int j = 0; j < kdes[0].length; ++j) {
                KernelEstimator kde = kdes[i][j];
                int nKernels = kde.getNumKernels();
                double[] mu = kde.getMeans();
                double pMax = 0.0;
                int index = 0;
                for (int n = 0; n < nKernels; ++n) {
                    double p = kde.getProbability(mu[n]);
                    if (p > pMax) {
                        pMax = p;
                        index = n;
                    }
                }
                ret[i][j] = mu[index];
            }
        }
        return ret;
    }

    @Override
    public void add(Object obj) {
        if (obj instanceof MultiThreadLDA) {
            MultiThreadLDA lda = ((MultiThreadLDA) obj);
            int[][] nd = lda.getDocumentTopicCounts();
            int[][] nw = lda.getWordTopicCounts();
            add(nd, nw);
        } else {
            int[][] z = (int[][]) obj;
            add(new DocumentTopicCounts(z, nTopics).getCounts(), new WordTopicCounts(z, wordKDEs.length, nTopics, docs).getCounts());
        }
    }

    private void add(int[][] nd, int[][] nw) {

        for (int d = 0; d < nd.length; ++d) {
            for (int t = 0; t < nd[d].length; ++t) {
                int v = nd[d][t];
                KernelEstimator kde = docKDEs[d][t];
                try {
                    kde.addValue(v, 1.0);
                } catch (Exception exc) {

                    System.out.printf("Exception in MarginalKDE v=%d d=%d t=%d\n", v, d, t);
                    System.out.printf("Num Kernels = %d\n", kde.getNumKernels());
                    double[] means = kde.getMeans();
                    for (int i = 0; i < means.length; ++i) {
//                        System.out.printf("%d Mean=%f\n", i, means[i]);
                    }
                    exc.printStackTrace();
                    int sadkljfhsd = 0;
                }
            }
        }
        for (int w = 0; w < nw.length; ++w) {
            for (int t = 0; t < nw[w].length; ++t) {
                wordKDEs[w][t].addValue(nw[w][t], 1.0);
            }
        }

    }
}
