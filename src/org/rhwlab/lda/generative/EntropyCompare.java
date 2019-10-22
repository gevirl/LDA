/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.io.File;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.math3.stat.StatUtils;
import org.rhwlab.lda.evaluation.Histogram;
import org.rhwlab.lda.evaluation.UtilsIO;

/**
 *
 * @author gevirl
 */
public class EntropyCompare {

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

    static double min(double[] x) {
        double min = 0.0;
        for (int i = 0; i < x.length; ++i) {
            if (x[i] < min) {
                min = x[i];
            }
        }
        return min;
    }

    static public void main(String[] args) throws Exception {
        String prefix = "/net/waterston/vol2/home/gevirl/ldatest/peaksSynthetic";
        String iterDir = "peaksSynthetic_topics60_alpha0.050_beta0.130";
        Corpus corp = new Corpus(prefix);
        double[][] theta = UtilsIO.readRealMatrix(new File(prefix+".theta"));

        TreeMap<String, List<Integer>> byType = corp.docsByType();
        TreeMap<String, double[]> entropyByType = new TreeMap<>();
        for (String type : byType.keySet()) {
            List<Integer> list = byType.get(type);
            double[] x = new double[list.size()];
            for (int i = 0; i < x.length; ++i) {
                x[i] = entropy(theta[list.get(i)]);
            }
            entropyByType.put(type, x);
            System.out.printf("Mean = %f\n", StatUtils.mean(x));

        }
        Histogram hist = new Histogram(prefix,"Theta", "Entropy", entropyByType, 400);
        hist.setSize(500, 500);
        hist.setVisible(true);
        
        
        double[][] outTheta = UtilsIO.readRealMatrix(new File(String.format("%s/%s/kde_mode.theta",prefix,iterDir)));
        TreeMap<String, List<Integer>> byTypeOut = corp.docsByType();
        TreeMap<String, double[]> entropyByTypeOut = new TreeMap<>();
        for (String type : byTypeOut.keySet()) {
            List<Integer> list = byTypeOut.get(type);
            double[] x = new double[list.size()];
            for (int i = 0; i < x.length; ++i) {
                x[i] = entropy(outTheta[list.get(i)]);
            }
            entropyByTypeOut.put(type, x);
            System.out.printf("Mean = %f\n", StatUtils.mean(x));

        }
        Histogram histOut = new Histogram("Output","Theta", "Entropy", entropyByTypeOut, 400);
        histOut.setSize(500, 500);
        histOut.setVisible(true);        
    }
}
