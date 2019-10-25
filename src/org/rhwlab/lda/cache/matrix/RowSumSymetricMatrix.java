/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import org.jdom2.Element;

/**
 *
 * @author gevirl
 */
public class RowSumSymetricMatrix implements RowSumMatrix {
    double value;
    double sum;
    
    public RowSumSymetricMatrix(double conc,int nCols){
        this.value = conc/nCols;
        this.sum = conc;
    }
    
    public RowSumSymetricMatrix(Element ele){
        value = Double.valueOf(ele.getAttributeValue("value"));
        sum = Double.valueOf(ele.getAttributeValue("sum"));
    }
    @Override
    public double getValue(int r, int c) {
        return value;
    }

    @Override
    public double getSum(int r) {
        return sum;
    }

    @Override
    public Element toXML(String name) {
        Element ret = new Element(name);
        ret.setAttribute("class", this.getClass().getName());
        ret.setAttribute("value",Double.toString(value));
        ret.setAttribute("sum",Double.toString(sum));
        return ret;
    }
    
}
