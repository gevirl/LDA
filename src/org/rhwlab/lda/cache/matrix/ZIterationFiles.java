/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.rhwlab.lda.cache.PointEstimateDistribution;

/**
 *
 * @author gevirl
 */
// all the worker ZIterationFiles for a given set of iterations
// assumes the skip is less than the number of records in the files
public class ZIterationFiles  {

    Collection<WorkerZIterationFile> files;
//    ArrayList<ArrayList<int[][]>> lists = new ArrayList<>();
    int skip;
    int nDocs;  // total number of documents in all the worker files
    PointEstimateDistribution dist;

    public ZIterationFiles(Collection<WorkerZIterationFile> files, int skip, int nDocs) throws Exception {
        this.files = files;
        this.skip = skip;
        this.nDocs = nDocs;
    }

    // multithread reading of all individual worker files and appending the documents together into a single z array for each iteration
    public int[][][] readFiles() throws Exception {
        int nWorkers = files.size();
        ArrayList[] zLists = new ArrayList[nWorkers];  // an array of lists, indexed by worker
        int work = 0;
        for (WorkerZIterationFile file : files){
            zLists[work] = file.readZlist();
            ++work;
        }
        int nrecs = zLists[0].size();  // number of iterations in the set of workers
        int[][][] ret = new int[nrecs-skip][][];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new int[nDocs][];
        }
        
        int iter = 0; 
        for (int i = skip; i < nrecs; ++i) {
            int d = 0;
            for (int w =0 ; w<nWorkers ; ++w) {
                Object obj  = zLists[w].get(i);
                int[][] z = (int[][])obj;  // topics for interation i of worker w
                for (int j = 0; j < z.length; ++j) {
                    ret[iter][d] = z[j];
                    ++d;
                }               
            }
            ++iter;
        }        
        
 /*       
        ExecutorService service = Executors.newWorkStealingPool();
        List<Future<int[][][]>> futures = service.invokeAll(files);

        int nrecs = futures.get(0).get().length;
        int[][][] ret = new int[nrecs-skip][][];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new int[nDocs][];
        }
        
        int iter = 0; 
        for (int i = skip; i < nrecs; ++i) {
            int d = 0;
            for (Future<int[][][]> future : futures) {
                int[][][] z = future.get();                             
                for (int j = 0; j < z[i].length; ++j) {
                    ret[iter][d] = z[i][j];
                    ++d;
                }               
            }
            ++iter;
        }
 */
        return ret;
    }

    
    public void setDistribution(PointEstimateDistribution dist){
        this.dist = dist;
    }


}
