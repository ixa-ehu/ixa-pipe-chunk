
ixa-pipe-chunk
============

ixa-pipe-chunk is a chunker currently offering pre-trained models for English. ixa-pipe-chunk is part of IXA pipes, a multilingual set of NLP tools developed by the IXA NLP Group [http://ixa2.si.ehu.es/ixa-pipes]. **Current version is 1.1.1**.

Please go to [http://ixa2.si.ehu.es/ixa-pipes] for general information about the IXA
pipes tools but also for **official releases, including source code and binary
packages for all the tools in the IXA pipes toolkit**.

This document is intended to be the **usage guide of ixa-pipe-chunk**. If you really need to clone
and install this repository instead of using the releases provided in
[http://ixa2.si.ehu.es/ixa-pipes], please scroll down to the end of the document for
the [installation instructions](#installation).

## TABLE OF CONTENTS

1. [Overview of ixa-pipe-chunk](#overview)
  + [Distributed models](#models)
2. [Usage of ixa-pipe-chunk](#usage)
  + [Training your own models](#training)
  + [Evaluation](#evaluation)
3. [API via Maven Dependency](#api)
4. [Git installation](#installation)

## OVERVIEW

ixa-pipe-chunk provides Perceptron models (Collins 2002) for chunking. To avoid duplication of efforts, we use and contribute to the machine learning API provided by the [Apache OpenNLP project](http://opennlp.apache.org).

**ixa-pipe-chunk is distributed under Apache License version 2.0 (see LICENSE.txt for details)**.

### Models

+ Latest model: [chunk-models-1.1.0](http://ixa2.si.ehu.es/ixa-pipes/models/chunk-models-1.1.0.tar.gz).

+ **English Chunk Models**:
  + CoNLL 2000 data: **en-perceptron-conll00.bin**: 92.96

## USAGE

ixa-pipe-chunk provides 4 basic functionalities:

1. **tag**: reads a NAF document containing *wf* elements and creates *term* elements with the morphological information.
2. **train**: trains new models for with several options
   available (read trainParams.properties file for details).
3. **eval**: evaluates a trained model with a given test set.
4. **cross**: perform cross-validation evaluation.
5. **server**: server mode.
6. **client**: client mode.

Each of these functionalities are accessible by adding (tag|train|eval|cross|server|client) as a
subcommand to ixa-pipe-chunk-$version.jar. Please read below and check the -help
parameter:

````shell
java -jar target/ixa-pipe-chunk-$version.jar (tag|train|eval|cross) -help
````

### Tagging

If you are in hurry, just execute:

````shell
cat file.txt | ixa-pipe-tok | ixa-pipe-pos | java -jar $PATH/target/ixa-pipe-chunk-$version-exec.jar tag -m model.bin
````

If you want to know more, please follow reading.

ixa-pipe-chunk reads NAF documents containing *wf* elements via standard input and outputs NAF
through standard output. The NAF format specification is here:

(http://wordpress.let.vupr.nl/naf/)

You can get the necessary input for ixa-pipe-pos by piping it with
[ixa-pipe-tok](https://github.com/ixa-ehu/ixa-pipe-tok) and [ixa-pipe-pos](https://github.com/ixa-ehu/ixa-pipe-pos)

There are several options to tag with ixa-pipe-chunk:

+ **model**: it is **required** to provide the model to do the tagging.
+ **lang**: choose between en and eu. If no language is chosen, the one specified
  in the NAF header will be used.

**Tagging Example**:

````shell
cat file.txt | ixa-pipe-tok | ixa-pipe-pos | java -jar $PATH/target/ixa-pipe-chunk-$version-exec.jar tag -m $model.bin
````

### Training

To train a new model, you just need to pass a training parameters file as an
argument. Every training option is documented in the template trainParams.properties file.

**Example**:

````shell
java -jar target/ixa.pipe.chunk-$version.jar train -p trainParams.properties
````

### Evaluation

To evaluate a trained model, the eval subcommand provides the following
options:

+ **model**: input the name of the model to evaluate.
+ **testSet**: testset to evaluate the model.
+ **evalReport**: choose the detail in displaying the results:
  + **brief**: it just prints the word accuracy.
  + **detailed**: detailed report with confusion matrixes and so on.
  + **error**: print to stderr all the false positives.

**Example**:

````shell
java -jar target/ixa.pipe.chunk-$version-exec.jar eval -m test-chunk.bin -l en -t test.data
````

## API

The easiest way to use ixa-pipe-chunk programatically is via Apache Maven. Add
this dependency to your pom.xml:

````shell
<dependency>
    <groupId>eus.ixa</groupId>
    <artifactId>ixa-pipe-chunk</artifactId>
    <version>1.1.1</version>
</dependency>
````

## JAVADOC

The javadoc of the module is located here:

````shell
ixa-pipe-chunk/target/ixa-pipe-chunk-$version-javadoc.jar
````
## Module contents

The contents of the module are the following:

    + formatter.xml           Apache OpenNLP code formatter for Eclipse SDK
    + pom.xml                 maven pom file which deals with everything related to compilation and execution of the module
    + src/                    java source code of the module and required resources
    + trainParams.properties      A template properties file containing documention
    + Furthermore, the installation process, as described in the README.md, will generate another directory:
    target/                 it contains binary executable and other directories


## INSTALLATION

Installing the ixa-pipe-chunk requires the following steps:

If you already have installed in your machine the Java 1.7+ and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

### 1. Install JDK 1.7 or JDK 1.8

If you do not install JDK 1.7+ in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java8
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java8
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your JDK is 1.7+

### 2. Install MAVEN 3

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

You should see reference to the MAVEN version you have just installed plus the JDK 7 that is using.

### 3. Get module source code

If you must get the module source code from here do this:

````shell
git clone https://github.com/ixa-ehu/ixa-pipe-chunk
````

### 4. Download the models

Download the models:

````shell
wget http://ixa2.si.ehu.es/ixa-pipes/models/chunk-models.tar.gz
tar xvzf chunk-models.tar.gz
````

### 5. Compile

````shell
cd ixa-pipe-chunk
mvn clean package
````

This step will create a directory called target/ which contains various directories and files.
Most importantly, there you will find the module executable:

ixa-pipe-chunk-$version-exec.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 or newer installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
mvn clean install
````

## Contact information

````shell
Rodrigo Agerri
IXA NLP Group
University of the Basque Country (UPV/EHU)
E-20018 Donostia-San Sebastián
rodrigo.agerri@ehu.eus
````
