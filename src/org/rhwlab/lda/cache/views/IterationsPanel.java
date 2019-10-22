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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author gevirl
 */
public class IterationsPanel {

    JFreeChart chart;

    public IterationsPanel(String title, List<int[]> topicCounts,int[] topics) {

        XYSeriesCollection collect = new XYSeriesCollection();

        XYSeries[] series = new XYSeries[topics.length];  // a series for each topic
        for (int t = 0; t < series.length; ++t) {
            String seriesLabel = String.format("Topic%d", topics[t]);
            series[t] = new XYSeries(seriesLabel);            
        }

        int i = 0;
        for (int[] counts : topicCounts) {
            for (int t = 0; t < topics.length; ++t) {
                series[t].add(i,counts[topics[t]]);
            }
            ++i;
        }       
        for (int t = 0; t < series.length; ++t) {
            collect.addSeries(series[t]);
        }       
        chart = ChartFactory.createXYLineChart(title, "Iteration", "Count", collect, PlotOrientation.VERTICAL, true, true, false);
    }

    public JPanel getPanel() {
        return new ChartPanel(chart);
    }
}
