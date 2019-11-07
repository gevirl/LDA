/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.File;
import java.io.PrintStream;
import org.rhwlab.lda.cache.PointEstimates;


/**
 *
 * @author gevirl
 */
abstract public class PointEstimatorDistBase implements PointEstimateDistribution {

    ZDirectory zDir;

    public PointEstimatorDistBase() {

    }

    public PointEstimatorDistBase(ZDirectory zDir) {
        this.zDir = zDir;
    }

    public void add(int[][][] z) {
        for (int i=0 ; i<z.length ; ++i){
            add(z[i]);
        }
    }

    @Override
    public void statisticReport(File dir, String statistic, int skip, RowSumMatrix alpha, RowSumMatrix beta, long totalWords, int[][] docs, int lastIter, int nTopics, int nVocab) throws Exception {
        // report the document point estimates
        System.out.println("Reporting the document/topic counts");
        PrintStream stream = new PrintStream(new File(dir, String.format("%s_%s.docTopic", getLabel(), statistic)));
        PointEstimates est = getEstimates(statistic);
        printMatrix(stream, est.docEst);
        stream.close();

        System.out.println("Reporting the theta array - document/topic distributions");
        stream = new PrintStream(new File(dir, String.format("%s_%s.theta", getLabel(), statistic)));
        double[][] theta = Likelihood.theta(est.docEst, alpha);
        printMatrix(stream, theta);

        // report the word point estimates
        System.out.println("Reporting the word/topic counts");
        stream = new PrintStream(new File(dir, String.format("%s_%s.wordTopic", getLabel(), statistic)));
        printMatrix(stream, est.wordEst);
        stream.close();

        System.out.println("Reporting the phi array - topic/word distributions");
        stream = new PrintStream(new File(dir, String.format("%s_%s.phi", getLabel(), statistic)));
        double[][] phi = Likelihood.phi(est.wordEst, beta);
        printMatrix(stream, phi);

        double logLike = Likelihood.computeLikelihood(docs, phi, theta);
        double perplexity = Math.exp(-logLike / (double) totalWords);

        System.out.println("Reporting the lda run summary");
        stream = new PrintStream(new File(dir, String.format("%s_%s.summary", getLabel(), statistic)));
        stream.printf("Iterations : %d\n", lastIter);
        stream.printf("Skip : %d\n", skip);
        stream.printf("Topics : %d\n", nTopics);
        stream.printf("Documents : %d\n", docs.length);
        stream.printf("Vocabulary : %d\n", nVocab);
        stream.printf("Total Words : %d\n", totalWords);
        stream.printf("Alpha : %f\n", alpha.getConcentration());
        stream.printf("Beta : %f\n", beta.getConcentration());
        stream.printf("LogLikelihood : %e\n", logLike);
        stream.printf("Perplexity : %e\n", perplexity);
        stream.close();
    }

    static void printMatrix(PrintStream stream, double[][] est) {
        int nCol = est[0].length;
        for (int i = 0; i < est.length; ++i) {
            stream.printf("%f", est[i][0]);
            for (int j = 1; j < nCol; ++j) {
                stream.printf(",%f", est[i][j]);
            }
            stream.println();
        }
    }
}
