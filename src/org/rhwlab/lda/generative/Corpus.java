/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import umontreal.iro.lecuyer.randvar.PoissonGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGenInt;
import umontreal.iro.lecuyer.randvarmulti.DirichletGen;
import umontreal.iro.lecuyer.rng.F2NL607;
import umontreal.iro.lecuyer.rng.MRG32k3a;

/**
 *
 * @author gevirl
 */
// generate a random corpus of documents
// alpha - dirichlet parameter for the theta (the document topic distributions)
// beta - dirchlet parameter for the phi (topic word distributions)
public class Corpus {
    int nDocs;
    int nVocab;
    double[][] theta;
    double[][] phi;
    int total;
    RandomVariateGenInt docSizeDist;
    List<Document> docList = new ArrayList<>();
    
    public Corpus(String prefix)throws Exception {
        File docsFile = new File(prefix+".docs");
        BufferedReader reader = new BufferedReader(new FileReader(docsFile));
        String line = reader.readLine();
        while (line != null){
            String[] tokens = line.split(",|\t| ");
            List<Document> docs = new ArrayList<>();
            for (int i=0 ; i<tokens.length ; i=i +2){
                int count = Integer.valueOf(tokens[i]);
                int type = Integer.valueOf(tokens[i+1]);
                docs.add(new CountsDocument(count,type));
            }
            switch (docs.size()) {
                case 1:
                    docList.add(new CountsDocument(docs.get(0).getWordCount(),docs.get(0).getType().get(0)));
                    break;
                case 2:
                    docList.add(new DoubletDocument(docs.get(0),docs.get(1)));
                    break;
                default:
                    docList.add(new MultiDocument(docs));
                    break;
            }
            line = reader.readLine();
        }
    }
    public Corpus(int nDocs,int nDocTypes,double lambda,int nTopics,int nVocab,double alpha,double beta){
        this.nDocs = nDocs;
        this.nVocab = nVocab;
        docSizeDist = new PoissonGen(new F2NL607(),lambda);
        
        double[] a = new double[nTopics];
        for (int i=0 ; i<a.length ; ++i){
            a[i] = alpha;
        }
        DirichletGen thetaGen = new DirichletGen(new MRG32k3a(),a); // dirichlet for generating the doc/topic distributions       
        // generate the document-topic distributions
        theta = new double[nDocTypes][nTopics];
        for (int d=0 ; d<theta.length ; ++d){
            thetaGen.nextPoint(theta[d]);
        }
        
        double[] b = new double[nVocab];
        for (int i=0 ; i<b.length ; ++i){
            b[i] = beta;
        }
        DirichletGen phiGen = new DirichletGen(new MRG32k3a(),b);   // dirichlet for generating the topic/word distibutions   
        // generate the topic/word distributions
        phi = new double[nTopics][nVocab];
        for (int k=0 ; k<nTopics ; ++k){
            phiGen.nextPoint(phi[k]);
            double[] sorted = Arrays.copyOf(phi[k], nVocab);
            Arrays.sort(sorted);
            double[] cumm = new double[sorted.length];
            cumm[0] = sorted[sorted.length-1];
            for (int i = 1 ; i<cumm.length ; ++i){
                cumm[i] = cumm[i-1] + sorted[sorted.length -i -1 ];
            }
            int tuiashfs=0;
        }

    }
    public void generateBOW(PrintStream stream,double doublets,Class docClass)throws Exception {
        Random rnd = new Random();
        Constructor con = docClass.getConstructor(int.class,int.class);
        
        stream.println(nDocs);
        stream.println(nVocab);
        stream.println(total);
        // generate the documents
        for (int d=0 ; d<nDocs ; ++d){
            // choose a document type;
 //           Document doc = new UniDocument(docSizeDist.nextInt(),rnd.nextInt(theta.length));
            Document doc = (Document)con.newInstance(docSizeDist.nextInt(),rnd.nextInt(theta.length));
            // should this be a doublet ?
            if (rnd.nextDouble()<doublets){
                doc = new DoubletDocument(doc,new CountsDocument(docSizeDist.nextInt(),rnd.nextInt(theta.length)));
            }
            
            docList.add(doc);
            
            TreeMap<Integer,Integer> map = doc.getWordMap(theta, phi);
            for (Integer vocab : map.keySet()){
                int count = map.get(vocab);
                stream.printf("%d %d %d\n",d+1,vocab+1,count);
            }
        }        
    }
    public double[][] getDocTheta(){
        double[][] ret = new double[nDocs][];
        int d=0;
        for (Document doc : docList){
            ret[d] = doc.getTopicDist(theta);
            ++d;
        }
        return ret;
    }
    public double[][] getPhi(){
        return this.phi;
    }
    
    public List<Document> getDocuments(){
        return this.docList;
    }
    public TreeMap<String,List<Integer>> docsByType(){
        TreeMap<String,List<Integer>> ret = new TreeMap<>();
        int d = 0;
        for (Document doc : docList){
            String name = doc.getClass().getName();
            if (name.contains("Doublet")){
                name = "Doublet";
            } else {
                name = "Singlet";
            }
            List<Integer> list = ret.get(name);
            if (list == null){
                list = new ArrayList<>();
                ret.put(name,list);
            }
            list.add(d);
            ++d;
        }
        return ret;
    }

    static public void reportArray(PrintStream stream , double[][] v){
        for (int r=0 ; r<v.length ; ++r){
            stream.print(v[r][0]);
            for (int c=1 ; c<v[r].length ; ++c){
                stream.print(",");
                stream.print(v[r][c]);
            }
            stream.println();
        }
    }
    static public void main(String[] args) throws Exception {
        String prefix = "/net/waterston/vol2/home/gevirl/ldatest/RNASeqDoublet";
        Corpus corp = new Corpus(prefix);
        int asdfuishd=0;
    }

}
