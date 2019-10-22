/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author gevirl
 */
public class ZDirectory {

    File dir;
    MultiThreadXML lda;

    TreeMap<Integer, TreeMap<Integer, WorkerZIterationFile>> iterWorkMap = new TreeMap<>(); // iter,worker -> file
    TreeMap<Integer, TreeMap<Integer, WorkerZIterationFile>> workIterMap = new TreeMap<>();  // worker,iter -> file
    TreeMap<Integer,Integer> countMap = new TreeMap<>(); // iter -> record count
    
    int nTopics;
    int nWorkers;
    int nDocs;
    int parallelism;

    public ZDirectory(File dir, MultiThreadXML lda, int parallelism) throws Exception {
        this.dir = dir;
        this.lda = lda;
        this.parallelism = parallelism;

        nTopics = lda.getTopicsSize();
        WorkerXML[] workers = lda.getWorkers();
        nWorkers = workers.length;
        nDocs = lda.getDocumentsSize();

        // make a set of iterations 
        TreeSet<Integer> iterSet = new TreeSet<>();
        for (File file : dir.listFiles()) {
            String name = file.getName();
            if (name.startsWith("Iter")) {
                int iter = Integer.valueOf(name.substring(4, 10));
                iterSet.add(iter);
            }
        }

        // organize the z iteration files

        for (File file : dir.listFiles()) {
            String name = file.getName();
            if (name.startsWith("Iter")) {
                int iter = Integer.valueOf(name.substring(4, 10));
                int w = Integer.valueOf(name.substring(16, name.indexOf(".Z")));
                int recCount  = workers[w].getRecordCount(file);
                countMap.put(iter, recCount);
                WorkerZIterationFile zFile = new WorkerZIterationFile(file);

                TreeMap<Integer, WorkerZIterationFile> workMap = iterWorkMap.get(iter);
                if (workMap == null) {
                    workMap = new TreeMap<>();
                    iterWorkMap.put(iter, workMap);
                }
                workMap.put(w, zFile);

                TreeMap<Integer, WorkerZIterationFile> iterMap = workIterMap.get(w);
                if (iterMap == null) {
                    iterMap = new TreeMap<>();
                    workIterMap.put(w, iterMap);
                }
                iterMap.put(iter, zFile);
            }
        }
    }

    // put all the iterations for all the workers into the distribution
    public void addTo(PointEstimateDistribution dist,int skip) throws Exception {
        System.out.println("Starting addTo dist");
        int toSkip = skip;
        Collection<Callable<PointEstimateDistribution>> threadCollection = new ArrayList<>();
        
        for (Integer iter : iterWorkMap.keySet()) {
            TreeMap<Integer, WorkerZIterationFile> workMap = iterWorkMap.get(iter);
            int recCount = countMap.get(iter);
            if (recCount <= toSkip){
                // skip the entire set of files
                toSkip = toSkip - recCount;
                
            }else {
                ZIterationFiles zFiles = new ZIterationFiles(workMap.values(),toSkip,lda.getDocumentsSize());
                int[][][] z = zFiles.readFiles();
                dist.add(z);

            }
        }

        System.out.println("Ending addTo");
    }
    

