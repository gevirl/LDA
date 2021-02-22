/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author gevirl
 */
public class VocabFile {
    File file;
    String[] words;
    TreeMap<String,Integer> map = new TreeMap<>();
    
    public VocabFile(File file,int col)throws Exception{
        this.file = file;
        readFile(col);
    }
    
    public void readFile(int col)throws Exception {
        ArrayList<String> list = new ArrayList<>();
        BufferedReader reader = BagOfWords.openFile(file);
        String line = reader.readLine();
        int i =0;
        while (line != null){
            String[] tokens = line.split("\t");
            list.add(tokens[col]);
            map.put(tokens[col], i);
            ++i;
            line = reader.readLine();
        }
        reader.close();
        words = list.toArray(new String[0]);
    }
 
    // return the genes in theremaoped order
    public String[] reorder(int[] index){
        String[] ret = new String[index.length];
        for (int i=0 ; i<index.length ; ++i){
            ret[i] = words[index[i]];
        }
        return ret;
    }
    
    public Integer getIndex(String w){
        return map.get(w)+1;
    }
    
    public Set<Integer> getIndex(File f)throws Exception {
        HashSet<Integer> ret = new HashSet<>();
        BufferedReader reader = BagOfWords.openFile(f);
        String line = reader.readLine();
        while (line != null){
            ret.add(getIndex(line));
            line = reader.readLine();
        }
        reader.close(); 
        return ret;
    }
}
