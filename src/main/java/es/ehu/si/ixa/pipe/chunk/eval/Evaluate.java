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

package es.ehu.si.ixa.pipe.chunk.eval;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.ChunkerEvaluationMonitor;
import opennlp.tools.chunker.ChunkerEvaluator;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.chunker.ChunkEvaluationErrorListener;
import opennlp.tools.cmdline.chunker.ChunkerDetailedFMeasureListener;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.EvaluationMonitor;
import es.ehu.si.ixa.pipe.chunk.train.InputOutputUtils;

/**
 * Evaluation class mostly inspired by {@link ChunkerEvaluator}.
 *
 * @author ragerri
 * @version 2014-07-08
 */
public class Evaluate {

  /**
   * The reference corpus to evaluate against.
   */
  private ObjectStream<ChunkSample> testSamples;
  /**
   * Static instance of {@link TokenNameFinderModel}.
   */
  private static ChunkerModel chunkerModel;
  /**
   * An instance of the probabilistic {@link POSTaggerME}.
   */
  private ChunkerME chunkerTagger;

  /**
   * Construct an evaluator. The features are encoded in the model itself.
   *
   * @param testData
   *          the reference data to evaluate against
   * @param model
   *          the model to be evaluated
   * @param beamsize
   *          the beam size for decoding
   * @throws IOException
   *           if input data not available
   */
  public Evaluate(final String testData, final String model)
      throws IOException {

    ObjectStream<String> testStream = InputOutputUtils.readInputData(testData);
    testSamples = new ChunkSampleStream(testStream);
    InputStream trainedModelInputStream = null;
    try {
      if (chunkerModel == null) {
        trainedModelInputStream = new FileInputStream(model);
        chunkerModel = new ChunkerModel(trainedModelInputStream);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
          System.err.println("Could not load model!");
        }
      }
    }
    chunkerTagger = new ChunkerME(chunkerModel);
  }

  /**
   * Evaluate and print precision, recall and F measure.
   *
   * @throws IOException
   *           if test corpus not loaded
   */
  public final void evaluate() throws IOException {
    ChunkerEvaluator evaluator = new ChunkerEvaluator(chunkerTagger);
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }

  /**
   * Detail evaluation of a model, outputting the report a file.
   *
   * @param outputFile
   *          the file to output the report.
   * @throws IOException
   *           the io exception if not output file provided
   */
  public final void detailEvaluate() throws IOException {
    List<EvaluationMonitor<ChunkSample>> listeners = new LinkedList<EvaluationMonitor<ChunkSample>>();
    ChunkerDetailedFMeasureListener detailedFListener = new ChunkerDetailedFMeasureListener();
    listeners.add(detailedFListener);
    ChunkerEvaluator evaluator = new ChunkerEvaluator(chunkerTagger,
        listeners.toArray(new ChunkerEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(detailedFListener.toString());
  }

  /**
   * Evaluate and print every error.
   *
   * @throws IOException
   *           if test corpus not loaded
   */
  public final void evalError() throws IOException {
    List<EvaluationMonitor<ChunkSample>> listeners = new LinkedList<EvaluationMonitor<ChunkSample>>();
    listeners.add(new ChunkEvaluationErrorListener());
    ChunkerEvaluator evaluator = new ChunkerEvaluator(chunkerTagger,
        listeners.toArray(new ChunkerEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }

}
