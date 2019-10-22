/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.views;

import java.util.List;
import java.util.TreeMap;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author gevirl
 */
public class HistogramPanel {

    JFreeChart chart;

    public HistogramPanel(String title, List<int[]> topicCounts, int[] topics, int nBins) {
        TreeMap<String, double[]> map = new TreeMap<>();
        for (int topic : topics) {
            double[] data = new double[topicCounts.size()];
            map.put(String.format("Topic%d", topic), data);
            for (int i = 0; i < data.length; ++i) {
                data[i] = topicCounts.get(i)[topic];
            }
        }
        chart = asHistogram(title, "X", map, nBins);
    }

    public JPanel getPanel() {
        return new ChartPanel(chart);
    }

    static JFreeChart asHistogram(String title, String xLabel, TreeMap<String, double[]> map, int nBins) {
        HistogramDataset ds = new HistogramDataset();
        for (String seriesName : map.keySet()) {
            ds.addSeries(seriesName, map.get(seriesName), nBins);
        }

        JFreeChart chart = ChartFactory.createHistogram(title, xLabel, "Count", ds, PlotOrientation.VERTICAL, true, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setForegroundAlpha(0.85f);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        return chart;
    }
}
