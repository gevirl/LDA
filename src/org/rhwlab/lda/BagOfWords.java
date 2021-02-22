/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;
import org.jdom2.Element;

/**
 *
 * @author gevirl
 */
abstract public class BagOfWords {

    File file;
    boolean binarize;

    int docColumn = -1;
    int wordColumn = -1;
    int countColumn = -1;

    int N;
    int V;
    int W;  // number of line in file (less the header)
    int total;  // total number of words in the data

    TreeMap<Integer, Integer> wordMap;  // original word ID -> new word ID , 1-based indexing

    public BagOfWords(String fileStr, boolean binarize) throws Exception {
        this(new File(fileStr), binarize);
    }

    public BagOfWords(File file, boolean binarize) throws Exception {
        this.file = file;
        this.binarize = binarize;
        BufferedReader reader = readHeader();
        reader.close();
    }

    public void setBinarize(boolean bin) {
        this.binarize = bin;
    }

    abstract public BufferedReader readHeader() throws Exception;

    static public boolean checkClass(String cl) {
        return cl.contains("MatrixMarket") || cl.contains("OriginalBOW");

    }

    public static BagOfWords factory(Element ele) throws Exception {
        String cl = ele.getAttributeValue("class");
        if (cl.contains("MatrixMarket")) {
            return new MatrixMarket(ele);
        }
        if (cl.contains("OriginalBOW")) {
            return new OriginalBOW(ele);
        }
        return null;
    }

    public static BagOfWords factory(File file, String cl, boolean bin) throws Exception {
        if (cl.contains("MatrixMarket")) {
            return new MatrixMarket(file, bin);
        }
        if (cl.contains("OriginalBOW")) {
            return new OriginalBOW(file, bin);
        }
        return null;
    }

    public Element toXML() {
        Element ret = new Element("BagOfWords");
        ret.setAttribute("class", this.getClass().getName());
        ret.setAttribute("file", file.getPath());
        if (binarize) {
            ret.setAttribute("binarize", "true");
        } else {
            ret.setAttribute("binarize", "false");
        }
        return ret;
    }

    static public boolean isBinarized(Element ele) {
        return ele.getAttributeValue("binarize").equals("true");
    }

    // partition this bow randomly
    public File[] partition(int nPart, File dir, Random rnd) throws Exception {
        File[] f = new File[nPart];
        int[] pCounts = new int[nPart];
        ArrayList<int[]>[] lists = new ArrayList[nPart];
        for (int i = 0; i < nPart; ++i) {
            lists[i] = new ArrayList<>();
        }
        // split up the docs
        int[][] docs = toDocumentFormat();
        for (int i = 0; i < docs.length; ++i) {
            int index = rnd.nextInt(nPart);
            lists[index].add(docs[i]);
            pCounts[index] = pCounts[index] + countItems(docs[i]);
        }

        // save each list as a bow
        for (int n = 0; n < nPart; ++n) {
            f[n] = partitionFile(dir, n);
            if (this instanceof MatrixMarket){
                MatrixMarket.saveAsBagOfWordsFile(lists[n], V, pCounts[n], f[n]);
            }else if (this instanceof OriginalBOW){
                OriginalBOW.saveAsBagOfWordsFile(lists[n], V, pCounts[n], f[n]);
            }
        }
        return f;
    }

    private int countItems(int[] docRec) {
        int ret = 0;
        for (Map.Entry e : asMap(docRec).entrySet()) {
            ret = ret + (Integer) e.getValue();
        }
        return ret;
    }

    public File partitionFile(File dir, int n) {
        return partitionFile(dir, file.getName(), n);
    }

    static public File partitionFile(File dir, String name, int n) {
        if (name.contains(".bow")){
            return new File(dir, name.replace(".bow", String.format("_part%d.bow", n)));
        }
        if (name.contains(".gz")){
            return new File(dir, name.replace(".gz", String.format("_part%d.gz", n)));
        }
        return new File(String.format("%s_part%d",name,n));
    }

