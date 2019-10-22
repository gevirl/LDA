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
public class RowSumMatrix {
    double[][] values;
    double[] rowSums;
    
    public RowSumMatrix(Element ele)throws Exception {
        values = UtilsXML.doubleArray(ele);
        rowSums = new double[values.length];
        doSums();
    }
    
    public RowSumMatrix(int nRows,int nCols,File file)throws Exception {
        this(nRows,nCols);
        int r=0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null){
            String[] tokens = line.split(",|\t");
            for (int c=0 ; c<nCols ; ++c){
                if (c >= tokens.length) {
                    values[r][c] = values[r][c-1];
                } else {
                    values[r][c] = Double.valueOf(tokens[c]);
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
    public RowSumMatrix(int nRows,int nCols,double v){
        this(nRows,nCols);
        for (int r=0 ; r<nRows ; ++r){
            for (int c=0 ; c<nCols ; ++c){
                values[r][c] = v;
            }
        }
        doSums();
    }
    
    public RowSumMatrix(int nRows,int nCols){
        values = new double[nRows][nCols];
        rowSums = new double[nRows];
    }
    
    public Element toXML(String name){    
        return UtilsXML.asElement(name, values);       
    }
    
    private void doSums(){
        for (int r=0 ; r<values.length ; ++r){
            rowSums[r] = 0.0;
            for (int c=0 ; c<values[0].length ; ++c){
                rowSums[r] = rowSums[r] + values[r][c];
            }
        }
    }
    
    public double[][] getValues(){
        return values;
    }
    
    public double[] getSums(){
        return rowSums;
    }
}
