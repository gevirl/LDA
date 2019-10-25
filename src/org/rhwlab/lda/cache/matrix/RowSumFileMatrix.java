/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.jdom2.Element;
import org.rhwlab.lda.UtilsXML;

/**
 *
 * @author gevirl
 */
public class RowSumFileMatrix implements RowSumMatrix{
    double[][] values;
    double[] rowSums;
    
    public RowSumFileMatrix(Element ele)throws Exception {
        values = UtilsXML.doubleArray(ele);
        rowSums = new double[values.length];
        doSums();
    }
    
    public RowSumFileMatrix(double conc,int nRows,int nCols,File file)throws Exception {
        values = new double[nRows][nCols];
        rowSums = new double[nRows];
        int r=0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null){
            String[] tokens = line.split(",|\t");
            for (int c=0 ; c<nCols ; ++c){
                if (c >= tokens.length) {
                    values[r][c] = values[r][c-1];
                } else {
                    values[r][c] = conc*Double.valueOf(tokens[c]);
                }
            }
            ++r;
            line = reader.readLine();
        }
        reader.close();
        
        while (r < nRows){
            for (int c=0 ; c<nCols ; ++c){
                values[r][c] = values[r-1][c];
            }
            ++r;
        }
        doSums();
    }

    
    @Override
    public Element toXML(String name){    
        Element ret = UtilsXML.asElement(name, values);   
        ret.setAttribute("class", this.getClass().getName());
        return ret;
    }
    
    private void doSums(){
        for (int r=0 ; r<values.length ; ++r){
            rowSums[r] = 0.0;
            for (int c=0 ; c<values[0].length ; ++c){
                rowSums[r] = rowSums[r] + values[r][c];
            }
        }
    }
    
    @Override
    public double getValue(int r,int c){
        return values[r][c];
    }
    
    @Override
    public double getSum(int r){
        return rowSums[r];
    }
}
