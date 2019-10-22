/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.evaluation;

/**
 *
 * @author gevirl
 */
public class TopicMatch {
    int topic;
    double jsd;
    public TopicMatch(int topic,double jsDistance){
        this.topic = topic;
        this.jsd = jsDistance;
    }
}
