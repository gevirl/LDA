/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import static org.rhwlab.lda.generative.Corpus.reportArray;

/**
 *
 * @author gevirl
 */
public class RNASeqCorpus {
    static public void main(String[] args)throws Exception {
        int nDocs = 20000;
        int nDocTypes = 250;
        double lambda = 500;
        int nTopics = 50;
        int nVocab = 20000;
        double alpha = .01;
        double beta = .1;
        
        Class cl = CountsDocument.class;
        Constructor con = cl.getConstructor(int.class,int.class);
        Document dc = (Document)con.newInstance(100,50);
        Constructor[] ct = cl.getConstructors();
        
        double doublets = 0.15;
        String prefix = "/net/waterston/vol2/home/gevirl/ldatest/RNASeqDoubletAlphaLow";
        Corpus corpus = new Corpus(nDocs,nDocTypes,lambda,nTopics,nVocab,alpha,beta);
        
        PrintStream stream = new PrintStream(prefix+".bow");
        corpus.generateBOW(stream,doublets,CountsDocument.class);
        stream.close();
        
        // save theta and phi
        stream = new PrintStream(prefix+".theta");
        reportArray(stream,corpus.getDocTheta());
        stream.close();
        
        stream = new PrintStream(prefix+".types");
        reportArray(stream,corpus.theta);
        stream.close();
        
        stream = new PrintStream(prefix+".phi");
        reportArray(stream,corpus.getPhi());
        stream.close();  

        
        // report the document identities
        stream = new PrintStream(prefix+".docs");
        for (Document doc : corpus.docList){
            stream.println(doc.identity());
        }
        stream.close();
        int ashuidfhsd=0;
    }    
}
