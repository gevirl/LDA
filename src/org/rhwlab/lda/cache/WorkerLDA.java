/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author gevirl
 */
public class WorkerLDA extends WorkerXML implements Callable  {
       
    
    int thinning = 1;

    int[][] nwGlobal;  // a local copy of the global word counts (all documents of all the workers)
    int[] nwsumGlobal;
    
    ArrayList<int[][]> cache = new ArrayList<>();
    int iter = 0;
    int iterOffset = 0;

    File dir;

    public WorkerLDA(String id, int[][] documents, int V, int K, double alpha, double beta, long seed) {

        this.workID = id;
        this.documents = documents;
        this.V = V;
        this.K = K;
        this.alpha = alpha;
        this.beta = beta;
        this.rand = new Random(seed);
        initialState();
    }

    public WorkerLDA(File xml) throws Exception {
        super(xml);
        super.call();
    }

    final void initialState() {

        int M = documents.length;

        // initialise count variables.
        nw = new int[V][K];
        nd = new int[M][K];
        nwsum = new int[K];
        ndsum = new int[M];

        // The z_i are are initialised to values in [1,K] to determine the
        // initial state of the Markov chain.
        z = new int[M][];
        for (int m = 0; m < M; m++) {

            int N = documents[m].length;
            z[m] = new int[N];
            for (int n = 0; n < N; n++) {
                int topic = (int) (rand.nextDouble() * K);
                z[m][n] = topic;
                // number of instances of word i assigned to topic j
                nw[documents[m][n]][topic]++;
                // number of words in document i assigned to topic j.
                nd[m][topic]++;
                // total number of words assigned to topic j.
                nwsum[topic]++;
            }
            // total number of words in document i
            ndsum[m] = N;
        }
    }

    @Override
    public Object call() throws Exception {
        ++iter;
        for (int m = 0; m < z.length; m++) {
            for (int n = 0; n < z[m].length; n++) {

                int topic = sampleFullConditional(m, n);
                z[m][n] = topic;
            }
        }
        if (cacheSize > 0) {
            if (iter % thinning == 0) {
                int[][] zclone = z.clone();
                for (int i = 0; i < z.length; ++i) {
                    zclone[i] = z[i].clone();
                }
                cache.add(zclone);
                if (cache.size() == cacheSize) {
                    flushCache();
                }
            }
        } 
        return this.workID;
    }

    public void flushCache() throws Exception {
        if (!cache.isEmpty()) {
            File f = new File(dir, String.format("Iter%06d%s.Z", iter + iterOffset, this.workID));
//            GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(f)){{def.setLevel(Deflater.BEST_COMPRESSION);}};
            
            ObjectOutputStream stream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(f)));
            stream.writeObject(cache);
            stream.close();
            this.iterationFiles.add(f);
            this.iterFileSizes.add(cache.size());
            cache.clear();
        }
    }

    private int sampleFullConditional(int m, int n) {

        // remove z_i from the count variables
        int topic = z[m][n];
        nw[documents[m][n]][topic]--;
        nwGlobal[documents[m][n]][topic]--;
        nd[m][topic]--;
        nwsum[topic]--;
        nwsumGlobal[topic]--;
        ndsum[m]--;

        // do multinomial sampling via cumulative method:
        double[] p = new double[K];
        for (int k = 0; k < K; k++) {
            p[k] = (nwGlobal[documents[m][n]][k] + beta) / (nwsumGlobal[k] + V * beta)
                    * (nd[m][k] + alpha) / (ndsum[m] + K * alpha);
        }
        // cumulate multinomial parameters
        for (int k = 1; k < p.length; k++) {
            p[k] += p[k - 1];
        }
        // scaled sample because of unnormalised p[]
        double u = rand.nextDouble() * p[K - 1];
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic]) {
                break;
            }
        }

        // add newly estimated z_i to count variables
        nw[documents[m][n]][topic]++;
        nwGlobal[documents[m][n]][topic]++;
        nd[m][topic]++;
        nwsum[topic]++;
        nwsumGlobal[topic]++;
        ndsum[m]++;

        return topic;
    }
    
    public int[][] getGlobalWordTopicCounts() {
        return this.nwGlobal;
    }

    public int[] getGlobalWordCounts() {
        return this.nwsumGlobal;
    }

    
 
    public void setDirectory(File dir) {
        this.dir = dir;
    }

    public void setCacheSize(int s) {
        this.cacheSize = s;
    }

    public void setThinning(int th) {
        this.thinning = th;
    }

    public void setOffset(int off) {
        this.iterOffset = off;
    }
    
    public void setGlobalWordTopicCounts(int[][] nwGlobal) {
        this.nwGlobal = nwGlobal;
    }

    public void setGlobalWordCounts(int[] nwsumGlobal) {
        this.nwsumGlobal = nwsumGlobal;
    }
}
