DAFNA-EA
========

Build Instructions
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
Details of parameters for each algorithms can be found [here](https://github.com/Qatar-Computing-Research-Institute/dafna-viz/blob/master/app/assets/javascripts/main/algorithms.js).

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
