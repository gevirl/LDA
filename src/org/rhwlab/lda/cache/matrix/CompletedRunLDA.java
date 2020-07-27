/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author gevirl
 */
public class CompletedRunLDA extends MultiThreadXMLBase {

    int[][] docs = null;
    TreeMap<String, Integer> iterMap = new TreeMap<>();

    public CompletedRunLDA(File dir) throws Exception {
        super(dir);
        docs = new int[this.D][];
        int index = 0;
        for (File workerXMLfile : this.workerXMLFiles) {
            WorkerXML workerXML = new WorkerXML(workerXMLfile);
            workerXML.call();  // reads the xml
            List<Integer> iterSizes = workerXML.getIterationSizes();
            List<File> iterFiles = workerXML.getIterationFiles();
            for (int i = 0; i < iterSizes.size(); ++i) {
                iterMap.put(iterFiles.get(i).getPath(), iterSizes.get(i));
            }
            int[][] workerDocs = workerXML.getDocuments();
            for (int j = 0; j < workerDocs.length; ++j) {
                docs[index] = workerDocs[j];
                ++index;
            }
        }
    }

    public int[][] getDocuments() throws Exception {

        return docs;
    }
    public Integer getRecordCount(String filePath){
        return iterMap.get(filePath);
    }
}
