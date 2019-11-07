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
public interface RowSumMatrix {
    public double getConcentration();
    public double getValue(int r,int c);
    public double getSum(int r);
    public Element toXML(String name);
    public static RowSumMatrix factory(Element ele) throws Exception{
        String cl = ele.getAttributeValue("class");
        if (cl.contains("RowSumSymetricMatrix")){
            return new RowSumSymetricMatrix(ele);
        }
        if (cl.contains("RowSumFileMatrix")){
            return new RowSumFileMatrix(ele);
        }
        return null;
    }
}
