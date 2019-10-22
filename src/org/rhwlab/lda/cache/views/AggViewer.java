/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author gevirl
 */
public class AggViewer extends JFrame {
    
    File[] docFiles;
    File[] wordFiles;
    double[] like=null;
    JTextField docField = new JTextField("           ");
    ColumnsPanel mainPanel = new ColumnsPanel();
    
    public AggViewer(File dir)throws Exception {
        super();
        TreeMap<Integer,File> docs = new TreeMap<>();
        TreeMap<Integer,File> words = new TreeMap<>();
        for (File file : dir.listFiles()){
            if (file.getName().startsWith("Topic")){
                int topic = Integer.valueOf(file.getName().substring(5, 8));
                if (file.getName().contains("Doc")){
                    docs.put(topic,file);
                }else {
                    words.put(topic, file);
                }
            } 
        }
        docFiles = docs.values().toArray(new File[0]);
        wordFiles = words.values().toArray(new File[0]);
        File logLikeFile = new File(dir,"LogLikelihoods");
        if (logLikeFile.exists()){
            ArrayList<Double> list = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(logLikeFile));
            String line = reader.readLine();
            while (line != null){
                list.add(-Double.valueOf(line)/10000000.0);
                line = reader.readLine();
            }
            like = new double[list.size()];
            for (int i=0 ; i<like.length ; ++i){
                like[i] = list.get(i);
            }
            reader.close();
        }
        
        docField.setColumns(20);
        JButton docButton = new JButton("Display Documents");
        docButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    displayDocs();
                } catch (Exception exc){
                    exc.printStackTrace();
                }
            }
        });
        
        JButton wordButton = new JButton("Display Words");
        wordButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    displayWords();
                } catch (Exception exc){
                    exc.printStackTrace();
                }
            }
        });        
        this.getContentPane().setLayout(new BorderLayout());
        
        JPanel south = new JPanel();
        south.add(new JLabel("Documents or Words:"));
        south.add(docField);
        south.add(docButton);
        south.add(wordButton);
        this.getContentPane().add(south, BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel,BorderLayout.CENTER);
        
    }
    
    private void displayDocs() throws Exception {
        String[] tokens = docField.getText().trim().split(",");
        int[] docs = new int[tokens.length];
        for (int i=0 ; i<docs.length ; ++i){
            docs[i] = Integer.valueOf(tokens[i]);
        }
        mainPanel.setColumns("Document",docFiles,docs,true,like);
    }
    private void displayWords() throws Exception {
        String[] tokens = docField.getText().trim().split(",");
        int[] docs = new int[tokens.length];
        for (int i=0 ; i<docs.length ; ++i){
            docs[i] = Integer.valueOf(tokens[i]);
        }
        mainPanel.setColumns("Word",wordFiles,docs,true,like);
    }    
    static public void main(String[] args)throws Exception {
        AggViewer viewer = new AggViewer(new File("/net/waterston/vol2/home/gevirl/ldatest/peaksSynthetic/peaksSynthetic_topics60_alpha0.050_beta0.130"));
        
        viewer.setSize(500, 500);
       viewer.pack();;
        viewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         viewer.setVisible(true);
        int uasidfuish=0;
    }
}
