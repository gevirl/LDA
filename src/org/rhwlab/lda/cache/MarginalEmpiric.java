/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;
import java.util.List;

/**
 *
 * @author gevirl
 */
public class MarginalEmpiric extends PointEstimatorDistBase {
    


    @Override
    public String getLabel() {
        return "empiric";
    }

    @Override
    public PointEstimates getEstimates(String statistic) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    @Override
    public void add(Object obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
