/*
 * Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package eus.ixa.ixa.pipe.chunk.eval;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.ChunkerCrossValidator;
import opennlp.tools.chunker.ChunkerEvaluationMonitor;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.cmdline.chunker.ChunkEvaluationErrorListener;
import opennlp.tools.cmdline.chunker.ChunkerDetailedFMeasureListener;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.EvaluationMonitor;
import eus.ixa.ixa.pipe.chunk.train.Flags;
import eus.ixa.ixa.pipe.chunk.train.InputOutputUtils;

/**
 * Training Chunk tagger with Apache OpenNLP Machine Learning API via cross
 * validation.
 * 
 * @author ragerri
 * @version 2014-11-25
 */

public class CrossValidator {

  /**
   * The language.
   */
  private final String lang;
  /**
   * ObjectStream of the training data.
   */
  private final ObjectStream<ChunkSample> trainSamples;
  /**
   * The folds value for cross validation.
   */
  private final int folds;
  /**
   * chunkerFactory features need to be implemented by any class extending
   * this one.
   */
  private ChunkerFactory chunkerFactory;
  /**
   * The evaluation listeners.
   */
  private final List<EvaluationMonitor<ChunkSample>> listeners = new LinkedList<EvaluationMonitor<ChunkSample>>();
  ChunkerDetailedFMeasureListener detailedListener;

  /**
   * Construct a CrossValidator. In the params parameter there is information
   * about the language and the featureset.
   * 
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exceptions
   */
  public CrossValidator(final TrainingParameters params) throws IOException {
    this.lang = Flags.getLanguage(params);
    final String trainData = Flags.getDataSet("TrainSet", params);
    final ObjectStream<String> trainStream = InputOutputUtils
        .readFileIntoMarkableStreamFactory(trainData);
    this.trainSamples = new ChunkSampleStream(trainStream);
    this.folds = Flags.getFolds(params);
    createChunkerFactory(params);
    getEvalListeners(params);
  }

  private void createChunkerFactory(final TrainingParameters params) {
    final String featureSet = Flags.getFeatureSet(params);
    if (featureSet.equalsIgnoreCase("Opennlp")) {
      this.chunkerFactory = new ChunkerFactory();
    } else {
      this.chunkerFactory = new ChunkerFactory();
    }
  }

  private void getEvalListeners(final TrainingParameters params) {
    if (params.getSettings().get("EvaluationType").equalsIgnoreCase("error")) {
      this.listeners.add(new ChunkEvaluationErrorListener());
    }
    if (params.getSettings().get("EvaluationType").equalsIgnoreCase("detailed")) {
      this.detailedListener = new ChunkerDetailedFMeasureListener();
      this.listeners.add(this.detailedListener);
    }
  }

  /**
   * Cross validate when no separate testset is available.
   * 
   * @param params
   *          the training parameters
   */
  public final void crossValidate(final TrainingParameters params) {

    ChunkerCrossValidator validator = null;
    try {
      validator = getChunkerCrossValidator(params);
      validator.evaluate(this.trainSamples, this.folds);
    } catch (final IOException e) {
      System.err.println("IO error while loading training set!");
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        this.trainSamples.close();
      } catch (final IOException e) {
        System.err.println("IO error with the train samples!");
      }
    }
    if (this.detailedListener == null) {
      System.out.println(validator.getFMeasure());
    } else {
      // TODO add detailed evaluation here
      System.out.println(validator.getFMeasure());
    }
  }

  /**
   * Get the chunker cross validator.
   * 
   * @param params
   *          the training parameters
   * @return the chunker tagger cross validator
   */
  private ChunkerCrossValidator getChunkerCrossValidator(
      final TrainingParameters params) {
    // features
    if (this.chunkerFactory == null) {
      throw new IllegalStateException(
          "You must create the ChunkerFactory features!");
    }
    ChunkerCrossValidator validator = new ChunkerCrossValidator(this.lang, params, chunkerFactory,
        this.listeners.toArray(new ChunkerEvaluationMonitor[this.listeners.size()]));
    
    return validator;
  }

}
