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
