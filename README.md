
IXA-pipe-chunk
============

ixa-pipe-chunk provides Chunking for English and Spanish.
This module is part of IXA-Pipeline ("is a pipeline"), a multilingual NLP pipeline
developed by the IXA NLP Group (ixa.si.ehu.es).

- Chunking models have been trained using the Apache OpenNLP API:

    + English perceptron models have been trained and evaluated using the CoNLL 2000 datasets.
      Currently we obtain a performance of 92.97% vs 94.13 of the best system in that task
      http://www.clips.ua.ac.be/conll2000/chunking/

    + Spanish Maximum Entropy models have been trained and evaluated using the Ancora corpus; it was randomly
      divided in 90% for training (440K words) and 10% testing (70K words), obtaining a performance of X%.

Contents
========

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories

INSTALLATION
============

Installing the ixa-pipe-chunk requires the following steps:

If you already have installed in your machine JDK7 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.7
-------------------

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java7
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java17
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.7

2. Install MAVEN 3
------------------

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/ragerri/local/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.0.5
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK 6 that is using.

3. Get module source code
--------------------------

````shell
git clone git@github.com:ixa-ehu/ixa-pipe-chunk.git
````

4. Download models and other resources
--------------------------------------

The Chunker needs the trained models. Download the models
and untar the archive into the src/main/resources directory:

````shell
cd ixa-pipe-chunk/src/main/resources
wget http://ixa2.si.ehu.es/ragerri/ixa-pipeline-models/chunk-resources.tgz
tar xvzf chunk-resources.tgz
````
If you change the name of the models you will need to modify also the source code in Models.java.

5. Move into main directory
---------------------------

````shell
cd ixa-pipe-chunk
````

6. Install module using maven
-----------------------------

````shell
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-chunk-1.0.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.6 installed.

To install the module in the local maven repository, usually located at ~/.m2/, execute:

````shell
mvn clean install
````

7. USING ixa-pipe-chunk
=====================

The program accepts tokenized and POS-tagged text in KAF format as standard input and outputs KAF.

https://github.com/opener-project/kaf/wiki/KAF-structure-overview

You can get the required input by piping ixa-pipe-tok and ixa-pipe-pos before. To run the program execute:

````shell
cat input.kaf | java -jar $PATH/target/ixa-pipe-chunk-1.0.jar
````

See

````shell
java -jar $PATH/target/ixa-pipe-chunk-1.0.jar -help
````

for options running the module.


GENERATING JAVADOC
==================

You can also generate the javadoc of the module by executing:

````shell
mvn javadoc:jar
````

Which will create a jar file target/ixa-pipe-chunk-1.0-javadoc.jar


Contact information
===================

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.es
````
