/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.generative;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gevirl
 */
public class DoubletDocument extends MultiDocument {
    
    public DoubletDocument(Document doc1,Document doc2) {
        super(asList(doc1,doc2));
    }
    static List<Document> asList(Document d1,Document d2){
        ArrayList<Document> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);
        return list;
    }
}
