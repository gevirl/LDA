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
public class PeaksCorpus {
 static public void main(String[] args)throws Exception {
        int nDocs = 30000;
        int nDocTypes = 20000;
        double lambda = 225;
        int nTopics = 50;
        int nVocab = 15000;
        
        double alpha = .05;
        double beta = .13;
              
        double doublets = 0.15;
        
        String prefix = "/net/waterston/vol2/home/gevirl/peaksHighTypes";
        Corpus corpus = new Corpus(nDocs,nDocTypes,lambda,nTopics,nVocab,alpha,beta);
        
        PrintStream stream = new PrintStream(prefix+".bow");
        corpus.generateBOW(stream,doublets,BinaryDocument.class);
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
