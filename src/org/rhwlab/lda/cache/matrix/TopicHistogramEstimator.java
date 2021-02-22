/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.rhwlab.lda.cache.DocumentTopicCounts;
import org.rhwlab.lda.cache.PointEstimates;
import org.rhwlab.lda.cache.WordTopicCounts;

/**
 *
 * @author gevirl
 */
public class TopicHistogramEstimator extends PointEstimatorDistBase {

    int nTopics;
    int nVocab;
    int[][] docs;
    int[][][] hists;
    int[][] modes = null;

    public TopicHistogramEstimator(int[][] docs, int nVocab, int nTopics) {
        super();
        this.nTopics = nTopics;
        this.nVocab = nVocab;
        this.docs = docs;
        hists = new int[docs.length][][];
        for (int i = 0; i < hists.length; ++i) {
            if (i % 10000 == 0) {
                System.out.printf("TopicHistogramEstimator: doc %d\n", i);
            }
            hists[i] = new int[docs[i].length][];
            for (int j = 0; j < hists[i].length; ++j) {
                hists[i][j] = new int[nTopics];
            }
        }
    }

    public TopicHistogramEstimator(ZDirectory zDir, int skip) throws Exception {
        super(zDir);

        // calculate the topic modes for each document/token by worker
        CompletedRunLDA lda = zDir.getCompletedRun();
        nVocab = lda.getVocabSize();
        nTopics = lda.getTopicsSize();
        docs = lda.getDocuments();
        modes = new int[docs.length][];
        int d = 0;
        int m=0;
        int nWorkers = lda.getWorkersSize();
        
        for (int w = 0; w < nWorkers; ++w) {
            int iter = 0;
            TopicHistogramEstimator wEst = null;
            TreeMap<Integer, WorkerZIterationFile> workerFiles = zDir.getWorkerFiles(w);
            Entry e = workerFiles.firstEntry();
            while (e != null) {
                Integer iteration = (Integer) e.getKey();
                WorkerZIterationFile wziFile = (WorkerZIterationFile) e.getValue();
                ArrayList<int[][]> zlist = wziFile.readZlist();
                if (wEst == null) {
                    int[][] z = zlist.get(0);
                    int[][] wdocs = new int[z.length][];
                    for (int i = 0; i < wdocs.length; ++i) {
                        wdocs[i] = docs[d];
                        ++d;
                    }
                    wEst = new TopicHistogramEstimator(wdocs, nVocab, nTopics);
                }

                // add the iterations for this worker
                for (int[][] z : zlist) {
                    ++iter;
                    if (iter > skip) {
                        wEst.add(z);
                    }
                }

                e = workerFiles.higherEntry(iteration);
            }
            int[][] wModes = wEst.getModes();
            for (int i=0 ; i<wModes.length ; ++i){
                modes[m] = wModes[i];
                ++m;
            }
        }
    }

    @Override
    public PointEstimates getEstimates(String stat) throws Exception {

        int[][] modes = getModes();

        int[][] nw = WordTopicCounts.wordTopicCounts(modes, nVocab, nTopics, docs);
        double[][] wordEst = new double[nVocab][nTopics];
        for (int i = 0; i < nVocab; ++i) {
            for (int j = 0; j < nTopics; ++j) {
                wordEst[i][j] = nw[i][j];
            }
        }

        int[][] nd = DocumentTopicCounts.documentTopicCounts(modes, nTopics);
        double[][] docEst = new double[nd.length][nTopics];
        for (int i = 0; i < nd.length; ++i) {
            for (int j = 0; j < nTopics; ++j) {
                docEst[i][j] = nd[i][j];
            }
        }
        return new PointEstimates(docEst, wordEst);
    }

    @Override
    public String getLabel() {
        return "topic";
    }

    @Override
    public void add(Object obj) {
        int[][] z;
        if (obj instanceof MultiThreadLDA) {
            z = ((MultiThreadLDA) obj).getZ();
        } else {
            z = (int[][]) obj;
        }

        for (int i = 0; i < z.length; ++i) {
            for (int j = 0; j < z[i].length; ++j) {
                ++hists[i][j][z[i][j]];
            }
        }
    }

    public int[][] getModes() {
        if (modes == null) {
            modes = new int[hists.length][];  // doc,word -> topic (mode)
            for (int d = 0; d < hists.length; ++d) {
                modes[d] = new int[hists[d].length];
                for (int w = 0; w < hists[d].length; ++w) {
                    int max = 0;
                    int mode = -1;
                    for (int t = 0; t < hists[d][w].length; ++t) {
                        if (hists[d][w][t] > max) {
                            max = hists[d][w][t];
                            mode = t;
                        }
                    }
                    modes[d][w] = mode;
                }
            }
        }
        return modes;
    }

}
