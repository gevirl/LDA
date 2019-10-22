/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;


/**
 *
 * @author gevirl
 */
public class WorkerZIterationFile implements Callable<int[][][]> {

    File file;
    int skip = 0;  // number of iterations to ignore at the begining of the list

    public WorkerZIterationFile(File file) {
        this.file = file;
    }

    // reads the single worker iteration file and form the z array for each iteration
    @Override
    public int[][][] call() throws Exception {
        System.out.printf("Starting %s %s \n",this.toString(),file.getName());
        ArrayList<int[][]> zlist = readZlist();
        int[][][] ret = new int[zlist.size()][][];
        int i=0;
        for (int[][] z : zlist){
            ret[i] = z;
            ++i;
        }
        System.out.printf("Ending %s %s \n",this.toString(),file.getName());
        return ret;
    }
    
    // read all the topic choice iterations
    public ArrayList<int[][]> readZlist() throws Exception {
 //       System.out.printf("Reading file: %s\n", file.getPath());
        ObjectInputStream stream = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
        ArrayList<int[][]> zList = (ArrayList<int[][]>) stream.readObject();
        stream.close();
        return zList;
    }
/*
    // form histogram of topics for each document/word for the iterations in this file, 
    // skip initial iterations to implement burnin if skip >0
    // returns iteration counts for doc,word,topic
    public int[][][] topicHistogram() throws Exception {
        List zList = readZlist();
        int[][] z = (int[][]) zList.get(0);
        int nTopics = worker.getTopicCount();

        int[][][] ret = new int[worker.getDocumentsSize()][][];  // doc,word,topic -> count       
        for (int d = 0; d < z.length; ++d) {
            ret[d] = new int[z[d].length][nTopics];
        }

        for (int iter = skip; iter < zList.size(); ++iter) {
            z = (int[][]) zList.get(iter);
            for (int d = 0; d < z.length; ++d) {
                for (int w = 0; w < z[d].length; ++w) {
                    ++ret[d][w][z[d][w]];
                }
            }
        }
        return ret;
    }

    // create kdes for each document,topic
    // initialize the kde with this file's iteration marginals
    public KernelEstimator[][] docTopicKDE(double precision) throws Exception {
        int nTopics = worker.getTopicCount();
        int nDocs = worker.getDocumentsSize();
        KernelEstimator[][] kde = new KernelEstimator[nDocs][nTopics];
        for (int d = 0; d < nDocs; ++d) {
            for (int t = 0; t < nTopics; ++t) {
                kde[d][t] = new KernelEstimator(precision);
            }
        }
        docTopicInto(kde);
        return kde;
    }
    public void docTopicInto(IntegerHistogram[][] iHist) throws Exception {
        int nTopics = iHist[0].length;
        List zList = readZlist();

        for (int iter = skip; iter < zList.size(); ++iter) {
            int[][] nd = DocTopicCounts.docTopicCounts((int[][]) zList.get(iter), nTopics);
            for (int d = 0; d < nd.length; ++d) {
                for (int t = 0; t < nTopics; ++t) {
                    iHist[d][t].addValue(nd[d][t]);
                }
            }
        }
    }
    // add the iteration marginals for this file to an existing document,topic kde
    public void docTopicInto(KernelEstimator[][] kde) throws Exception {
        int nTopics = kde[0].length;
        List zList = readZlist();

        for (int iter = skip; iter < zList.size(); ++iter) {
            int[][] nd = DocTopicCounts.docTopicCounts((int[][]) zList.get(iter), nTopics);
            for (int d = 0; d < nd.length; ++d) {
                for (int t = 0; t < nTopics; ++t) {
                    kde[d][t].addValue(nd[d][t], 1.0);
                }
            }
        }
    }
*/


    public void resetSkip(int sk) {
        this.skip = sk;
    }

}
