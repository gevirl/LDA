/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import org.rhwlab.lda.generative.Corpus;
import org.rhwlab.lda.generative.Document;
import smile.math.distance.Distance;
import smile.math.distance.JensenShannonDistance;
import weka.core.Utils;

/**
 *
 * @author gevirl
 */
public class ThetaComparison {
    double[] distances;
    
    public ThetaComparison(double[][] theta1,double[][]theta2,PhiComparison phiComp , double thresh,Distance measure ){
        distances = new double[theta1.length];
        
        Random rnd = new Random();
        int n = theta2[0].length;
        
        for (int i=0 ; i<distances.length ; ++i){
            ArrayList<Double> list1 = new ArrayList<>();
            ArrayList<Double> list2 = new ArrayList<>();
            
            for (int j=0 ; j<theta1[0].length ; ++j){
                if (phiComp != null){
                    TopicMatch[] m = phiComp.getMatches();
                    if (m[j].jsd <= thresh){
                        list1.add(theta1[i][j]);
                        list2.add(theta2[i][m[j].topic]);
                    }
                } else {
                    list1.add(theta1[i][j]);
                    list2.add(theta2[i][rnd.nextInt(n)]);
                }
            }
            
            double[] th1 = new double[list1.size()];
            double[] th2 = new double[list1.size()];
            for (int j=0 ; j <th1.length ; ++j){
                th1[j] = list1.get(j);
                th2[j] = list2.get(j);
            }
            distances[i] = measure.d(th1,th2);
        }
    }
    public double getMean(){
        return Utils.mean(distances);
    }
    public double[] getDistances(){
        return this.distances;
    }
    
    public TreeMap<String,Double> meansByTypes(Corpus corp){
        TreeMap<String,Double> ret = new TreeMap<>();
        TreeMap<String,List<Integer>> byType = corp.docsByType();
        for (String type : byType.keySet()){
            double sum = 0.0;
            int count = 0;
            for (Integer d : byType.get(type)){
                sum = sum + distances[d];
                ++count;
            }
            ret.put(type,sum/count);
        }
        return ret;
    }
    
    public List<double[]> distancesByType(Corpus corp){
        List<double[]> ret = new ArrayList<>();
        
        TreeMap<String,List<Integer>> recs = corp.docsByType();
        for (String type : recs.keySet()){
            List<Integer> list = recs.get(type);
            double[] d = new double[list.size()];
            for (int i=0 ; i<d.length ; ++i){
                d[i] = this.distances[list.get(i)];
            }
            ret.add(d);
        }
        return ret;
    }    
    static public void main(String[] args) throws Exception {
/*        
        double[][] phi1 = UtilsIO.readRealMatrix(new File("/net/waterston/vol2/home/gevirl/ldatest/doublet.phi"));
        
        double[][] phi2 = UtilsIO.readRealMatrix(new File("/net/waterston/vol2/home/gevirl/ldatest/doubletout/topics_55/doublet_topics55_alpha0.010_beta0.010/KDE_mean.phi"));
        PhiComparison phiComp = new PhiComparison(phi1,phi2);

        double[][] theta1 = UtilsIO.readRealMatrix(new File("/net/waterston/vol2/home/gevirl/ldatest/doublet.theta"));
        double[][] theta2 = UtilsIO.readRealMatrix(new File("/net/waterston/vol2/home/gevirl/ldatest/doubletout/topics_55/doublet_topics55_alpha0.010_beta0.010/KDE_mean.theta"));     
        
        double thresh = 0.1;
        ThetaComparison thetaComp = new ThetaComparison(theta1,theta2,phiComp,thresh);
        System.out.printf("eight thread mean = %f\n",thetaComp.getMean());
        
        TopicMatch[] matches = phiComp.getMatches();
        int count = 0;
        for (int i=0 ; i<matches.length ; ++i){
            if (matches[i].jsd <= thresh){
                ++count;
            }
        }
        System.out.printf("Matching phi = %d\n",count);
        thetaComp = new ThetaComparison(theta1,theta2,null,1.0);
        System.out.printf("unajusted mean = %f\n",thetaComp.getMean());
        int ioasdjfiojsd=0;
*/
    }    
}
