/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gevirl
 */
public class BagOfWordsList {
    List<BagOfWords> bowList = new ArrayList<>();
    
    int nDocs;
    int nVocab;
    int nTotal;

    public BagOfWordsList(){
        
    }
    public BagOfWordsList(List<BagOfWords> bows){
        for (BagOfWords bow : bows){
            bowList.add(bow);
            nDocs = nDocs + bow.N;
            nVocab = bow.V;
            nTotal = nTotal + bow.W;
        }
    }
    static public BagOfWordsList factory(List<File> bows,String cl,boolean bin)throws Exception{
        if (bows.isEmpty()) return null;
        if (!BagOfWords.checkClass(cl)) return null;
        
        // check the vocabulary size of all the bows       
        int nV = BagOfWords.factory(bows.get(0), cl, bin).getVocabSize();
        for (int i=1 ; i<bows.size() ; ++i){
            if (nV != BagOfWords.factory(bows.get(i), cl, bin).getVocabSize()){
                return null; // cannot make the list of bows
            }
        }
        
        // construct the list of bows
        BagOfWordsList ret = new BagOfWordsList();
        for (int i=0 ; i<bows.size() ; ++i){
            ret.bowList.add(BagOfWords.factory(bows.get(i), cl, bin));
        }
        
        // calculate the sizes
        ret.nVocab = ret.bowList.get(0).V;
        for (BagOfWords bow : ret.bowList) {
            ret.nDocs = ret.nDocs + bow.N;
            ret.nTotal = ret.nTotal + bow.W;
        }
        return ret;
    }
    
    public int[][] toDocumentFormat() throws Exception {
        int[][] ret = new int[nDocs][];
        int i = 0;
        for (BagOfWords bow : bowList) {
            int[][] docs = bow.toDocumentFormat();
            for (int j = 0; j < docs.length; ++j) {
                ret[i] = docs[j];
                ++i;
            }
        }
        return ret;        
    }
    
   
    public int getDocsN(){
        return this.nDocs;
    }
    public int getVocabN(){
        return this.nVocab;
    }
    public int getTotalN(){
        return this.nTotal;
    }
    public String getID(){
        return this.bowList.get(0).file.getName().replace(".bow", "");
    }
    public List<BagOfWords> getList(){
        return this.bowList;
    }
    
    // combine multiple bag of word files
    // args[0] - file with list of bow paths
    // args[1] - class of the bow files
    // args[2] - path of output file
    public static void main(String[] args)throws Exception{
        String bowClass = args[1];
        File outFile = new File(args[2]);
        
        ArrayList<File> files = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String path = reader.readLine();
        while(path != null){
            files.add(new File(path.trim()));
            path = reader.readLine();
        }
        reader.close();
        
        BagOfWordsList list = BagOfWordsList.factory(files, bowClass, false);
        int[][] docs = list.toDocumentFormat();
        
        if (bowClass.contains("MatrixMarket")){
            MatrixMarket.saveAsBagOfWordsFile(docs, list.getVocabN(), list.getTotalN(), outFile);
        } else if (bowClass.contains("OriginalBOW")){
            OriginalBOW.saveAsBagOfWordsFile(docs, list.getVocabN(), list.getTotalN(), outFile);
        }
    }
}
