/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.pointestimate;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.rhwlab.lda.cache.matrix.WorkerZIterationFile;
import org.rhwlab.lda.cache.matrix.ZDirectory;

/**
 *
 * @author gevirl
 */
public class TopicMode {

    ZDirectory zDir;

    public TopicMode(ZDirectory zDir) {
        this.zDir = zDir;
    }

    public int[][] getModes(int skip, int worker, int nTopics) throws Exception {
        int count = 1;
        TreeMap<Integer, WorkerZIterationFile> files = zDir.getWorkerFiles(worker);
        Entry<Integer, WorkerZIterationFile> entry = files.firstEntry();
        ArrayList<int[][]> iterList = entry.getValue().readZlist();
        int[][] ret = iterList.get(0);

        // initialize the histograms for each doc/word
        int[][][] hists = new int[ret.length][][];
        for (int i = 0; i < hists.length; ++i) {
            hists[i] = new int[ret[i].length][];
            for (int j = 0; j < hists[i].length; ++j) {
                hists[i][j] = new int[nTopics];
            }
        }

        while (entry != null) {

            for (int[][] z : iterList) {
                if (count > skip) {
                    for (int i = 0; i < z.length; ++i) {
                        for (int j = 0; j < z[i].length; ++j) {
                            ++hists[i][j][z[i][j]];
                        }
                    }
                }
                ++count;
            }
            entry = files.higherEntry(entry.getKey());
            if (entry != null) {
                iterList = entry.getValue().readZlist();
            }

        }

        // find the maximum topic
        for (int d = 0; d < hists.length; ++d) {
            for (int w = 0; w < hists[d].length; ++w) {
                int max = 0;
                int mode = -1;
                for (int t = 0; t < hists[d][w].length; ++t) {
                    if (hists[d][w][t] > max) {
                        max = hists[d][w][t];
                        mode = t;
                    }
                }

                ret[d][w] = mode;
            }
        }
        return ret;
    }
}
