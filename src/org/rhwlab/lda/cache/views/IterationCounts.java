/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gevirl
 */
public class IterationCounts {
    File[] files;
    int[] columns;
    
    public IterationCounts(File[] topicFiles,int[] columns){
        this.files = topicFiles;
        this.columns = columns;
    }
    
    public List<int[]>[] getCounts()throws Exception {
        List<int[]>[] ret = new List[columns.length];  // a list for each column
        for (int i=0 ; i<ret.length ; ++i){
            ret[i] = new ArrayList<>();
        }
        
        BufferedReader[] readers = new BufferedReader[files.length];
        for (int i=0 ; i<readers.length ; ++i){
            readers[i] = new BufferedReader(new FileReader(files[i]));
        }
        
        boolean eof = false;
        while(!eof){
            int[][] values = new int[columns.length][files.length];

            for (int topic=0 ; topic<readers.length ; ++topic){
                String line = readers[topic].readLine();
                if (line == null){
                    eof = true;
                    break;
                }
                String[] tokens = line.split(",");
                for (int c =0 ; c<columns.length ; ++c){
                    values[c][topic] = Integer.valueOf(tokens[columns[c]]);
                }
            }
            if (!eof){
                for (int c=0 ; c<columns.length ; ++c){
                    ret[c].add(values[c]);
                }
            }
        }
        return ret;
    }
}
