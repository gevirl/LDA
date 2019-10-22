/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rhwlab.lda.cache.matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.rhwlab.command.CommandLine;
import org.rhwlab.lda.BagOfWords;
import org.rhwlab.lda.JarFile;
import org.rhwlab.lda.ChibEstimator;
import org.rhwlab.lda.cache.MarginalEmpiric;
import org.rhwlab.lda.cache.MarginalKDE;
import org.rhwlab.lda.cache.PointEstimateDistribution;
import org.rhwlab.lda.cache.TopicHistogramEstimator;
import org.rhwlab.lda.cache.ZDirectory;

/**
 *
 * @author gevirl
 */
public class LDA_CommandLine extends CommandLine {

    // general options
    ArrayList<BagOfWords> bows = new ArrayList<>();

    String rid = null;
    int nThreads = 1;
    int thining = 1;
    long seed = 1000;

    // proccessing directives
    boolean partProcessing = false;
    boolean ldaProcessing = false;
    boolean chibProcessing = false;
    boolean pointEstimateProcessing = false;

    // chib options
    File phiFile;
    int chibIter = 1000;
    int chibBurnin = 200;
    File chibBOW;

    // lda options   
    int topics;
    RowSumMatrix alpha ;
    RowSumMatrix beta ;
    int cacheSize = 10;
    File outDir;
    int ldaIter = 1000;
    //   int ldaBurn = 0;

    // point estimator options
    int skip = 0;
    double precision = 1;
    String dist = "kde";
    String statistic = "mode";
    int verbose = 1;
    File iterationDir;

    // partioning validation parameers
    Integer nPart = null;

    Random rnd;
    static String prog = "org.rhwlab.lda.cache.LDA_CommandLine";

    private ArrayList<String> partitionOptions(File[] bowFiles, String runID) throws Exception {
        ArrayList<String> options = new ArrayList<>();
        BagOfWords[] bowArray = new BagOfWords[bowFiles.length];
        for (int i = 0; i < bowFiles.length; ++i) {
            bowArray[i] = new BagOfWords(bowFiles[i]);
        }

        for (int p = 0; p < nPart; ++p) {
            File partDir = null;
            // build the model
            ArrayList<BagOfWords> ldaBows = new ArrayList<>();
            for (int i = 0; i < nPart; ++i) {
                if (i != p) {
                    ldaBows.add(bowArray[i]);
                } else {
                    partDir = new File(this.outDir, String.format("Partition_%d", p));
                }

            }
            File iterDir = iterationDirectory(partDir);
            String phi = new File(iterDir, String.format("%s_%s.phi", dist.toLowerCase(), statistic.toLowerCase())).getPath();
            options.add(LDAOptions(ldaBows, partDir, runID) + PointEstimatorOptions(iterDir) + ChibOptions(phi, bowArray[p]));
        }

        return options;
    }

    private void outputPartitionCommands(PrintStream stream, File[] bowFiles, String runID) throws Exception {
        for (String options : partitionOptions(bowFiles, runID)) {
            stream.printf("java -cp %s %s ", JarFile.getJar(), prog);
            stream.print(options);
            stream.println();
        }
    }

    private String PointEstimatorOptions(File dir) {
        StringBuilder builder = new StringBuilder();
        if (cacheSize != 0) {
            builder.append("-pe ");
        }
        builder.append(String.format("-d %s ", dist));
        builder.append(String.format("-st %s ", statistic));
        builder.append(String.format("-sk %d ", skip));
        builder.append(String.format("-v %d ", verbose));
        builder.append(String.format("-id %s ", dir.getPath()));
        builder.append(String.format("-pr %f ", precision));
        return builder.toString();
    }

    private String ChibOptions(String phi, BagOfWords bow) {
        StringBuilder builder = new StringBuilder();
        builder.append("-chib ");
        builder.append(String.format("-ic %s ", bow.getFile().getPath()));
        builder.append(String.format("-ip %s ", phi));
        builder.append(String.format("-ci %d ", chibIter));
        builder.append(String.format("-cb %d ", chibBurnin));
        return builder.toString();
    }

    private String LDAOptions(ArrayList<BagOfWords> bowList, File dir, String runID) {
        StringBuilder builder = new StringBuilder();
        builder.append("-lda ");
        builder.append(String.format("-a %f ", alpha));
        builder.append(String.format("-b %f ", beta));
        for (BagOfWords bow : bowList) {
            builder.append(String.format("-ib %s ", bow.getFile().getPath()));
        }
        builder.append(String.format("-li %d ", this.ldaIter));
        builder.append(String.format("-o %s ", dir.getPath()));
        builder.append(String.format("-s %d ", seed));
        builder.append(String.format("-t %d ", topics));
        builder.append(String.format("-th %d ", nThreads));
        builder.append(String.format("-tn %d ", thining));
        builder.append(String.format("-ch %d ", cacheSize));
        builder.append(String.format("-rid %s ", runID));
        return builder.toString();
    }

