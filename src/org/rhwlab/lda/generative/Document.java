/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author gevirl
 */
public interface Document {
    public TreeMap<Integer,Integer> getWordMap(double[][] theta,double[][] phi);
    public String identity();
    public double[] getTopicDist(double[][] theta);
    public int getWordCount();
    public List<Integer> getType();
}
