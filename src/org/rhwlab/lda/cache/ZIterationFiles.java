/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author gevirl
 */
// all the worker ZIterationFiles for a given set of itertions
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
 
        return ret;
    }

    
    public void setDistribution(PointEstimateDistribution dist){
        this.dist = dist;
    }


}