    public void iterationsReport(PrintStream likeStream, File outDir) throws Exception {
        System.out.println("Starting Iterations Report");
        int[][] docs = lda.getDocuments();

        int likeIter = 0;
        for (Integer iter : iterWorkMap.keySet()) {
            System.out.printf("Starting report for iteration : %d\n",iter);
            TreeMap<Integer, WorkerZIterationFile> workMap = iterWorkMap.get(iter);
            ZIterationFiles iterFiles = new ZIterationFiles(workMap.values(), 0,docs.length);
            int[][][] z = iterFiles.readFiles();
            
            int[][][] nw = new int[z.length][][];
            for (int i=0 ; i<z.length ; ++i){
                nw[i] = WordTopicCounts.wordTopicCounts(z[i],lda.getVocabSize(), lda.getTopicsSize(), docs);
            }
            int[][][] nd = new int[z.length][][];
            for (int i=0 ; i<z.length ; ++i){
                nd[i] = DocumentTopicCounts.documentTopicCounts(z[i],lda.getTopicsSize());
            }
            
            System.out.printf("Counts completed for iteration %d\n", iter);
            if (likeStream != null) {
                Collection<Callable<Double>> threadCollection = new ArrayList<>();
                for (int i = 0; i < nw.length; ++i) {
                    ++likeIter;
                    threadCollection.add(new Likelihood(docs, nw[i], nd[i], lda.getAlpha(), lda.getBeta(), likeIter));
                }
                System.out.printf("Likelihood Service started\n");
                ExecutorService service = Executors.newWorkStealingPool(this.parallelism);
                List<Future<Double>> futures = service.invokeAll(threadCollection);
                System.out.printf("Likelihood service complete\n");
                for (Future<Double> future : futures) {
                    likeStream.println(future.get());
                }
            }
            if (outDir != null) {
                toDocTextCounts(nd, outDir,parallelism);
                toWordTextCounts(nw, outDir,parallelism);
            }
        }
    } 
    public void toDocTextCounts(int[][][] nd, File outDir,int parallelism) throws Exception {
        Collection<Callable<Object>> threadCollection = new ArrayList<>();
        ExecutorService service = Executors.newWorkStealingPool(parallelism);

        PrintStream[] stream = new PrintStream[nTopics];
        for (int t = 0; t < nTopics; ++t) {
            String label = String.format("Topic%03dDoc.csv", t);
            stream[t] = new PrintStream(new FileOutputStream(new File(outDir, label), true));
            threadCollection.add(new TextCounts(stream[t], nd, t,label));
        }
        service.invokeAll(threadCollection);
        for (int t = 0; t < nTopics; ++t) {
            stream[t].close();
        }
    }

    public void toWordTextCounts(int[][][] nw, File outDir,int parallelism) throws Exception {
        Collection<Callable<Object>> threadCollection = new ArrayList<>();
        ExecutorService service = Executors.newWorkStealingPool(parallelism);

        PrintStream[] stream = new PrintStream[nTopics];
        for (int t = 0; t < nTopics; ++t) {
            String label = String.format("Topic%03dWord.csv", t);
            stream[t] = new PrintStream(new FileOutputStream(new File(outDir, label), true));
            threadCollection.add(new TextCounts(stream[t], nw, t,label));
        }

        service.invokeAll(threadCollection);
        for (int t = 0; t < nTopics; ++t) {
            stream[t].close();
        }
    }    

    public void setSkip(int skip) {
        int cs = lda.getCacheSize();
        int toSkip = skip;

        for (Integer iter : iterWorkMap.keySet()) {
            TreeMap<Integer, WorkerZIterationFile> workMap = iterWorkMap.get(iter);
            if (cs < toSkip) {
                for (WorkerZIterationFile file : workMap.values()) {
                    file.resetSkip(cs);  // skipping all the iterations in this file
                }
                toSkip = toSkip - cs;
            } else {
                for (WorkerZIterationFile file : workMap.values()) {
                    file.resetSkip(toSkip);  // skipping some of the iterations in this file
                }
                break;
            }
        }

    }

    public MultiThreadXML getLDA() {
        return this.lda;
    }

    public int lastIteration() {
        return this.iterWorkMap.lastKey();
    }

    static public void main(String[] args) throws Exception {
        File f = new File("/net/waterston/vol2/home/gevirl/ldatest/zcacheout/topics_50/test_topics50_alpha0.010_beta0.010");
        MultiThreadXML lda = new MultiThreadXML(f);
        ZDirectory zdir = new ZDirectory(f, lda, 4);
 //       ZHistogram mode = zdir.zHistogram();
        //       mode.report(new File(f, "zmode"), lda.getDocuments());
        int uixduif = 0;
    }
}
