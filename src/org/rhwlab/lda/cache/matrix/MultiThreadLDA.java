/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.rhwlab.lda.cache.DocumentTopicCounts;
import org.rhwlab.lda.cache.WordTopicCounts;

/**
 *
 * @author gevirl
 */
public class MultiThreadLDA extends MultiThreadXMLBase implements Callable {

    
    int[][] nw;  //  V x K ,vocab by topic
    int[] nwsum;  // K , sum over vocab

    Collection<Callable<Object>> workers = new ArrayList<>();
    WorkerLDA[] workersArray;

    File outputDir = null;  // report counts for each iteration and saved state to this directory
    int thinning = 1;   // number of iterations between each save 
    int burnIn;

//    List<int[][]> nwCache = new ArrayList<>();
//    List<int[][]>[] ndCaches;

    PointEstimateDistribution peDist;
    String statistic;
    
    boolean maxLike=false;  //  if true, the iteration with the maximum likelihood is found during iterations
    double logLike = Double.NEGATIVE_INFINITY;
    int[][] maxLikeZ = null;
    
    public MultiThreadLDA(int[][] documents, int V, int K, RowSumMatrix alpha, RowSumMatrix beta, int nThreads, int cSize,String dist,String stat,double prec) {
        this(documents, V, K, alpha, beta, nThreads, cSize, 900,dist,stat,prec);
    }

    public MultiThreadLDA(int[][] documents, int V, int K, RowSumMatrix alpha, RowSumMatrix beta, int nThreads, int cSize, long seed, String dist,String stat,double prec) {
        super(nThreads);
        this.docs = documents;
        this.D = documents.length;
        this.V = V;
        this.K = K;
        this.alpha = alpha;
        this.beta = beta;
        this.cacheSize = cSize;
        this.statistic = stat;

/*
        ndCaches = new List[nThreads];
        for (int i = 0; i < nThreads; ++i) {
            ndCaches[i] = new ArrayList<>();
        }
*/
        // build all the workers
        int nDocs = documents.length / nThreads + 1;  // number of documents per thread
        int start = 0;
        for (int t = 0; t < nThreads; ++t) {
            int n = Math.min(nDocs, documents.length - start);
            int[][] workerDocs = new int[n][];
            for (int i = 0; i < workerDocs.length; ++i) {
                workerDocs[i] = documents[i + start];
            }
            int[][] nwW = new int[V][K];  // each worker will have a local place for storing the global word counts
            int[] nwsumW = new int[K];
            long nextSeed = seed + 10 * t;
            WorkerLDA worker = new WorkerLDA(String.format("Worker%d", t), workerDocs, V, K, alpha, beta, nextSeed);
            worker.setCacheSize(cSize);
            worker.setGlobalWordCounts(nwsumW);
            worker.setGlobalWordTopicCounts(nwW);
            workers.add(worker);
            start = start + n;
        }
        workersArray = workers.toArray(new WorkerLDA[0]);

        nw = new int[V][K];
        nwsum = new int[K];

        accumCounts();
        for (int d = 0; d < documents.length; ++d) {
            totalWords = totalWords + documents[d].length;
        }
        if (cSize == 0) {
            if (dist.equalsIgnoreCase("topic")){
                peDist = new TopicHistogramEstimator(documents, V, K);
            } else if (dist.equalsIgnoreCase("kde")){
                peDist = new MarginalKDE(documents, V, K,prec);
            }
        }

    }

    // restart the LDA from a state saved in directory
    public MultiThreadLDA(File dir) throws Exception {
        super(dir);     
        System.out.printf("MultiThreadLDA: directory %s\n", dir.getPath());
        outputDir = dir;       
        workersArray = new WorkerLDA[nWorkers];
               
        // load up the workers from saved state
        for (int i=0 ; i<nWorkers ; ++i) {
            WorkerLDA worker = new WorkerLDA(this.workerXMLFiles[i]);
            workersArray[i] = worker;
            this.workers.add(worker);
            int[][] nwW = new int[V][K];
            int[] nwsumW = new int[K];
            workersArray[i].setGlobalWordCounts(nwsumW);
            workersArray[i].setGlobalWordTopicCounts(nwW);
            workersArray[i].setOffset(iterationOffset);
        }

        nw = new int[V][K];
        nwsum = new int[K];
        accumCounts();
        docs = MultiThreadXML.getDocuments(workersArray);
    }


    public File saveAsXML(File outDir) throws Exception {
        
        // save all the worker xmls
        for (int i = 0; i < this.workersArray.length; ++i) {
            this.workerXMLFiles[i] = workersArray[i].saveAsXML(outDir);
        }
        
        File xmlFile = new File(outDir, "MultiThreadLDA.xml");
        System.out.printf("Saving xml file: %s\n",xmlFile.getPath());
        Element ele = super.toXML();
        OutputStream stream = new FileOutputStream(xmlFile);
        XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
        xmlOut.output(ele, stream);
        stream.close();
        return xmlFile;
    }

    // accumulate the word counts from the workers and pass the accumulated counts back to the workers
    private void accumCounts() {

        accumCounts(nw);  // calculate the word counts from all the workers

        // copy the new word counts to all the workers
        // each worker has a local area for these counts that it modifies during an iteraton
        for (int w = 0; w < workers.size(); ++w) {
            int[][] c = workersArray[w].getGlobalWordTopicCounts();
            for (int i = 0; i < c.length; ++i) {
                for (int j = 0; j < c[i].length; ++j) {
                    c[i][j] = nw[i][j];

                }
            }
        }

        accumCounts(nwsum);
        for (int w = 0; w < workers.size(); ++w) {
            int[] c = workersArray[w].getGlobalWordCounts();
            for (int i = 0; i < c.length; ++i) {
                c[i] = nwsum[i];
            }
        }
    }

