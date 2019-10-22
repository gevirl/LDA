/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.PrintStream;
import java.util.concurrent.Callable;

/**
 *
 * @author gevirl
 */
public class TextCounts implements Callable {

    int t;
    int[][][] nd;
    PrintStream stream;
    String label;

    public TextCounts(PrintStream stream,int[][][] nd,int t,String label){
        this.t = t;
        this.stream = stream;
        this.nd = nd;
        this.label = label;
    }
    @Override
    public Object call() throws Exception {
        
        System.out.printf("Output started for %s\n",label);
        for (int i = 0; i < nd.length; ++i) {
            boolean first = true;
            for (int d = 0; d < nd[i].length; ++d) {
                if (!first) {
                    stream.print(",");
                }
                first = false;
                stream.print(nd[i][d][t]);
            }

            stream.println();
        }
        System.out.printf("Output ended for %s\n",label);
        return null;
    }

}
