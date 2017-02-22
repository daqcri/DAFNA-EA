DAFNA-EA
========

DAFNA-EA is a java library of truth discovery methods from the literature to evaluate the veracity of data claimed by multiple online sources.

The methods that have been implemented for the [comparative study](http://arxiv.org/abs/1409.6428) are the following: 
More detail can be found [here](http://da.qcri.org/dafna/#/dafna/exp_sections/home.html).

- TruthFinder from X. Yin, J. Han and P. S. Yu. In IEEE Trans. Knowl. Data Eng., 20(6):796-808, 2008. 
- Cosine, 2-Estimates and 3-Estimates from A. Galland, S. Abiteboul, A. Marian and P. Senellart In WSDM, PAGES 131-140, 2010.
- SimpleLCA and GuessLCA 	from J. Pasternack and D. Roth In WWW, pages 1009-1020, 2013.
- Depen, Accu, AccuSim and AccuNoDep from X. L. Dong, L. Berti-Equille and D. Srivastava In PVLDB, 2(1):550-561, 2009.
- LTM from B. Zhao, B. I. P. Rubinstein, J. Gemmell and J. Han In PVLDB, 5(6):550-561, 2012.
- and MLE from D. Wang, L. M. Kaplan, H. K. Le and T.F. Abdelzaher In ISPN, pages 233-244, 2012.

Datasets
------------------
Real-world data sets are available [here](http://da.qcri.org/dafna/#/dafna/exp_sections/realworldDS/).

A dataset generator for truth discovery scenario can be donwladed [here](http://da.qcri.org/dafna/exp_sections/realworldDS/synthetic/DAFNA-DataSetGenerator.jar) and description of the parameters are given [here](http://da.qcri.org/dafna/#/dafna/exp_sections/realworldDS/synthetic/syntheticDs.html) with a [full documentation](http://da.qcri.org/dafna/exp_sections/realworldDS/synthetic/generator_doc.pdf). 

Citations
------------------
To cite DAFNA-EA in publications use:
- Dalia Attia Waguih and Laure Berti-Equille: Truth Discovery Algorithms — An Experimental Evaluation. Technical Report QCRI, [arXiv 1409.6428](http://arxiv.org/abs/1409.6428), May 2014.

For LaTeX users:
``` @article{WaguihBertiEquille14,	
author    = {Dalia Attia Waguih and	
Laure Berti{-}Equille},
title     = {Truth Discovery Algorithms: An Experimental Evaluation},
journal   = {CoRR},
volume    = {abs/1409.6428},
year      = {2014},
url       = {http://arxiv.org/abs/1409.6428}}
```

To cite ensembling of truth discovery methods
- Laure Berti-Equille: Data Veracity Estimation with Ensembling Truth discovery Methods. In Big Data (IEEE International Conference on Big Data), Santa Clara, CA USA, pages 2628-2636.

For LaTeX users:
```@inproceedings{Berti-Equille15,
author    = {Laure Berti{-}Equille},
title     = {Data veracity estimation with ensembling truth discovery methods},
booktitle = {2015 {IEEE} International Conference on Big Data, Big Data 2015, Santa Clara, CA, USA, October 29 - November 1, 2015},
pages     = {2628--2636},
year      = {2015}}
```
  
For a survey:
- Laure Berti-Equille and Javier Borge-Holthoefer: Veracity of Big Data — From Truth Discovery Computation Algorithms to Models of Misinformation Dynamics. In Synthesis Lectures on Data Management, December 2015, Vol. 7, No. 3, Pages 1-155, Morgan & Claypool Publishers. [Available here](http://www.morganclaypool.com/doi/abs/10.2200/S00676ED1V01Y201509DTM042)

For LaTeX users:
```@book{DBLP:series/synthesis/2015Berti,
author    = {Laure Berti{-}Equille and Javier Borge{-}Holthoefer},
title     = {Veracity of Data: From Truth Discovery Computation Algorithms to Models of Misinformation Dynamics},
series    = {Synthesis Lectures on Data Management},
publisher = {Morgan {\&} Claypool Publishers},
year      = {2015}}
```
  
Tutorials
------------------
Two tutorials surveying truth discovery methods and the topic of data veracity are available [here](http://da.qcri.org/dafna/tutorial_cikm2015/index.html).
-  Scaling Up Truth Discovery — From Probabilistic Inference to Misinformation Dynamics. In ICDE 2016. [abstract](http://da.qcri.org/dafna/home_sections/tutorial-ICDE16-CRV.pdf)
-  Veracity of Big Data. In CIKM 2015 [slides](http://da.qcri.org/dafna/home_sections/tutorial-CIKM2015.pdf)

API
------------------
We have releasee an API so that users can test the truth discovery methods on their own. Documentation of the API is [here](http://da.qcri.org/dafna/#/dafna/apidoc/gettingstarted.html)

Demos
------------------
You can try the demos:
- AllegatorTrack: [here](http://dafna.qcri.org/allegatortrack) as a guest or [here](http://dafna.qcri.org/users/sign_in) to sign in. AllegatorTrack is a scalable truth discovery system  based on DAFNA-EA to score the veracity of data from multiple structured sources.  
- Vera: [here](http://vera-qcri.herokuapp.com/#/) VERA is a Web-based platform using DAFNA-EA and that supports the full pipeline of truth discovery from Web unstructured corpus and tweets, ranging from information extraction from raw texts and micro-texts and data fusion to truth discovery and visualization (WWW216)

Build Instructions fro DAFNA-EA
------------------
Make sure you have installed [Java 7](http://java.com) and [Maven](http://maven.apache.org) on your computer first.
Before the first build you need to prepare some libraries in your local repository:

    mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=lib/independance-vldb2010-solomon-cleaner.jar \
    -DgroupId=com.att.research -DartifactId=solomon.cleaner -Dversion=0.0.1 -Dpackaging=jar -DlocalRepositoryPath=my-repo

    mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=lib/simmetrics_jar_v1_6_2_d07_02_07.jar \
    -DgroupId=uk.ac.shef.wit -DartifactId=simmetrics -Dversion=1.6.2 -Dpackaging=jar -DlocalRepositoryPath=my-repo
  
### Building for dafna-viz 

To build a jar containing all the algorithms ready to consume for dafna-viz UI and API:

    mvn clean # cleans previously created jar
    mvn package # builds everything
  
or simply

    mvn clean package

This will build a jar located in `target` folder. Just copy it to dafna-viz:

    cp target/DAFNA-EA-1.0-jar-with-dependencies.jar <dafna-viz-root>/vendor

### Building from eclipse

Import the project as a Maven project then build it normally (recommended).
Alternatively, import as Java project and set the `classpath` manually to include all dependancies listed in `pom.xml`.

Command line invocation
-----------------------

    java -jar <JAR_PATH> <ALGORITHM_NAME> <DATASETS_CLAIMS_DIR> <DATASETS_GROUND_DIR> <OUTPUT_DIR> <ALGORITHM_PARAMS>
    
Where `<JAR_PATH>` points to the generated jar file in the build section. `<ALGORITHM_NAME>` stands for algorithm name,
which can be one of the following:
`Cosine`, `2-Estimates`, `3-Estimates`, `Depen`, `Accu`, `AccuSim`, `AccuNoDep`, `TruthFinder`, `SimpleLCA`, `GuessLCA`, `MLE` or `LTM`.
`<DATASETS_CLAIMS_DIR>`, `<DATASETS_GROUND_DIR>`
and `<OUTPUT_DIR>` point to directories where CSV claim, ground files and the directory where all output files
should be generated, respectively.

`<ALGORITHM_PARAMS>` is a white-space separated values and are dependant on the algorithm selected.
In all cases, general parameters come first followed by specific parameters.
Details of parameters for each algorithms can be found [here](https://github.com/daqcri/DAFNA-EA/blob/master/algorithms.js).

There are 3 possible patterns for the `<ALGORITHM_PARAMS>`:

### Normal algorithm

4 General then specific parameters.

### Combiner invocation

4 General parameters are all set to 0, followed by the number of algorithms to be combined. Next comes 
a number of file paths pointing to claim results generated from the corresponding algorithm before calling the combiner.
Example:

    java -jar <JAR_PATH> <ALGORITHM_NAME> <DATASETS_CLAIMS_DIR> <DATASETS_GROUND_DIR> <OUTPUT_DIR> 0 0 0 0 3 results1.csv results2.csv results3.csv

### Allegator invocation

5 extra parameters are added at the end: 

    java -jar <JAR_PATH> <ALGORITHM_NAME> <DATASETS_CLAIMS_DIR> <DATASETS_GROUND_DIR> <OUTPUT_DIR> <ALGORITHM_PARAMS> <RUN_ID> <CLAIM_ID> <CLAIM_RESULTS_FILE> <SOURCE_TRUSTWORTHINESS_FILE> Allegate

Where `<ALGORITHM_PARAMS>` is the same as in normal invocation, `<RUN_ID>` and `<CLAIM_ID>` denote the run id and the claim id being allegated, respectively.
These can be anything and are only used for convenience to generate meaningful file names in the output.
`<CLAIM_RESULTS_FILE>` and `<SOURCE_TRUSTWORTHINESS_FILE>` point to results generatd by the run being allegated.
`Allegate` should be put as is.
