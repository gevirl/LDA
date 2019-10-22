/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.apache.commons.math3.special.Gamma;
//import org.rhwlab.sparcematrix.DbSparceMatrix;

/**
 *
 * @author gevirl
 */
public class ChibEstimator implements Callable {

    double[][] phi;  // topic x vocab -> prob
    double alpha;  // document dirichlet distribution prior
    int nIters = 1000;
    int burnin = 200;
    int optimizeIters = 20;
    Random rnd;
    double[] logTranProb; // transition prob from each z to ZStar 

    int[] document; // the word identity for each position in the document
    int T;  // number of topics
    int Nd;  // number of words in the document
    int V;
    int[] Nz; // number of words for each topic
    int[] z;  // the current topic choice for each word in the document, dim = Nd
    int[] NzStar;
    int[] zStar;
    int[] Nzs;
    int[] zs;

    public ChibEstimator(double[][] phi, double alpha, long seed) {
        this.phi = phi;
        this.alpha = alpha;
        this.rnd = new Random(seed);
        this.T = phi.length;
        V = phi[0].length;
    }

    public void setDocument(int[] doc){
        this.document = doc;
    }
    public void setIterations(int iter){
        this.nIters = iter;
    }
    public void setBurnin(int bi){
        this.burnin = bi;
    }
    @Override
    public Object call() throws Exception {

        return estimateLogProb();
    }
    
    public int[] getTopicWordCounts() {
        return Nz;
    }

    // document = the list of word identities
    public double estimateLogProb() {
        Nz = new int[T];
        Nzs = new int[T];
        NzStar = new int[T];
        logTranProb = new double[nIters];
        this.document = document;
        Nd = document.length;

        init();  //calc zStar and the zs (topic assignments for random middle iteration)

        int S = rnd.nextInt(nIters);
        logTranProb[S] = logTransitionProb(zStar, zs, Nzs);

        // sample forward from the middle
        for (int iter = S + 1; iter < nIters; ++iter) {
            sampleForward();
            logTranProb[iter] = logTransitionProb(zStar, z, Nz);
        }
        // go back to the middle
        for (int pos = 0; pos < Nd; ++pos) {
            z[pos] = zs[pos];
        }
        for (int t=0;t<T ; ++t){
            Nz[t]= Nzs[t];
        }

        // sample backwards
        for (int iter = S - 1; iter >= 0; --iter) {
            sampleBackward();
            logTranProb[iter] = logTransitionProb(zStar, z, Nz);
        }

        // calculate the P(zStar) from equation (5)
        double logPzStar = Gamma.logGamma(T * alpha) - Gamma.logGamma(Nd + T * alpha);
//        double logPzStar = Gamma.logGamma(V * alpha) - Gamma.logGamma(Nd + V * alpha);
        for (int t = 0; t < T; ++t) {
            logPzStar = logPzStar + Gamma.logGamma((double) NzStar[t] + alpha);
        }
        logPzStar = logPzStar - T * Gamma.logGamma(alpha);

        // calculate P(w|zStar)
        double logP_wGivenZ = 0.0;
        for (int pos = 0; pos < Nd; ++pos) {
            int topic = zStar[pos];
            int vocab = document[pos];
            logP_wGivenZ = logP_wGivenZ + Math.log(phi[topic][vocab]);
        }

        // calculate the joint P(w,zStar)
        double logP_joint = logPzStar + logP_wGivenZ;

        // calculate average transition probability
        double logSum = logSumExp(this.logTranProb);
        logSum = logSum - Math.log(nIters);

        return logP_joint - logSum;
    }

