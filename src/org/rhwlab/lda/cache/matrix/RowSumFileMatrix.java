/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.jdom2.Element;
import org.rhwlab.lda.UtilsXML;

/**
 *
 * @author gevirl
 */
public class RowSumFileMatrix implements RowSumMatrix {

    double conc;
    int nRows;
    int nCols;
    File file;
    double[][] values;
    double[] rowSums;

    public RowSumFileMatrix(Element ele) throws Exception {
        values = UtilsXML.doubleArray(ele);
        rowSums = new double[values.length];
        doSums();
    }

    public RowSumFileMatrix(double conc, int nRows, int nCols, File file) {
        this.conc = conc;
        this.nCols = nCols;
        this.nRows = nRows;
        this.file = file;
    }

    public String build() {
        values = new double[nRows][nCols];
        rowSums = new double[nRows];
        int r = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                String[] tokens = line.split(",|\t");
                if (tokens.length != nCols) {
                    return String.format("File: %s, row length is not %d", file.getPath(), nCols);
                }
                for (int c = 0; c < nCols; ++c) {
                    values[r][c] = conc * Double.valueOf(tokens[c]);
                }
                double sum = 0.0;
                for (int c = 0; c < nCols; ++c) {
                    sum = sum  + values[r][c];
                }
                if (sum < .99 || sum >1.01){
                    return String.format("File: %s , row %d does not sum to one",file.getPath(),r);
                }
                ++r;
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception exc) {
            StringWriter writer = new StringWriter();
            exc.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }

        while (r < nRows) {
            for (int c = 0; c < nCols; ++c) {
                values[r][c] = conc / nCols;
            }
            ++r;
        }
        doSums();
        return null;
    }

    @Override
    public Element toXML(String name) {
        Element ret = UtilsXML.asElement(name, values);
        ret.setAttribute("class", this.getClass().getName());
        return ret;
    }

    private void doSums() {
        for (int r = 0; r < values.length; ++r) {
            rowSums[r] = 0.0;
            for (int c = 0; c < values[0].length; ++c) {
                rowSums[r] = rowSums[r] + values[r][c];
            }
        }
    }

    @Override
    public double getValue(int r, int c) {
        return values[r][c];
    }

    @Override
    public double getSum(int r) {
        return rowSums[r];
    }

    @Override
    public double getConcentration() {
        return this.conc;
    }
}
