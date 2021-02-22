/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author gevirl
 */
public class FilterMerged {
    static public void main(String[] args) throws Exception{
        File inMM = new File("/net/waterston/vol9/flyRNA/nn_multi_Wards_50000_merge_4_broad_matrix.mtx.gz");
        MatrixMarket mm = new MatrixMarket(inMM,false);
        int[][] docs = mm.toDocumentFormat();
        
        int total =0;
        File mergedDocsFile = new File("/net/waterston/vol9/flyRNA/nn_multi_Wards_50000_merge_4_docs_broad_matrix.mtx.gz");
        PrintStream stream = 
                new PrintStream(new GZIPOutputStream(new FileOutputStream("/net/waterston/vol9/flyRNA/nn_multi_Wards_50000_merge_4_filter_docs_broad_matrix.mtx.gz")));  
        BufferedReader reader = BagOfWords.openFile(mergedDocsFile);
        int i=0;
        ArrayList<int[]> toKeep = new ArrayList<>();
        String line = reader.readLine();
        while (line != null){
            if (line.contains("_")){
                stream.println(line);
                
                TreeMap<Integer,Integer> map = BagOfWords.asMap(docs[i]);
                toKeep.add(docs[i]);
                total = total + map.size();
            }
            ++i;
            line = reader.readLine();
        }
        reader.close();
        stream.close();
//
        MatrixMarket.saveAsBagOfWordsFile(toKeep, mm.V, total, new File("/net/waterston/vol9/flyRNA/nn_multi_Wards_50000_merge_4_filter_broad_matrix.mtx.gz"));

        int asdhfiusah=0;
    }
}
