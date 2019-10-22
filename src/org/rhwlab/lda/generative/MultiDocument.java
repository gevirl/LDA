/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author gevirl
 */
public class MultiDocument implements Document {
    List<Document> docList;
    
    public MultiDocument(List<Document> docList){
        this.docList = docList;
    }
    
    @Override
    public TreeMap<Integer, Integer> getWordMap(double[][] theta, double[][] phi) {
        TreeMap<Integer,Integer> map = new TreeMap<>();
        for (Document doc : docList){
            TreeMap<Integer,Integer> docMap = doc.getWordMap(theta, phi);
            for (Integer vocab : docMap.keySet()){
                Integer count = map.get(vocab);
                if (count != null){
                    count = count + docMap.get(vocab);
                } else {
                    count = docMap.get(vocab);
                }
                map.put(vocab,count);
            }
        }
        return map;
    }

    @Override
    public String identity() {
        StringBuilder builder = new StringBuilder();
        builder.append(docList.get(0).identity());
        for (int i=1 ; i<docList.size() ; ++i){
            builder.append("\t");
            builder.append(docList.get(i).identity());
        }
        return builder.toString();
    }

    @Override
    public double[] getTopicDist(double[][] theta) {
        double n = this.getWordCount();
        double[] dist = new double[theta[0].length];
        
        for (Document doc : docList){
            double w = ((double)doc.getWordCount())/n;
            double[] docDist = doc.getTopicDist(theta);
            for (int i=0 ; i<dist.length ; ++i){
                dist[i] = dist[i] + w*docDist[i];
            }
        }
        return dist;
    }

    @Override
    public int getWordCount() {
        int total = 0;
        for (Document doc : docList){
            total = total + doc.getWordCount();
        }
        return total;
    }

    @Override
    public List<Integer> getType() {
        List<Integer> ret = new ArrayList<>();
        for (Document doc : this.docList){
            ret.add(doc.getType().get(0));
        }
        return ret;
    }
    
}
