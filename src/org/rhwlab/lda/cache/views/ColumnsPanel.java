/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.views;

import java.io.File;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author gevirl
 */
public class ColumnsPanel extends JScrollPane {

    public void setColumns(String prefix, File[] topicFiles, int[] columns, boolean agg, double[] likes) throws Exception {
        int[] alltopics = new int[topicFiles.length];
        for (int i = 0; i < alltopics.length; ++i) {
            alltopics[i] = i;
        }
        int[] singleTopic = new int[1];
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        List<int[]>[] counts = new IterationCounts(topicFiles, columns).getCounts();
        for (int c = 0; c < counts.length; ++c) {
            if (agg) {
                panel.add(new IterationsPanel(String.format("%s%d", prefix, columns[c]), counts[c], alltopics).getPanel());

            } else {

                for (int topic : alltopics) {
                    JPanel both = new JPanel();
                    both.setLayout(new BoxLayout(both, BoxLayout.X_AXIS));
                    singleTopic[0] = topic;
                    both.add(new IterationsPanel(String.format("%s%d", prefix, columns[c]), counts[c], singleTopic).getPanel());
                    both.add(new HistogramPanel(String.format("%s%d", prefix, columns[c]), counts[c], singleTopic, 100).getPanel());
                    panel.add(both);
                }

            }
        }
        if (likes != null) {
            panel.add(new LogLikePanel("LogLikelihood", likes).getPanel());
        }
        this.setViewportView(panel);
    }
}
