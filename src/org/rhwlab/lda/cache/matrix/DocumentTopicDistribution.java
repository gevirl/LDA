/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 *
 * @author gevirl
 */
public class DocumentTopicDistribution implements Callable {

    int[] document;
    int nIters;
    double[][] phi;  // topic word distributions from previous lda model
    double alpha;

    int T;  // number of topics
    int V;  // vocabulary size
    Random rnd;
    double alphaDivT;

    int[] z;    // topic assignment for each word in document
    int[] nd;   // number of words in document  assigned to each topic .

    public DocumentTopicDistribution(double[][] phi, double alpha, long seed,int iter) {      
        this.nIters = iter;
        this.phi = phi;
        this.alpha = alpha;
        this.rnd = new Random(seed);
        this.T = phi.length;
        this.V = phi[0].length;   
        this.alphaDivT = alpha / T;
    }

    public void setDocument(int[] document){
        this.document = document;
    }

    @Override
    public Object call() throws Exception {
        initialState();
        double maxLike = Double.NEGATIVE_INFINITY;
        double[] maxTheta = null;
        for (int i = 0; i < nIters; ++i) {
            for (int w = 0; w < document.length; ++w) {
                z[w] = sampleFullConditional(w);
            }
            double[] th = theta();
            double like = computeLikelihood(th);
            if (like > maxLike) {
                maxTheta = th;
            }
        }
        return maxTheta;
    }

    final void initialState() {
        nd = new int[T];
        z = new int[document.length];
        int N = document.length;
        for (int n = 0; n < N; n++) {
            int topic = (int) (rnd.nextDouble() * T);
            z[n] = topic;
            nd[topic]++;
        }
    }

    // sample word at position n
    private int sampleFullConditional(int n) {
        int topic = z[n];

        // remove z_i from the count variable
        nd[topic]--;

        // do multinomial sampling via cumulative method:
        double[] p = new double[T];
        for (int k = 0; k < T; k++) {
            int w = document[n];
            p[k] = phi[k][w] * (nd[k] + alphaDivT) / (document.length - 1 + alpha);
        }
        // cumulate multinomial parameters
        for (int k = 1; k < p.length; k++) {
            p[k] += p[k - 1];
        }
        // scaled sample because of unnormalised p[]
        double u = rnd.nextDouble() * p[T - 1];
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic]) {
                break;
            }
        }
        // add newly estimated z_i to count variable
        nd[topic]++;
        return topic;
    }

    public void setIterations(int iter) {
        this.nIters = iter;
    }

    public double[] theta() {

        double[] theta = new double[T];
        for (int t = 0; t < T; t++) {
            theta[t] = (nd[t] + alphaDivT) / (document.length + alpha);
        }
        return theta;
    }

    public double computeLikelihood(double[] theta) {

        double loglikelihood = 0.0;

        for (int w = 0; w < document.length; ++w) {
            double sum = 0.0;
            for (int t = 0; t < T; ++t) {
                sum += theta[t] * phi[t][document[w]];
            }
            loglikelihood += Math.log(sum);
        }
        return loglikelihood;
    }
}
