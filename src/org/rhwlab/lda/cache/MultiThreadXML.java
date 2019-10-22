/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author gevirl
 */
public class MultiThreadXML extends MultiThreadXMLBase {

    int[][] docs;
    WorkerXML[] workerXMLs;

    public MultiThreadXML(File dir) throws Exception {
        super(dir);
        
        Collection<Callable<Object>> threadCollection = new ArrayList<>();
        workerXMLs = new WorkerXML[getWorkersSize()];
        for (int w=0 ; w<workerXMLs.length ; ++w) {
            workerXMLs[w] = new WorkerXML(this.workerXMLFiles[w]);
            threadCollection.add(workerXMLs[w]);
        }
        ExecutorService service = Executors.newWorkStealingPool();
        List<Future<Object>> futures = service.invokeAll(threadCollection);
        for (Future<Object> future : futures) {
            future.get();
        }

        docs = new int[getDocumentsSize()][];
        int r = 0;
        for (int w = 0; w < getWorkersSize(); ++w) {
            int[][] nd = workerXMLs[w].getDocuments();
            for (int d = 0; d < nd.length; ++d) {
                docs[r] = nd[d];
                ++r;
            }
        }

    }

    public int[][] getDocuments() throws Exception {
        return docs;
    }

    public WorkerXML[] getWorkers() throws Exception {
        return workerXMLs;
    }
}