    //initiaialize z and zStar
    public void init() {
        zStar = new int[Nd];
        zs = new int[Nd];
        z = new int[Nd];

        // randomly assign topics for each word based on the phi, the topic word distributions
        for (int pos = 0; pos < Nd; ++pos) {
            int vocab = document[pos];
            double[] p = new double[T];
            for (int t = 0; t < T; ++t) {
                p[t] = phi[t][vocab];
            }
            z[pos] = sample(p);
            ++Nz[z[pos]];
        }

        // Gibbs sample forward a few iterations
        for (int i = 0; i < burnin; ++i) {
            sampleForward();
        }

        // locally optimize to find zStar
        for (int i = 0; i < optimizeIters; ++i) {
            optimize();
        }
        // save zStar and NzStar
        for (int pos = 0; pos < Nd; ++pos) {
            zStar[pos] = z[pos];
        }
        for (int t = 0; t < T; ++t) {
            NzStar[t] = Nz[t];
        }
        // sample backward once to get the middle topic assignments
        sampleBackward();

        // save the middle topic assignments in zs and Nzs
        for (int pos = 0; pos < Nd; ++pos) {
            zs[pos] = z[pos];
        }
        for (int t = 0; t < T; ++t) {
            Nzs[t] = Nz[t];
        }
    }

    public void sampleForward() {
        for (int pos = 0; pos < Nd; ++pos) {
            sample(pos);
        }
    }

    public void sampleBackward() {
        for (int pos = Nd - 1; pos >= 0; --pos) {
            sample(pos);
        }
    }

    public void optimize() {
        for (int pos = 0; pos < Nd; ++pos) {
            // remove the topic at the given word position  from the counts
            --Nz[z[pos]];

            if (Nz[z[pos]] < 0){
                int usdu = 0;
            }
            double[] p = probZ(document[pos], Nz);

            // find topic with maximum probability and choose that topic
            double max = 0.0;
            int topic = -1;
            for (int t = 0; t < T; ++t) {
                if (p[t] > max) {
                    max = p[t];
                    topic = t;
                }
            }
            z[pos] = topic;

            // add in the new choosen topic for the word position
            ++Nz[z[pos]];
        }

    }

    // sample a topic for a word position in the document 
    public void sample(int pos) {

        // remove the topic at the given word position  from the counts
        --Nz[z[pos]];

            if (Nz[z[pos]] < 0){
                int usdu = 0;
            }
        // sample a topic for the word
        z[pos] = sample(probZ(document[pos], Nz));

        // add in the new choosen topic for the word position
        ++Nz[z[pos]];

    }

    public double logTransitionProb(int[] zto, int[] zfrom, int[] Nin) {
        int[] N = Arrays.copyOf(Nin, Nin.length);
        double ret = 0.0;
        for (int pos = 0; pos < Nd; ++pos) {
            N[zfrom[pos]] = N[zfrom[pos]] - 1;
            double[] pz = norm(probZ(document[pos], N));
            ret = ret + Math.log(pz[zto[pos]]);
            N[zto[pos]] = N[zto[pos]] + 1;
        }
        return ret;
    }

    // the topic probability distribution for given a vocabulary word (not normalized)
    // based on the topic word distribution and the given counts of each topic in the document
    public double[] probZ(int vocab, int[] N) {
        double[] pz = new double[T];  // a prob for each topic
        for (int t = 0; t < T; ++t) {
            pz[t] = probZ(t, vocab, N);

        }
        return pz;
    }

    public double probZ(int topic, int vocab, int[] N) {
        double p = phi[topic][vocab] * (N[topic] + alpha);
        if (p < 0.0) {
            int iuashdfui = 0;
        }
        return p;
    }

    // sample the discrete prob distibution
    // input does not have to be normalized
    // dimension of p is number of topics
    public int sample(double[] p) {
        double[] cumm = new double[p.length];
        cumm[0] = p[0];
        for (int i = 1; i < p.length; ++i) {
            cumm[i] = cumm[i - 1] + p[i];
        }
        double value = rnd.nextDouble() * cumm[p.length - 1];
        for (int i = 0; i < p.length; ++i) {
            if (value <= cumm[i]) {
                return i;
            }
        }
        return -1; //error
    }

    // normalize to sum of one
    static double[] norm(double[] p) {
        double[] ret = new double[p.length];

        double sum = 0.0;
        for (int i = 0; i < p.length; ++i) {
            sum = sum + p[i];
        }

        for (int i = 0; i < p.length; ++i) {
            ret[i] = p[i] / sum;
        }
        return ret;
    }

