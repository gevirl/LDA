/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gevirl
 */
public class MatrixMarket extends BagOfWords {
    boolean binarize = false;

    public MatrixMarket(String s,boolean binarize)throws Exception {
        this(new File(s),binarize);
    }
    public MatrixMarket(File file,boolean binarize) throws Exception {
        super(file);
        this.binarize = binarize;
    }

    @Override
    public BufferedReader readHeader() throws Exception {
        BufferedReader reader = openFile();

        // skip comments
        String line = reader.readLine();
        while (line.startsWith("%") || line.length()==0){
            line = reader.readLine();
        }
        String[] tokens = line.split("\t| ");
        this.N = Integer.parseInt(tokens[1]);
        this.V = Integer.parseInt(tokens[0]);
        this.W = Integer.parseInt(tokens[2]);
        
        return reader;
    }
    @Override
    public int[][] toDocumentFormat() throws Exception {
        System.out.printf("Reading Matrix Market file: %s\n",this.file.getPath());
        List<Integer> list = new ArrayList<>();
        int currentDoc = 1;
        total = 0;
        BufferedReader reader = readHeader();
        int[][] ret = new int[N][];
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\t| ");
            int doc = Integer.valueOf(tokens[1]);
            if (doc != currentDoc) {
                if (doc != currentDoc + 1) {
                    System.out.printf("Error. No words for document %d\n", currentDoc + 1);
                    System.exit(0);
                }
                int i = currentDoc - 1;
                listToInt(list, i, ret);
                ++currentDoc;
            }
            int n = 1;
            if (!binarize){
                n = Integer.valueOf(tokens[2]);
            }
            for (int i = 0; i < n ; ++i) {
                list.add(Integer.valueOf(tokens[0]) - 1);
                ++total;
            }
            line = reader.readLine();
        }
        listToInt(list, currentDoc - 1, ret);
        reader.close();
        return ret;
    }
}