    static public void saveAsBagOfWordsFile(List docs, int nVocab, int totalLines, File file) throws Exception {
        PrintStream stream
                = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));
        stream.printf("%d\n%d\n%d\n", docs.size(), nVocab, totalLines);
        for (int d = 0; d < docs.size(); ++d) {
            for (Entry e : asMap((int[]) docs.get(d)).entrySet()) {
                int w = (Integer) e.getKey() + 1;
                int count = (Integer) e.getValue();
                stream.printf("%d\t%d\t%d\n", d + 1, w, count);
            }
        }
        stream.close();
    }

    public void saveAsBagOfWordsFile(List docs, File file) throws Exception {
        PrintStream stream
                = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));
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

    static public void intoMap(int[] words, TreeMap<Integer, Integer> map) {
        for (int i = 0; i < words.length; ++i) {
            Integer count = map.get(words[i]);
            if (count == null) {
                map.put(words[i], 1);
            } else {
                map.put(words[i], count + 1);
            }
        }
    }

    static TreeMap<Integer, Integer> asMap(int[] words) {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        intoMap(words, map);
        return map;
    }

    static public TreeMap<Integer, Integer> mergeDocs(int[] words1, int[] words2) {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        intoMap(words1, map);
        intoMap(words2, map);
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

    public List<int[]> subsample(double pct) throws Exception {
        Random rnd = new Random();
        BufferedReader reader = readHeader();
        List<int[]> ret = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",|\t| ");
            int[] remap = new int[3];
            remap[docColumn] = Integer.valueOf(tokens[docColumn]);
            remap[countColumn] = Integer.valueOf(tokens[countColumn]);
            remap[wordColumn] = Integer.valueOf(tokens[wordColumn]);
            int n = remap[countColumn];
            for (int i = 0; i < n; ++i) {
                double r = rnd.nextDouble();
                if (r <= pct) {
                    --remap[countColumn];
                }
            }
            if (remap[countColumn] > 0) {
                ret.add(remap);
            }
            line = reader.readLine();
        }
        reader.close();
        W = ret.size();
        return ret;
    }

    public List<int[]> remapWords(Set<Integer> toRemove) throws Exception {
        System.out.printf("Reading document file: %s\n", this.file.getPath());
        int currentWord = 1;  // R 1-based indexing
        wordMap = new TreeMap<>();
        BufferedReader reader = readHeader();
        List<int[]> ret = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",|\t| ");
            int[] remap = new int[3];

            remap[docColumn] = Integer.valueOf(tokens[docColumn]);
            remap[countColumn] = Integer.valueOf(tokens[countColumn]);

            int w = Integer.valueOf(tokens[wordColumn]);

            if (!toRemove.contains(w)) {
                Integer wi = wordMap.get(w);
                if (wi == null) {
                    wordMap.put(w, currentWord);
                    wi = currentWord;
                    ++currentWord;
                }
                remap[wordColumn] = wi;
                ret.add(remap);
            }
            line = reader.readLine();
        }
        reader.close();
        V = wordMap.size();
        W = ret.size();
        return ret;
    }

    public int[][] toDocumentFormat() throws Exception {
        System.out.printf("Reading document file: %s\n", this.file.getPath());
        if (binarize) {
            System.out.println("Binarizing");
        } else {
            System.out.println("Using all UMIs");
        }
        List<Integer> list = new ArrayList<>();
        int currentDoc = 1;
        total = 0;
        BufferedReader reader = readHeader();
        int[][] ret = new int[N][];
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",|\t| ");
            int doc = Integer.valueOf(tokens[docColumn]);  // doc is 1-based
            if (doc != currentDoc) {
                if (doc != currentDoc + 1) {
                    System.out.printf("Error. No words for document %d\n", currentDoc + 1);
                    System.exit(0);
                }
                int i = currentDoc - 1;
                listToInt(list, i, ret);
                ++currentDoc;
                if (currentDoc % 10000 == 0) {
                    System.out.printf("Document: %d\n", currentDoc);
                }
            }
            int n = 1;
            if (!binarize) {
                n = Integer.valueOf(tokens[countColumn]);
            }
            for (int i = 0; i < n; ++i) {
                list.add(Integer.valueOf(tokens[wordColumn]) - 1);
                ++total;
            }
            line = reader.readLine();
        }
        listToInt(list, currentDoc - 1, ret);
        reader.close();
        System.out.println("Finished reading input data");
        return ret;
    }

    static public int[][] toDocumentFormat(BagOfWords[] bows) throws Exception {
        int[][] ret = new int[getDocumentCount(bows)][];
        int i = 0;
        for (BagOfWords bow : bows) {
            int[][] docs = bow.toDocumentFormat();
            for (int j = 0; j < docs.length; ++j) {
                ret[i] = docs[j];
                ++i;
            }
        }
        return ret;
    }

    public TreeMap<Integer, Integer> getWordMap() {
        return wordMap;
    }

    public int[] reverseWordMap() {
        int[] ret = new int[wordMap.size()];
        for (Entry e : wordMap.entrySet()) {
            int orig = (Integer) e.getKey() - 1;
            int remapID = (Integer) e.getValue() - 1;
            ret[remapID] = orig;
        }
        return ret;
    }

    public int getTotalWords() {
        return total;
    }

    public int getVocabSize() {
        return V;
    }

    public File getFile() {
        return this.file;
    }

    static int getDocumentCount(BagOfWords[] bows) throws Exception {
        int r = 0;
        for (BagOfWords bow : bows) {
            r = r + bow.getDocumentCount();
        }
        return r;
    }

    public int getDocumentCount() {
        return N;
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

    public BufferedReader openFile() throws Exception {
        return BagOfWords.openFile(file);
    }

    static public BufferedReader openFile(File f) throws Exception {
        BufferedReader reader = null;
        try {
            GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(f));
            reader = new BufferedReader(new InputStreamReader(gzipStream));
        } catch (ZipException exc) {
            reader = new BufferedReader(new FileReader(f));
        }
        return reader;
    }

    public void mergeDocuments(File clusterFile, int n) throws Exception {
        String prefix = String.format("%s_merge_%d", clusterFile.getName().replace(".csv", ""), n);
        PrintStream outBOW
                = new PrintStream(new GZIPOutputStream(new FileOutputStream(new File(file.getParent(), String.format("%s_%s", prefix, file.getName())))));
        PrintStream outDocs
                = new PrintStream(new GZIPOutputStream(new FileOutputStream(new File(file.getParent(), String.format("%s_docs_%s", prefix, file.getName())))));

        Random rnd = new Random();
        ArrayList<TreeMap<Integer, Integer>> docList = new ArrayList<>();
        int nLines = 0;
        int[][] docs = this.toDocumentFormat();

        BufferedReader reader = new BufferedReader(new FileReader(clusterFile));
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(tokens));
            if (list.size() > 1 || docList.size() > 122000) {
                System.out.printf("Size: %d\n", tokens.length);
            }
            while (!list.isEmpty()) {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                List<String> toMerge = new ArrayList<>();

                if (list.size() <= n) {
                    toMerge.addAll(list);
                    list.clear();
                } else if (list.size() < 2 * n) {
                    for (int i = 0; i < list.size() / 2; ++i) {
                        int index = rnd.nextInt(list.size());
                        toMerge.add(list.remove(index));
                    }
                } else {
                    for (int i = 0; i < n; ++i) {
                        int index = rnd.nextInt(list.size());
                        toMerge.add(list.remove(index));
                    }
                }
                for (int i = 0; i < toMerge.size(); ++i) {
                    intoMap(docs[-1 + Integer.valueOf(toMerge.get(i))], map);
                }

                docList.add(map);
                nLines = nLines + map.size();
                if (docList.size() % 1000 == 0 || docList.size() > 122000) {
                    System.out.printf("Out docs: %d\n", docList.size());
                }
                outDocs.print(toMerge.get(0));
                for (int i = 1; i < toMerge.size(); ++i) {
                    outDocs.printf("_%s", toMerge.get(i));
                }
                outDocs.println();
            }
            line = reader.readLine();
        }
        System.out.println("Finished merging");
        outDocs.close();
        reader.close();

        saveTo(docList, nLines, outBOW);
        outBOW.close();

    }

    public abstract void saveTo(ArrayList<TreeMap<Integer, Integer>> docList, int nLines, PrintStream stream);

    static public void main(String[] args) throws Exception {
        File mmFile = new File("/net/waterston/vol9/ldaWorm/elegans.all.csv.gz");
        File subSampled = new File(mmFile.getParent(), "sub0.25_" + mmFile.getName());
        MatrixMarket mm = new MatrixMarket(mmFile, false);
        List<int[]> list = mm.subsample(0.25);

        PrintStream mmOutStream = new PrintStream(new GZIPOutputStream(new FileOutputStream(subSampled)));
        mm.saveTo(list, mmOutStream);
        mmOutStream.close();

    }
}