    private void accumCounts(int[][] result) {
        for (int i = 0; i < result.length; ++i) {
            Arrays.fill(result[i], 0);
        }

        for (WorkerLDA worker : workersArray) {
            int[][] c = worker.getWordTopicCounts();
            for (int i = 0; i < c.length; ++i) {
                for (int j = 0; j < c[i].length; ++j) {
                    result[i][j] = result[i][j] + c[i][j];
                }
            }
        }
    }

    // adds up the word counts from all the workers individual counts
    private void accumCounts(int[] result) {
        Arrays.fill(result, 0);
        for (WorkerLDA worker : workersArray) {
            int[] c = worker.getWordCounts();
            for (int i = 0; i < c.length; ++i) {
                result[i] = result[i] + c[i];
            }
        }
    }

    @Override
    public Object call() throws Exception {
        ExecutorService service = Executors.newWorkStealingPool();
        for (int i = 1; i <= iterations; i++) {
            System.out.printf("Iteration: %d\n", i);
            List<Future<Object>> futures = service.invokeAll(workers);
            accumCounts();

            if (peDist != null && i >= burnIn * thinning && i % thinning == 0) {
                peDist.add(this);
            }
            if (maxLike){
                int[][] nd = this.getDocumentTopicCounts();
                Likelihood like = new Likelihood(docs,nw,nd,alpha,beta,i);
                double logL = Math.log(like.call());
                if (logL > this.logLike){
                    this.logLike = logL;
                    this.maxLikeZ = this.getZ();
                }
            }
        }
        // flush any remaining iterations
        for (int w = 0; w < this.workersArray.length; ++w) {
            workersArray[w].flushCache();
        }
        saveAsXML(outputDir);
        if (peDist != null) {
            // report the point estimate 
            peDist.statisticReport(outputDir, statistic, burnIn, alpha, beta, totalWords, this.docs, iterations + iterationOffset, K, V);
        }
        
        // report the maximum likelihood iteration
        if (maxLike){
            DocumentTopicCounts dtc = new DocumentTopicCounts(maxLikeZ,K);
            int[][] nd = dtc.call();
            WordTopicCounts wtc = new WordTopicCounts(maxLikeZ,V,K,docs);
            int[][] nw = wtc.call();
            
            PrintStream stream = new PrintStream(new File(outputDir,"MaxLikelihoodTopics.txt"));
            printIntegerMatrix(stream,maxLikeZ);
            stream.close();
            
            stream = new PrintStream(new File(outputDir,"MaxLikelihoodDocumentTopicCounts.txt"));
            printIntegerMatrix(stream,nd);
            stream.close();
            
            stream = new PrintStream(new File(outputDir,"MaxLikelihoodWordTopicCounts.txt"));
            printIntegerMatrix(stream,nw);
            stream.close();            
        }
        return null;
    }

    public static String filePrefix(String prefix, int K, double alpha) {
        return String.format("%s_%d_%.3f", prefix, K, alpha);
    }

    public void setDirectory(File f) throws Exception {
        this.outputDir = f;
        Files.createDirectories(outputDir.toPath());
//        likeStream = new PrintStream(new FileOutputStream(new File(outputDir, "loglikelihoods"), false)); 
        for (WorkerLDA worker : this.workersArray) {
            worker.setDirectory(f);
        }
    }

    public void setThinning(int th) {
        this.thinning = th;
        for (WorkerLDA worker : this.workersArray) {
            worker.setThinning(th);
        }
    }

    public void setBurnIn(int s) {
        burnIn = s;
    }

    public void setIterations(int it) {
        this.iterations = it;
    }

    public void setMaxLike(boolean b){
        this.maxLike = b;
    }
    public int[][] getDocuments() {
        return docs;
    }

    public long getTotalWords() {
        return this.totalWords;
    }

    public RowSumMatrix getAlpha() {
        return alpha;
    }

    public RowSumMatrix getBeta() {
        return beta;
    }

    public int getWorkersSize() {
        return this.workersArray.length;
    }

    public int getVocabSize() {
        return this.V;
    }

    public int getDocumentsSize() {
        return this.D;
    }

    public int getTopicsSize() {
        return this.K;
    }

    public WorkerLDA[] getWorkers() {
        return this.workersArray;
    }

    public int[][] getZ() {
        int[][] ret = new int[D][];
        int d = 0;
        for (int w = 0; w < this.workersArray.length; ++w) {
            int[][] z = this.workersArray[w].z;
            for (int i = 0; i < z.length; ++i) {
                ret[d] = z[i];
                ++d;
            }
        }
        return ret;
    }

    public int[][] getDocumentTopicCounts() {
        int[][] ret = new int[D][];
        int d = 0;
        for (int w = 0; w < this.workersArray.length; ++w) {
            int[][] nd = this.workersArray[w].getDocumentTopicCounts();
            for (int i = 0; i < nd.length; ++i) {
                ret[d] = nd[i];
                ++d;
            }
        }
        return ret;
    }

    public int[][] getWordTopicCounts() {
        return this.nw;
    }

    public double getMaxLogLikelihood(){
        return this.logLike;
    }
    public int[][] getMaxLikelihoodZ(){
        return this.maxLikeZ;
    }
    
    static void printIntegerMatrix(PrintStream stream,int[][] m){
        for (int r=0 ; r<m[r].length ; ++r){
            stream.printf("%d", m[r][0]);
            for (int c=1 ; c<m[r].length ; ++c){
                stream.printf(",%d",m[r][c]);
            }
            stream.println();
        }
    }
}
