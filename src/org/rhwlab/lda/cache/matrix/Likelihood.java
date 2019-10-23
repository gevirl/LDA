/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.util.concurrent.Callable;

/**
 *
 * @author gevirl
 */
public class Likelihood implements Callable<Double> {
    RowSumMatrix alpha;
    RowSumMatrix beta;
    int[][] nw;
    int[][]nd;
    int[][] docs;
    int iter;
    
    public Likelihood(int[][]docs,int[][]nw,int[][] nd,RowSumMatrix alpha,RowSumMatrix beta,int iter){
        this.alpha = alpha;
        this.beta = beta;
        this.nw = nw;
        this.nd = nd;
        this.docs = docs;
        this.iter = iter;
    }
    @Override
    public Double call() throws Exception {
        System.out.printf("Likelihood started on iteration %d\n",iter);
        double[][] phi = phi(nw, beta);
        double[][] theta = theta(nd, alpha);
        Double ret =  computeLikelihood(docs, phi, theta);
        System.out.printf("Likelihood finished on iteration %d\n",iter);
        return ret;
    }

    static public double[][] phi(int[][] nw, RowSumMatrix beta) {
        return phi(asDouble(nw), beta);
    }

    static public double[][] phi(double[][] nw, RowSumMatrix beta) {
        // nw - VxK
        // phi - KXV
        // nwsum - K
        double[] nwsum = new double[nw[0].length];
        for (int k = 0; k < nwsum.length; ++k) {
            double sum = 0;
            for (int v = 0; v < nw.length; ++v) {
                sum = sum + nw[v][k];
            }
            nwsum[k] = sum;
        }
        return phi(nw, nwsum, beta);
    }

    static public double[][] phi(double[][] nw, double[] nwsum,RowSumMatrix beta) {
        int K = nw[0].length;
        int V = nw.length;
        double[][] phi = new double[K][V];
        for (int k = 0; k < K; k++) {
            for (int w = 0; w < V; w++) {
                phi[k][w] = (nw[w][k] + beta.getValues()[k][w]) / (nwsum[k] + beta.getSums()[k]);
            }
        }
        return phi;
    }

    static public double[][] theta(int[][] nd, RowSumMatrix alpha) {
        return theta(asDouble(nd), alpha);
    }

    static public double[][] theta(double[][] nd, RowSumMatrix alpha) {
        // nd = DxK
        // theta - DxK
        // ndsum - D
        int D = nd.length;
        int K = nd[0].length;
        double[] ndsum = new double[D];
        for (int d = 0; d < D; ++d) {
            double sum = 0;
            for (int k = 0; k < K; ++k) {
                sum = sum + nd[d][k];
            }
            ndsum[d] = sum;
        }
        return theta(nd, ndsum, alpha);
    }

    static public double[][] theta(double[][] nd, double[] ndsum, RowSumMatrix alpha) {
        int D = nd.length;
        int K = nd[0].length;
        double[][] theta = new double[D][K];

        for (int m = 0; m < D; m++) {
            for (int k = 0; k < K; k++) {
                theta[m][k] = (nd[m][k] + alpha.getValues()[m][k]) / (ndsum[m] + alpha.getSums()[m]);
            }
        }
        return theta;
    }    
    static double[][] asDouble(int[][] n) {
        int nCols = n[0].length;
        double[][] ret = new double[n.length][nCols];
        for (int r = 0; r < n.length; ++r) {
            for (int c = 0; c < nCols; ++c) {
                ret[r][c] = n[r][c];
            }
        }
        return ret;
    }

    static public double computeLikelihood(int[][] docs, double[][] phi, double[][] theta) {
        int K = phi.length;
        double loglikelihood = 0.0;
        for (int d = 0; d < docs.length; ++d) {
            for (int w = 0; w < docs[d].length; ++w) {
                int v = docs[d][w];
                double sum = 0.0;
                for (int t = 0; t < K; ++t) {
                    sum += theta[d][t] * phi[t][v];
                }
                loglikelihood += Math.log(sum);
            }
        }
        return loglikelihood;
    }    
}
