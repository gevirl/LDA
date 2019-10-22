/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.evaluation;

import java.util.TreeMap;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

/**
 *
 * @author gevirl
 */
public class Histogram extends ChartFrame {
/*    
    public Histogram(String title,double[] x,int nBins){
        super(title,asHistogram(title,x,nBins));
    }
  */  
    public Histogram(String runTitle,String title,String xLabel,TreeMap<String,double[]> map,int nBins){
        super(runTitle,asHistogram(title,xLabel,map,nBins));
    }
/*    
     static JFreeChart asHistogram(String title,double[] x,int nBins){
        HistogramDataset ds = new HistogramDataset();
        ds.addSeries(title, x, nBins);
        return ChartFactory.createHistogram(title, "X", "Count", ds, PlotOrientation.VERTICAL, false, false, false);
     }
  */   
     static JFreeChart asHistogram(String title,String xLabel,TreeMap<String,double[]> map,int nBins){
        HistogramDataset ds = new HistogramDataset();
        for (String seriesName : map.keySet()){
            ds.addSeries(seriesName, map.get(seriesName), nBins);
        }

        JFreeChart chart =  ChartFactory.createHistogram(title,xLabel, "Count", ds, PlotOrientation.VERTICAL, true, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setForegroundAlpha(0.85f);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);    
        return chart;
     }
}
