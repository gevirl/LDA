/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.File;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author gevirl
 */
public class MultiThreadXMLBase {

    int V;  // vocab size
    int K;  // number of topics
    int D;  // number of dcouments
    RowSumMatrix alpha;
    RowSumMatrix beta;
    
    int cacheSize = 10;  // number of output iterations to cache before writing them out
    int iterationOffset = 0;
    int iterations = 1000;
    
    long totalWords = 0;
    int nWorkers;
    File[] workerXMLFiles;
    
    public MultiThreadXMLBase(int nWorkers){
        this.nWorkers = nWorkers;
        workerXMLFiles = new File[nWorkers];
    }
    
    public MultiThreadXMLBase(File dir) throws Exception {

        File xml = new File(dir, "MultiThreadLDA.xml");
        System.out.printf("Reading xml file %s\n", xml.getPath());
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(xml);
        Element root = doc.getRootElement();
        cacheSize = Integer.valueOf(root.getAttributeValue("cacheSize"));
        V = Integer.valueOf(root.getAttributeValue("V"));
        K = Integer.valueOf(root.getAttributeValue("K"));
        D = Integer.valueOf(root.getAttributeValue("D"));
        nWorkers = Integer.valueOf(root.getAttributeValue("nWorkers"));
        totalWords = Long.valueOf(root.getAttributeValue("totalWords"));
        iterationOffset = Integer.valueOf(root.getAttributeValue("lastIteration"));
        
        List<Element> workerEles = root.getChildren("WorkerLDA");
        workerXMLFiles = new File[nWorkers];
        for (Element workerEle : workerEles){
            int index = Integer.valueOf(workerEle.getAttributeValue("index"));
            workerXMLFiles[index] = new File(dir,workerEle.getAttributeValue("file"));
        }
        
        alpha = new RowSumMatrix(root.getChild("Alpha"));
        beta = new RowSumMatrix(root.getChild("Beta"));

        System.out.printf("Closing xml file %s\n", xml.getPath());
    } 
    
    public Element toXML(){
        Element ele = new Element("MultiThreadLDA");
        ele.setAttribute("V", Integer.toString(V));
        ele.setAttribute("K", Integer.toString(K));
        ele.setAttribute("D", Integer.toString(D));
        ele.setAttribute("nWorkers", Integer.toString(nWorkers));
        ele.setAttribute("totalWords", Long.toString(totalWords));
        ele.setAttribute("lastIteration", Integer.toString(iterations + iterationOffset));
        ele.setAttribute("cacheSize", Integer.toString(this.cacheSize));
        for (int i = 0; i < nWorkers; ++i) {
            Element workerEle = new Element("WorkerLDA");
            workerEle.setAttribute("index", Integer.toString(i));
            workerEle.setAttribute("file", workerXMLFiles[i].getName());
            ele.addContent(workerEle);
        } 
        ele.addContent(alpha.toXML("Alpha"));
        ele.addContent(beta.toXML("Beta"));        
        return ele;
    }

    public RowSumMatrix getAlpha() {
        return alpha;
    }

    public RowSumMatrix getBeta() {
        return beta;
    }

    public int getWorkersSize() {
        return nWorkers;
    }

    public int getDocumentsSize() {
        return D;
    }

    public int getVocabSize() {
        return V;
    }

    public int getTopicsSize() {
        return K;
    }

    public long getTotalWords() {
        return totalWords;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public int getLastIteration() {
        return iterations + iterationOffset;
    }    
}