    public void runPointEstimate() throws Exception {
        if (iterationDir == null) {
            iterationDir = this.outDir;
        }

        MultiThreadXML lda = new MultiThreadXML(iterationDir);
        alpha = lda.getAlpha();
        beta = lda.getBeta();
        int nWorkers = lda.getWorkersSize();
        int nDocs = lda.getDocumentsSize();
        int nVocab = lda.getVocabSize();
        int nTopics = lda.getTopicsSize();

        ZDirectory zDir = new ZDirectory(iterationDir, lda, nThreads);
        zDir.setSkip(skip);
        PointEstimateDistribution estimator = null;
        if (dist.equalsIgnoreCase("kde")) {
            estimator = new MarginalKDE(lda.getDocuments(), nVocab, nTopics, this.precision);
        } else if (dist.equalsIgnoreCase("empiric")) {
            estimator = new MarginalEmpiric();
        } else if (dist.equalsIgnoreCase("topic")) {
            estimator = new TopicHistogramEstimator(lda.getDocuments(), nVocab, nTopics);
        }

        zDir.addTo(estimator, skip);  // put the iterations into the point estimate distribution

        if (estimator != null) {
            if ((verbose & 1) != 0) {
                estimator.statisticReport(iterationDir, statistic.toLowerCase(), skip, alpha, beta, lda.getTotalWords(), lda.docs, lda.getLastIteration(), nTopics, nVocab);
            }
        }

        File textDir = null;
        if ((verbose & 4) != 0) {
            textDir = iterationDir;
        }
        PrintStream stream = null;
        if ((verbose & 2) != 0) {
            stream = new PrintStream(new File(iterationDir, "LogLikelihoods"));

        }
        if (stream != null || textDir != null) {
            zDir.iterationsReport(stream, textDir);
        }
    }

