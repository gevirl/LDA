/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.io.File;

/**
 *
 * @author gevirl
 */
public class MergeDocuments {

    static public void main(String[] args) throws Exception {
        File mmFile = new File("/net/waterston/vol9/flyRNA/broad_matrix.mtx.gz");
        File clusterfile = new File("/net/waterston/vol9/flyRNA/broad300/broad_matrix.mtx.gz_topics300_alpha50.000_beta5000.000/nn_multi_Wards_50000.csv");
        
        MatrixMarket mm = new MatrixMarket(mmFile,false);
        mm.mergeDocuments(clusterfile, 4);
    }
}
