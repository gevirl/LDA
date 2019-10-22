/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import org.rhwlab.lda.cache.LDA_CommandLine;

/**
 *
 * @author gevirl
 */
public class JarFile {

    // returns the file location of the running jar
    static public String getJar(){
        ProtectionDomain domain = LDA_CommandLine.class.getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        URL location = source.getLocation();
        return location.getPath();        
    }

}