    // run the chib estimator
    private void runChib() throws Exception {
        // read the phi file
        ArrayList<double[]> phiList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(phiFile));
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            double[] topicPhi = new double[tokens.length];
            for (int i = 0; i < topicPhi.length; ++i) {
                topicPhi[i] = Double.valueOf(tokens[i]);
            }
            phiList.add(topicPhi);
            line = reader.readLine();
        }
        reader.close();
        double[][] phi = new double[phiList.size()][];
        for (int i = 0; i < phi.length; ++i) {
            phi[i] = phiList.get(i);
        }

        // get the lda xml
        File dir = phiFile.getParentFile();
        MultiThreadXML xml = new MultiThreadXML(dir);

        // get the documents to validate
        int[][] docs = new BagOfWords(chibBOW).toDocumentFormat();

        Collection<Callable<Object>> workers = new ArrayList<>();
        ChibEstimator[] estimators = new ChibEstimator[nThreads];
        for (int i = 0; i < nThreads; ++i) {
            estimators[i] = new ChibEstimator(phi, xml.getAlpha(), seed + i);
            estimators[i].setIterations(this.chibIter);
            estimators[i].setBurnin(this.chibBurnin);
        }

        PrintStream stream = new PrintStream(phiFile.getPath().replace(".phi", ".chib"));
        ExecutorService service = Executors.newWorkStealingPool(nThreads);
        int start = 0;
        while (start < docs.length) {
            workers.clear();
            int nW = Math.min(nThreads, docs.length - start);
            for (int t = 0; t < nW; ++t) {
                System.out.printf("Chib on document %d\n", start + t);
                estimators[t].setDocument(docs[start + t]);
                workers.add(estimators[t]);
            }
            List<Future<Object>> futures = service.invokeAll(workers);
            for (int t = 0; t < nW; ++t) {
                Future future = futures.get(t);
                double logProb = (Double) future.get();
                double perp = -logProb / docs[start + t].length;
                stream.printf("%f,%f,%d\n", logProb, perp, docs[start + t].length);
            }
            start = start + nW;
        }
        stream.close();
    }

    private File iterationDirectory(File baseDir) {
        String rName;
        if (rid == null) {
            rName = runName(bows.get(0).getFile().getName().replace(".bow", ""));
        } else {
            rName = runName(rid);
        }
        return new File(baseDir, rName);
    }

    private String runName(String baseName) {
        return String.format("%s_topics%d_alpha%.3f_beta%.3f", baseName, topics, alpha, beta);
    }

    // run the lda model on the input bow files and parameters for given number of topics
    private void runLDA() throws Exception {

        iterationDir = iterationDirectory(this.outDir);  // output iterations directory
        Files.createDirectories(iterationDir.toPath());
        File xml = new File(iterationDir, "MultiThreadLDA.xml");

        MultiThreadLDA lda;
        if (xml.exists()) {
            lda = new MultiThreadLDA(iterationDir);  // adding iterations to exiting lda run
        } else {
            int[][] docs = BagOfWords.toDocumentFormat(bows.toArray(new BagOfWords[0]));
            lda = new MultiThreadLDA(docs, bows.get(0).getVocabSize(), topics, alpha, beta, nThreads, cacheSize, seed, dist, statistic, precision);
        }

        lda.setDirectory(iterationDir);
        lda.setIterations(ldaIter);
        lda.setBurnIn(skip);
        lda.setThinning(thining);
        lda.call();
        if (cacheSize == 0) {
            this.pointEstimateProcessing = false;
        }
    }

    @Override
    public String post() {

        rnd = new Random(seed);
        try {
            if (ldaProcessing) {
                if (rid == null) {
                    rid = bows.get(0).getFile().getName().replace(".bow", "");  // base the runid on the first bow file name
                }
                runLDA();
            }
            if (pointEstimateProcessing) {
                runPointEstimate();
            }
            if (chibProcessing) {
                runChib();
            }
            if (partProcessing) {
                String runID = bows.get(0).getFile().getName().replace(".bow", "");
                File[] bowFiles = bows.get(0).partition(nPart, outDir, rnd);  // randomly partition the input bow
                outputPartitionCommands(System.out, bowFiles, runID);
            }
        } catch (Exception exc) {
            StringWriter writer = new StringWriter();
            exc.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        }
        return null;
    }

    public String tn(String s) {
        return thinning(s);
    }

    public String thinning(String s) {
        try {
            thining = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error        
    }

    public String li(String s) {
        return ldaIterations(s);
    }

    public String ldaIterations(String s) {
        try {
            ldaIter = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error          
    }

    public String ci(String s) {
        return chibIterations(s);
    }

    public String chibIterations(String s) {
        try {
            chibIter = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error          
    }

    public String cb(String s) {
        return chibBurn(s);
    }

    public String chibBurn(String s) {
        try {
            chibBurnin = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error          
    }

    public String a(String s) {
        return alpha(s);
    }

    public String alpha(String s) {
        try {
            alpha = Double.parseDouble(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String b(String s) {
        return beta(s);
    }

    public String beta(String s) {
        try {
            beta = Double.parseDouble(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String t(String s) {
        return topic(s);
    }

    public String topic(String s) {
        try {
            topics = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error
    }

    public String th(String s) {
        return threads(s);
    }

    public String threads(String s) {
        try {
            nThreads = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error        
    }

    public String p(String s) {
        return partitions(s);
    }

    public String partitions(String s) {
        try {
            nPart = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error        
    }

    public String o(String s) {
        return out(s);
    }

    public String out(String s) {
        outDir = new File(s);

        try {
            Files.createDirectories(outDir.toPath());
        } catch (Exception exc) {
            return exc.getMessage();
        }

        return null; // no error
    }

    public String id(String s) {
        return iterDir(s);
    }

    public String iterDir(String s) {
        iterationDir = new File(s);
        return null; // no error
    }

    public String ip(String s) {
        return inputPhi(s);
    }

    public String inputPhi(String s) {
        phiFile = new File(s);

        return null; // no error
    }

    public String ib(String s) {
        return inputBOW(s);
    }

    public String inputBOW(String s) {
        try {
            BagOfWords b = new BagOfWords(s);
            int vocab = b.getVocabSize();
            if (!bows.isEmpty()) {
                if (vocab != bows.get(0).getVocabSize()) {
                    return String.format("vocabularly size does not match other BOWS for BOW: %s", s);
                }
            }
            this.bows.add(b);

        } catch (Exception exc) {
            return String.format("Error reading Bag of Words file: %s", exc.getMessage());
        }
        return null;
    }

    public String s(String s) {
        return seed(s);
    }

    public String seed(String s) {
        try {
            seed = Long.parseLong(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error          
    }

    @Override
    public void init() {

    }

    @Override
    public String noOption(String s) {
        return String.format("No option associated with %s", s);
    }

    public void rid(String s) {
        rid = s;
    }

    public void r(String s) {
        rid = s;
    }

    public String ch(String s) {
        return cache(s);
    }

    public String cache(String s) {
        try {
            cacheSize = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            return exc.getMessage();
        }
        return null;  // no error        
    }

    public void part() {
        this.partProcessing = true;
    }

    public void lda() {
        this.ldaProcessing = true;
    }

    public void pe() {
        this.pointEstimateProcessing = true;
    }

    public void chib() {
        this.chibProcessing = true;
    }

    public String d(String str) {
        if (str.equalsIgnoreCase("kde") || str.equalsIgnoreCase("empiric") || str.equalsIgnoreCase("topic") || str.equalsIgnoreCase("none")) {
            dist = str;
            return null;
        }
        return String.format("Unknown type of distribution: %s", str);
    }

    public String dist(String s) {
        return d(s);
    }

    public String st(String str) {
        if (str.equalsIgnoreCase("mean") || str.equalsIgnoreCase("mode")) {
            statistic = str;
            return null;
        }
        return String.format("Unknown statistic: %s", str);
    }

    public String statistic(String str) {
        return st(str);
    }

    public String sk(String s) {
        try {
            skip = Integer.valueOf(s);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

    public String skip(String s) {
        return sk(s);
    }

    public String v(String s) {
        try {
            verbose = Integer.valueOf(s);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

    public String verbose(String s) {
        return v(s);
    }

    public String pr(String s) {
        try {
            precision = Double.valueOf(s);
        } catch (Exception exc) {
            return exc.getMessage();
        }
        return null;
    }

    public String precision(String s) {
        return pr(s);
    }

    public String ic(String s) {
        return inputChibBOW(s);
    }

    public String inputChibBOW(String s) {
        this.chibBOW = new File(s);

        return null; // no error
    }

    @Override
    public void usage() {
        System.out.println("\n\nDescription - Latent Dirichlet Allocation (binary iteration output)");

        System.out.println("\nGeneral Options:");
        System.out.println("\t-r, -rid (string) \n\t\tuser supplied run identification, default based on bow file, topics, alpha, and beta");
        System.out.println("\t-s, -seed (long integer)\n\t\trandom number generator seed, default=1000");
        System.out.println("\t-th, -threads (integer)\n\t\tnumber of threads to use for LDA and Chib, default=1");

        System.out.println("\nProcessing Directives:");
        System.out.println("\t-lda  \n\t\tlda iterations");
        System.out.println("\t-pe  \n\t\tpoint estimate from lda iterations");
        System.out.println("\t-chib  \n\t\tchib estimation");
        System.out.println("\t-part  \n\t\tpartition validation, partitions BOW file and writes commands to stdout");

        System.out.println("\nLDA Options:");
        System.out.println("\t-a, -alpha (float)\n\t\t symmetric Dirichlet parameter for topic distribution, default=0.1");
        System.out.println("\t-b, -beta (float)\n\t\tsymmetric Dirichlet parameter for document distribution, default=0.1");
        System.out.println("\t-ch, -cache (integer)\n\t\tOutput cache size, if cache size = 0 then compute point estimates during lda, default=10");
        System.out.println("\t-ib, -inputBOW (path)\n\t\tinput bag of words file, no default");
        System.out.println("\t-li, -ldaIterations (integer)\n\t\tnumer of lda iterations, default=1000");
        System.out.println("\t-o, -out (path)\n\t\tmain output directory for LDA iterations, no default");
        System.out.println("\t-t, -topic (integer)\n\t\tnumber of topics,  no default");
        System.out.println("\t-tn, -thinning (integer)\n\t\titeration interval between saves, default=1");

        System.out.println("\nPoint Estimator Options:");
        System.out.println("\t-d, -dist (topic/kde/empiric/none)\n\t\ttype of distribution to use for point estimates, default = kde");
        System.out.println("\t-id, -iterDir (path)\n\t\tinput directory of LDA iterations, will use lda output directory if -pe and -lda used together as the default");
        System.out.println("\t-pr, -precision (float)\n\t\tprecision for KDE distribution, default=1");
        System.out.println("\t-st, -statistic (mean/mode)\n\t\tstatistic of the distribution to report, default = mode");
        System.out.println("\t-sk, -skip (integer)\n\t\tnumber of initial records to skip , default = 0");
        System.out.println("\t-v, -verbose (0-7)\n\t\tverbose level of output, default = 1;");

        System.out.println("\nChib Estimation Options:");
        System.out.println("\t-ci, -chibIterations (integer)\n\t\tnumber of Chib validation iterations, default=1000");
        System.out.println("\t-cb, -chibBurn (integer)\n\t\tChib validation burnin, default=200");
        System.out.println("\t-ic, -inputChibBOW (path)\n\t\tinput bag of words file, no default");
        System.out.println("\t-ip, -inputPhi (path)\n\t\tinput phi file, for Chib validation, no default");

        System.out.println("\nPartition Validation Options:");
        System.out.println("Includes all LDA, Point Estimation, and Chib Options:");
        System.out.println("\t-p, -partitions (integer)\n\t\tnumber of partitions of input bow, default=5");

    }

    static public void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime starting = LocalDateTime.now();
        System.out.println(dtf.format(starting));
        LDA_CommandLine lda = new LDA_CommandLine();
        lda.process(args, true);
        LocalDateTime finished = LocalDateTime.now();
        System.out.printf("Starting %s - Finished %s\n",dtf.format(starting),dtf.format(finished));        
    }

}