    // numerically stable log ( sum( exp (logValues)))
    // ie the log of the sum of values, given the log of the values
    static public double logSumExp(double[] logValues) {
        double ret = logSumExp(logValues[0], logValues[1]);
        for (int i = 2; i < logValues.length; ++i) {
            ret = logSumExp(ret, logValues[i]);
        }
        return ret;
    }

    static public double logSumExp(double logv1, double logv2) {
        return Utils.elnsum(logv1, logv2);
    }
/*
    static public void test()throws Exception {
        String prefix = "/net/waterston/vol2/home/gevirl/CaoL2/All_10_70_0.010";
        
        // read the summary file
        TreeMap<String,String> summaryMap = new TreeMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(prefix+".summary"));
        String line = reader.readLine();
        while (line != null){
            String[] tokens = line .split(":");
            summaryMap.put(tokens[0].trim(),tokens[1].trim());
            line = reader.readLine();
        }
        reader.close();
        
        double alpha = Double.valueOf(summaryMap.get("Alpha"));
                
        // read the phi file
        int nTopics = Integer.valueOf(summaryMap.get("Topics"));
        double[][] phi = new double[nTopics][];
        int v=0;
        reader = new BufferedReader(new FileReader(prefix+".phi"));
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            double[] topicPhi = new double[tokens.length];
            for (int i = 0; i < topicPhi.length; ++i) {
                topicPhi[i] = Double.valueOf(tokens[i]);
            }
            phi[v] = topicPhi;
            ++v;
            line = reader.readLine();
        }
        reader.close();
        
        ChibEstimator estimator = new ChibEstimator(phi,alpha,1000);

        
        DbSparceMatrix mat = new DbSparceMatrix("CaoL2",new File(prefix+".docs"),new File(prefix+".voc"));
        String[] docs = mat.getRowNames();
        String[] subDocs = Arrays.copyOf(docs, 20);
        
        DbSparceMatrix subMat = (DbSparceMatrix)mat.subsetRowsAsMatrix(subDocs);
        
        int[][] documents = subMat.ldaDocumentFormat();
        
        // process each document
        PrintStream stream = new PrintStream(prefix+".chib");
        for (int d=0 ; d<documents.length ; ++d){
            estimator.setDocument(documents[d]);
            double logProb = estimator.estimateLogProb();
            int[] counts = estimator.getTopicWordCounts();
            stream.printf("%d", counts[0]);
            for (int j = 1; j < counts.length; ++j) {
                stream.printf(",%d", counts[j]);
            }
            stream.println();
            System.out.printf("%f\n", logProb);            
        }
        stream.close();
    }
    
*/
    // args[0] -  phi file
    // args[1] -  alpha
    // args[2] -  bag of words file
    // args[3] -  seed
    // args[4] -  output file
    static public void main(String[] args) throws Exception {
       
        // read the phi file
        ArrayList<double[]> phiList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            double[] topicPhi = new double[tokens.length];
            for (int i = 0; i < topicPhi.length; ++i) {
                topicPhi[i] = Double.valueOf(tokens[i]);
            }
            phiList.add(topicPhi);
            line = reader.readLine();
        }
        reader.close();
        double[][] phi = new double[phiList.size()][];
        for (int i = 0; i < phi.length; ++i) {
            phi[i] = phiList.get(i);
        }

        double alpha = Double.valueOf(args[1]);

        int[][] docs = new BagOfWords(args[2]).toDocumentFormat();

        long seed = Long.valueOf(args[3]);

        ChibEstimator estimator = new ChibEstimator(phi, alpha, seed);

        PrintStream stream = new PrintStream(args[4]);
        // estimator log prob of each document given the phi and alpha
        for (int i = 0; i < docs.length; ++i) {
            estimator.setDocument(docs[i]);
            double logProb = estimator.estimateLogProb();
/*            
            int[] counts = estimator.getTopicWordCounts();
            stream.printf("%d", counts[0]);
            for (int j = 1; j < counts.length; ++j) {
                stream.printf(",%d", counts[j]);
            }
            stream.println();
*/
            stream.printf("%f\n", logProb);
            int uaisdfuisduf = 0;
        }
        stream.close();
       
    }


}
