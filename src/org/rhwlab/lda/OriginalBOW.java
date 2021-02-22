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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;
import org.jdom2.Element;
import static org.rhwlab.lda.BagOfWords.asMap;

/**
 *
 * @author gevirl
 */
public class OriginalBOW extends BagOfWords {
    
    public OriginalBOW(Element ele)throws Exception {
        this(ele.getAttributeValue("file"),BagOfWords.isBinarized(ele));
    }
    public OriginalBOW(String file,boolean binarize)throws Exception {
        this(new File(file),binarize);
    }
    public OriginalBOW(File file,boolean binarize)throws Exception {
        super(file,binarize);
        docColumn = 0;
        wordColumn = 1;
        countColumn = 2;                
    }
    
    public BufferedReader readHeader() throws Exception {
        BufferedReader reader = openFile();
        N = Integer.valueOf(reader.readLine());
        V = Integer.valueOf(reader.readLine());
        W = Integer.valueOf(reader.readLine()); 
        return reader;
    }    
    
    static public void saveAsBagOfWordsFile(List docs, int V,int total,File file) throws Exception {
        int i=0;
        int[][] docArray = new int[docs.size()][];
        for (Object obj : docs){
            docArray[i] = (int[])obj;
            ++i;
        }
        saveAsBagOfWordsFile(docArray,V,total,file);
    }
    static public void saveAsBagOfWordsFile(int[][] docs, int V,int total,File file) throws Exception {
        PrintStream stream = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));
        stream.printf("%d,%d,%d\n", docs.length, V, total);
        for (int d = 0; d < docs.length; ++d) {
            for (Map.Entry e : asMap(docs[d]).entrySet()) {
                int w = (Integer) e.getKey() + 1;
                int count = (Integer) e.getValue();
                stream.printf("%d,%d,%d\n", d + 1, w, count);
            }
        }
        stream.close();
    }    
    @Override
    public void saveTo(ArrayList<TreeMap<Integer, Integer>> docList, int nLines, PrintStream stream) {
        stream.printf("%d,%d,%d\n", docList.size(),V,nLines);
        for (int i=0 ; i<docList.size() ; ++i){
            TreeMap<Integer,Integer> map = docList.get(i);
            for (Integer vocab : map.descendingKeySet()){
                stream.printf("%d,%d,%d,\n", i+1, 1+vocab  , map.get(vocab));
            }
        }
    }  
    public void saveTo(List<int[]> recs,PrintStream stream){
        stream.printf("%d %d %d\n", V,N,W);
        for (int[] rec : recs){
            stream.printf("%d %d %d\n",rec[0],rec[1],rec[2]);
        }
    }
    
    static public void saveTo(List<int[]> recs,int V,int W,PrintStream stream){
        stream.printf("%d %d %d\n", V,recs.size(),W);
        for (int[] rec : recs){
            stream.printf("%d %d %d\n",rec[0],rec[1],rec[2]);
        }
    }     
}
