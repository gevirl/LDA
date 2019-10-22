/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.evaluation;

import java.io.File;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.rhwlab.lda.generative.Corpus;
import smile.math.distance.CorrelationDistance;
import smile.math.distance.Distance;
import smile.math.distance.EuclideanDistance;
import smile.math.distance.JensenShannonDistance;

/**
 *
 * @author gevirl
 */
public class RNASeqCompare {

    // compute entropy of the rows of an array
    static double[] entropy(double[][] x) {
        double[] ret = new double[x.length];

        for (int r = 0; r < x.length; ++r) {

            ret[r] = entropy(x[r]);
        }
        return ret;
    }

    static double min(double[] x) {
        double min = 0.0;
        for (int i = 0; i < x.length; ++i) {
            if (x[i] < min) {
                min = x[i];
            }
        }
        return min;
    }

    static double entropy(double[] x) {
        double e = 0.0;
        double mn = min(x);
        for (int i = 0; i < x.length; ++i) {
            if (x[i] > 0.0) {
                double lg = Math.log(x[i]);
                if (Double.isFinite(lg)) {
                    e = e - x[i] * lg;
                }
            }
        }
        return e;
    }

    static public void main(String[] args) throws Exception {
        Class jds = JensenShannonDistance.class;
        Class euclid = EuclideanDistance.class;
        Class corr = CorrelationDistance.class;

        String corpus = "RNASeqDoubletAlphaLow";
        String phiDir = "RNASeqDoubletAlphaLow_topics60_alpha0.010_beta0.100";
        String prefix = "/net/waterston/vol2/home/gevirl/ldatest";
/*      
        String corpus = "RNASeqDoublet";
        String phiDir = "RNASeqDoublet_topics60_alpha0.010_beta0.100";
        String prefix = "/net/waterston/vol2/home/gevirl/ldatest";        
 */       
        Corpus corp = new Corpus(prefix + "/" + corpus);
        Distance measure = (Distance) jds.newInstance();
        double[][] theta1 = UtilsIO.readRealMatrix(
                new File(String.format("%s/%s.theta",prefix,corpus)));
        double[][] theta2 = UtilsIO.readRealMatrix(
                new File(String.format("%s/%s/%s/kde_mode.theta",prefix,corpus,phiDir)));
        
       

        double[][] phi1 = UtilsIO.readRealMatrix(
                new File(String.format("%s/%s.phi",prefix,corpus)));

        double[][] phi2 = UtilsIO.readRealMatrix(
                new File(String.format("%s/%s/%s/kde_mode.phi",prefix,corpus,phiDir)));
        PhiComparison phiComp = new PhiComparison(phi1, phi2);



        double thresh = 0.2;
        ThetaComparison thetaComp = new ThetaComparison(theta1, theta2, phiComp, thresh, measure);

        List<double[]> distList = thetaComp.distancesByType(corp);

        //       TTest tt = new TTest();
        //       double pValue = tt.tTest(distList.get(0), distList.get(1));
        TreeMap<String, List<Integer>> map = corp.docsByType();
        double[] e2 = entropy(theta2);

        System.out.printf("reordered mean = %f\n", thetaComp.getMean());

        TopicMatch[] matches = phiComp.getMatches();
        int count = 0;
        TreeSet<Integer> unique = new TreeSet<>();
        for (int i = 0; i < matches.length; ++i) {
            if (matches[i].jsd <= thresh) {
                unique.add(matches[i].topic);
                ++count;
            }
        }
        System.out.printf("Matching phi = %d\n", count);
        System.out.printf("Unique matches = %s\n", unique.size());
        thetaComp = new ThetaComparison(theta1, theta2, null, 1.0, measure);
        System.out.printf("unajusted mean = %f\n", thetaComp.getMean());
        int ioasdjfiojsd = 0;
    }
}
