/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 *
 * @author gevirl
 */
public class WorkerZIterationFile implements Callable<int[][][]> {

    File file;
    int skip = 0;  // number of iterations to ignore at the begining of the list
    int worker;
    int lastIter;
    

    public WorkerZIterationFile(File file) {
        this.file = file;
        parseFileName();
    }

    public void saveZList(ArrayList<int[][]> zlist,File f)throws Exception {
        
        ObjectOutputStream stream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(f)));
        stream.writeObject(zlist);
        stream.close();        
    }
    // reads the single worker iteration file and form the z array for each iteration
    @Override
    public int[][][] call() throws Exception {
        System.out.printf("Starting %s %s \n",this.toString(),file.getName());
        ArrayList<int[][]> zlist = readZlist();
        int[][][] ret = new int[zlist.size()][][];
        int i=0;
        for (int[][] z : zlist){
            ret[i] = z;
            ++i;
        }
        System.out.printf("Ending %s %s \n",this.toString(),file.getName());
        return ret;
    }
    
    // read all the topic choice iterations
    public ArrayList<int[][]> readZlist() throws Exception {
        System.out.printf("Reading file: %s\n", file.getPath());
        ObjectInputStream stream = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
        ArrayList<int[][]> zList = (ArrayList<int[][]>) stream.readObject();
        stream.close();
        System.out.printf("Closingfile: %s\n", file.getPath());
        return zList;
    }



    public void resetSkip(int sk) {
        this.skip = sk;
    }
    
    final public void parseFileName(){
        lastIter = Integer.parseInt(file.getName().substring(4, 10));
        worker = Integer.parseInt(file.getName().substring(16, file.getName().lastIndexOf(".")));
        
    }
    
   
    static public void main(String[] args)throws Exception {
        File dir = new File("/net/waterston/vol9/flyATAC/all.peak_matrix_out_save1/S1_UW_6-10.peak_matrix.mtx.gz_topics60_alpha3.000_beta2000.000");
        for (File file : dir.listFiles()){
            if (file.getName().endsWith(".Z")){
                System.out.printf("File: %s\n", file.getName());
                WorkerZIterationFile iterFile = new WorkerZIterationFile(file);
                int[][][] z = iterFile.call();
                int woiefhsiah=0;
            }
        }
    }
}
