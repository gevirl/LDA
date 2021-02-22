/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author gevirl
 */
public class Test {
    static public void main(String[] args)throws Exception {
        
        String[] s = {"a","b","c"};
        ArrayList<String> l = new ArrayList<>(Arrays.asList(s));
        int auisdhf=0;
        
        int[][] cache = new int[10][10];
        cache[0][0] = 200;
            File f = new File("/net/waterston/vol2/home/gevirl/test.obj");
           ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(f));
//            ObjectOutputStream stream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(f)));
            stream.writeObject(cache);
            stream.close();      
            
            ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(f));
            int[][] obj = (int[][])inStream.readObject();
            int asiudfhiuas=0;
    }
}
