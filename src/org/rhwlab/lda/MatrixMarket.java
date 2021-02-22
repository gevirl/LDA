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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;
import org.jdom2.Element;
import static org.rhwlab.lda.BagOfWords.asMap;

/**
 *
 * @author gevirl
 */
public class MatrixMarket extends BagOfWords {

    public MatrixMarket(Element ele)throws Exception {
        this(ele.getAttributeValue("file"),BagOfWords.isBinarized(ele));
    }
    public MatrixMarket(String s,boolean binarize)throws Exception {
        this(new File(s),binarize);
    }
    public MatrixMarket(File file,boolean binarize) throws Exception {
        super(file,binarize);
        docColumn = 1;
        wordColumn = 0;
        countColumn = 2;
    }

    @Override
    public BufferedReader readHeader() throws Exception {
        BufferedReader reader = openFile();

        // skip comments
        String line = reader.readLine();
        while (line.startsWith("%") || line.length()==0){
            line = reader.readLine();
        }
        String[] tokens = line.split(",|\t| ");
        this.N = Integer.parseInt(tokens[1]);
        this.V = Integer.parseInt(tokens[0]);
        this.W = Integer.parseInt(tokens[2]);
        
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
        stream.printf("%d,%d,%d\n", V, docs.length, total);
        for (int d = 0; d < docs.length; ++d) {
            for (Map.Entry e : asMap(docs[d]).entrySet()) {
                int w = (Integer) e.getKey() + 1;
                int count = (Integer) e.getValue();
                stream.printf("%d,%d,%d\n", w, d + 1,  count);
            }
        }
        stream.close();
    }
    @Override
    public void saveTo(ArrayList<TreeMap<Integer, Integer>> docList, int nLines, PrintStream stream) {
        stream.printf("%d,%d,%d\n", V,docList.size(),nLines);
        for (int i=0 ; i<docList.size() ; ++i){
            TreeMap<Integer,Integer> map = docList.get(i);
            for (Integer vocab : map.keySet()){
                stream.printf("%d,%d,%d\n", 1+vocab , i+1 , map.get(vocab));
            }
        }
    }    
    public void saveTo(List<int[]> recs,PrintStream stream){
        stream.printf("%d %d %d\n", V,N,W);
        for (int[] rec : recs){
            stream.printf("%d %d %d\n",rec[1],rec[0],rec[2]);
        }
    }
    
    static public void saveTo(List<int[]> recs,int V,int W,PrintStream stream){
        stream.printf("%d %d %d\n", V,recs.size(),W);
        for (int[] rec : recs){
            stream.printf("%d %d %d\n",rec[1],rec[0],rec[2]);
        }
    }    
    static public void main(String[] args) throws Exception {
        File mmFile = new File("/net/waterston/vol9/flyRNA/matrix.mtx.gz");
        File mmOutFile = new File(mmFile.getParent(),"broad_"+mmFile.getName());
        File vocabFile = new File("/net/waterston/vol9/flyRNA/features.tsv.gz");
        File vocabOutFile = new File(vocabFile.getParent(),"broad_"+vocabFile.getName());
        
        File removeGenesFile = new File("/net/waterston/vol9/flyRNA/BroadGenes");
        
        
        MatrixMarket mm = new MatrixMarket(mmFile,false);
        VocabFile vocab = new VocabFile(vocabFile,0);

        
        Set<Integer> geneSet = vocab.getIndex(removeGenesFile);
 //       Set<Integer> geneSet = new TreeSet<>();

        List<int[]> recs = mm.remapWords(geneSet);
        int[] remap = mm.reverseWordMap();
        String[] genes = vocab.reorder(remap);
        
        
       
        PrintStream mmOutStream = new PrintStream(new GZIPOutputStream(new FileOutputStream(mmOutFile)));
        PrintStream vocabOutStream = new PrintStream(new GZIPOutputStream(new FileOutputStream(vocabOutFile)));    
        mm.saveTo(recs, mmOutStream);
        mmOutStream.close();
        for (String gene : genes){
            vocabOutStream.println(gene);
        }
        vocabOutStream.close();

    }

}
