/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author gevirl
 */
public class UtilsIO {
    static public double[][] readRealMatrix(File file)throws Exception {
        ArrayList<double[]> list = new ArrayList<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null){
            String[] tokens = line.split(",| |\t");
            double[] v = new double[tokens.length];
            for (int i=0 ; i<tokens.length ; ++i){
                v[i] = Double.valueOf(tokens[i]);
            }
            list.add(v);
            line = reader.readLine();
        }
        reader.close();
        
        double[][] ret = new double[list.size()][];
        for (int i=0 ; i<ret.length ; ++i){
            ret[i] = list.get(i);
        }
        return ret;
    }
    static public void main(String[] args) throws Exception {
        double[][] wordkde = readRealMatrix(
                new File("/net/waterston/vol2/home/gevirl/ldatest/zcacheout/topics_50/test_topics50_alpha0.010_beta0.010/KDE_mode.wordTopic"));
        double[][] wordtopic  = readRealMatrix(
                new File("/net/waterston/vol2/home/gevirl/ldatest/zcacheout/topics_50/test_topics50_alpha0.010_beta0.010/Topic_mode.wordTopic")); 
        
        double[][] doctopic  = readRealMatrix(
                new File("/net/waterston/vol2/home/gevirl/ldatest/zcacheout/topics_50/test_topics50_alpha0.010_beta0.010/Topic_mode.docTopic")); 
        double[][] dockde  = readRealMatrix(
                new File("/net/waterston/vol2/home/gevirl/ldatest/zcacheout/topics_50/test_topics50_alpha0.010_beta0.010/KDE_mode.docTopic")); 
        
        double sumWordKDE = 0.0;
        double sumWordTopic = 0.0;
        for (int i=0 ; i<wordkde.length ; ++i){
            for (int j = 0; j<wordkde[0].length ; ++j){
                sumWordKDE = sumWordKDE + wordkde[i][j];
                sumWordTopic = sumWordTopic + wordtopic[i][j];
            }
        }
        
        double sumDocKDE = 0.0;
        double sumDocTopic = 0.0;
        for (int i=0 ; i<dockde.length ; ++i){
            for (int j = 0; j<dockde[0].length ; ++j){
                sumDocKDE = sumDocKDE + dockde[i][j];
                sumDocTopic = sumDocTopic + doctopic[i][j];
            }
        }        
        
        int sdfjiosdhfui=0;
    }
}
