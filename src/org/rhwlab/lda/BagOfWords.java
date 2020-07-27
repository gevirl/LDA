/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 *
 * @author gevirl
 */
public class BagOfWords {

    File file;
    int N;
    int V;
    int W;  // number of line in file (less the header)
    int total;  // total number of words in the data

    public BagOfWords(String fileStr)throws Exception {
        this(new File(fileStr));
    }
    public BagOfWords(File file) throws Exception {
        this.file = file;
        BufferedReader reader = readHeader();
        reader.close();
    }

    // partition this bow randomly
    public File[] partition(int nPart, File dir, Random rnd) throws Exception {
        File[] f = new File[nPart];
        ArrayList[] lists = new ArrayList[nPart];
        for (int i = 0; i < nPart; ++i) {
            lists[i] = new ArrayList<>();
        }
        // split up the docs
        int[][] docs = toDocumentFormat();
        for (int i = 0; i < docs.length; ++i) {
            int index = rnd.nextInt(nPart);
            lists[index].add(docs[i]);
        }

        // save each list as a bow
        for (int n = 0; n < nPart; ++n) {
            f[n] = partitionFile(dir,n);
            saveAsBagOfWordsFile(lists[n], f[n]);
        }
        return f;
    }

    public File partitionFile(File dir,int n){
        return new File(dir, file.getName().replace(".bow", String.format("_part%d.bow", n)));
    }
    public void saveAsBagOfWordsFile(List docs, File file) throws Exception {
        PrintStream stream = new PrintStream(file);
        stream.printf("%d\n%d\n%d\n", docs.size(), V, total);
        for (int d = 0; d < docs.size(); ++d) {
            for (Entry e : asMap((int[]) docs.get(d)).entrySet()) {
                int w = (Integer) e.getKey() + 1;
                int count = (Integer) e.getValue();
                stream.printf("%d\t%d\t%d\n", d + 1, w, count);
            }
        }
        stream.close();
    }

    static TreeMap<Integer, Integer> asMap(int[] words) {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        for (int i = 0; i < words.length; ++i) {
            Integer count = map.get(words[i]);
            if (count == null) {
                map.put(words[i], 1);
            } else {
                map.put(words[i], count + 1);
            }
        }
        return map;
    }

    public int[][][] toThreadDocuments(int threads) throws Exception {
        int[][] allDocs = toDocumentFormat();
        int docsPerThread = (allDocs.length / threads) + 1;

        int[][][] ret = new int[threads][][];
        int start = 0;
        for (int t = 0; t < threads; ++t) {
            int end = Math.min(start + docsPerThread, allDocs.length);
            int len = end - start;
            int[][] threadDocs = new int[len][];
            for (int d = start; d < end; ++d) {
                threadDocs[d - start] = allDocs[d];
            }
            start = end;
            ret[t] = threadDocs;
        }
        allDocs = null;
        return ret;
    }

    public int[][] toDocumentFormat() throws Exception {
        System.out.printf("Reading BOW file: %s\n",this.file.getPath());
        List<Integer> list = new ArrayList<>();
        int currentDoc = 1;
        total = 0;
        BufferedReader reader = readHeader();
        int[][] ret = new int[N][];
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\t| ");
            int doc = Integer.valueOf(tokens[0]);
            if (doc != currentDoc) {
                if (doc != currentDoc + 1) {
                    System.out.printf("Error. No words for document %d\n", currentDoc + 1);
                    System.exit(0);
                }
                int i = currentDoc - 1;
                listToInt(list, i, ret);
                ++currentDoc;
            }
            for (int i = 0; i < Integer.valueOf(tokens[2]); ++i) {
                list.add(Integer.valueOf(tokens[1]) - 1);
                ++total;
            }
            line = reader.readLine();
        }
        listToInt(list, currentDoc - 1, ret);
        reader.close();
        return ret;
    }

    static public BagOfWords[] factory(File[] files)throws Exception{
        BagOfWords[] ret = new BagOfWords[files.length];
        for (int i=0 ; i<files.length ; ++i){
            ret[i] = new BagOfWords(files[i]);
        }
        return ret;
    }
    static public int[][] toDocumentFormat(BagOfWords[] bows) throws Exception {
        int[][] ret = new int[getDocumentCount(bows)][];
        int i=0;
        for (BagOfWords bow : bows){
            int[][] docs = bow.toDocumentFormat();
            for (int j=0 ; j<docs.length ; ++j){
                ret[i] = docs[j];
                ++i;
            }
        }
        return ret;
    }
    public int getTotalWords() {
        return total;
    }

    public int getVocabSize() {
        return V;
    }

    public File getFile(){
        return this.file;
    }
    static int getDocumentCount(BagOfWords[] bows)throws Exception{
        int r =0;
        for (BagOfWords bow : bows){
            r = r + bow.getDocumentCount();
        }
        return r;
    }
    public int getDocumentCount(){
        return N;
    }
    public BufferedReader readHeader() throws Exception {
        BufferedReader reader = openFile();
        N = Integer.valueOf(reader.readLine());
        V = Integer.valueOf(reader.readLine());
        W = Integer.valueOf(reader.readLine()); 
        return reader;
    }

    public void listToInt(List<Integer> list, int i, int[][] ret) {
        Integer[] v = list.toArray(new Integer[0]);
        int[] r = new int[v.length];
        for (int j = 0; j < v.length; ++j) {
            r[j] = v[j];
        }
        ret[i] = r;
        list.clear();
    }

    public BufferedReader openFile()throws Exception {
        BufferedReader reader = null;
        try {
            GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(file));
            reader = new BufferedReader(new InputStreamReader(gzipStream));
        } catch (ZipException exc) {
            reader = new BufferedReader(new FileReader(file));
        }
        return reader;
    }
    
    static public void main(String[] args) throws Exception {
        BagOfWords bow = new BagOfWords("/net/waterston/vol2/home/gevirl/Cao_cellCounts_10.bow");
        File[] files = bow.partition(5, new File("/net/waterston/vol2/home/gevirl"), new Random());
        int uiasdfuihsd = 0;
    }
}
