/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.views;

import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author gevirl
 */
public class LogLikePanel {
    JFreeChart chart;

    public LogLikePanel(String title,double[] likes) {

        XYSeriesCollection collect = new XYSeriesCollection();
        XYSeries series = new XYSeries("LogLike"); 
       
        for (int t = 0; t < likes.length ;  ++t) {

                series.add(t,likes[t]);

        }      
        collect.addSeries(series);
        chart = ChartFactory.createXYLineChart(title, "Iteration", "-LogLikelihood/10^7", collect, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = chart.getXYPlot();
        NumberAxis axis = (NumberAxis)plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
    }

    public JPanel getPanel() {
        return new ChartPanel(chart);
    }    
}
