package es.ehu.si.ixa.pipe.chunk.train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerEvaluator;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

import org.apache.commons.io.FileUtils;

/**
 * Training Chunk taggers with Apache OpenNLP Machine Learning API.
 * @author ragerri
 * @version 2014-07-07
 */

public abstract class AbstractTrainer implements Trainer {

  /**
   * The language.
   */
  private String lang;
  /**
   * ObjectStream of the training data.
   */
  private ObjectStream<ChunkSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  private ObjectStream<ChunkSample> testSamples;
  /**
   * beamsize value needs to be established in any class extending this one.
   */
  private int beamSize;
  /**
   * posTaggerFactory features need to be implemented by any class extending
   * this one.
   */
  private ChunkerFactory chunkerFactory;

  /**
   * Construct an AbstractTrainer.
   * @param alang
   *          the language
   * @param aTrainData
   *          the training data
   * @param aTestData
   *          the test data
   * @param aDictPath
   *          the tag dictionary path
   * @param aDictCutOff
   *          the cutoff to automatically build a tag dictionary
   * @param aBeamsize
   *          the beamsize for decoding
   * @throws IOException
   *           the io exceptions
   */
  public AbstractTrainer(final String alang, final String aTrainData,
      final String aTestData, final String aDictPath, final int aBeamsize) throws IOException {
    this.lang = alang;
    ObjectStream<String> trainStream = InputOutputUtils
        .readInputData(aTrainData);
    trainSamples = new ChunkSampleStream(trainStream);
    ObjectStream<String> testStream = InputOutputUtils.readInputData(aTestData);
    testSamples = new ChunkSampleStream(testStream);
    this.beamSize = aBeamsize;

  }

  /*
   * (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.pos.train.Trainer#train(opennlp.tools.util.
   * TrainingParameters)
   */
  public final ChunkerModel train(final TrainingParameters params) {
    // features
    if (getChunkerFactory() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractTrainer must "
              + " create a POSTaggerFactory features!");
    }
    // training model
    ChunkerModel trainedModel = null;
    ChunkerEvaluator chunkerEvaluator = null;
    try {
      trainedModel = ChunkerME.train(lang, trainSamples, params,
          getChunkerFactory());
      chunkerEvaluator = evaluate(trainedModel, testSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final result: " + chunkerEvaluator.getFMeasure());
    return trainedModel;
  }

  /*
   * (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.pos.train.Trainer#trainCrossEval(java.lang.String,
   * java.lang.String, opennlp.tools.util.TrainingParameters,
   * java.lang.String[])
   */
  public final ChunkerModel trainCrossEval(final String trainData,
      final String devData, final TrainingParameters params,
      final String[] evalRange) {

    // get best parameters from cross evaluation
    List<Integer> bestParams = null;
    try {
      bestParams = crossEval(trainData, devData, params, evalRange);
    } catch (IOException e) {
      System.err.println("IO error while loading training and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    TrainingParameters crossEvalParams = new TrainingParameters();
    crossEvalParams.put(TrainingParameters.ALGORITHM_PARAM, params.algorithm());
    crossEvalParams.put(TrainingParameters.ITERATIONS_PARAM,
        Integer.toString(bestParams.get(0)));
    crossEvalParams.put(TrainingParameters.CUTOFF_PARAM,
        Integer.toString(bestParams.get(1)));

    // use best parameters to train model
    ChunkerModel trainedModel = train(crossEvalParams);
    return trainedModel;
  }

  /**
   * Cross evaluation for Maxent training to obtain the best iteration.
   * @param trainData
   *          the training data
   * @param devData
   *          the development data
   * @param params
   *          the parameters file
   * @param evalRange
   *          the range to perform cross evaluation
   * @return the best parameters
   * @throws IOException
   *           io exception if data not available
   */
  private List<Integer> crossEval(final String trainData, final String devData,
      final TrainingParameters params, final String[] evalRange)
      throws IOException {

    // cross-evaluation
    System.out.println("Cross Evaluation:");
    // lists to store best parameters
    List<List<Integer>> allParams = new ArrayList<List<Integer>>();
    List<Integer> finalParams = new ArrayList<Integer>();

    // F:<iterations,cutoff> Map
    Map<List<Integer>, Double> results = new LinkedHashMap<List<Integer>, Double>();
    // maximum iterations and cutoff
    Integer cutoffParam = Integer.valueOf(params.getSettings().get(
        TrainingParameters.CUTOFF_PARAM));
    List<Integer> cutoffList = new ArrayList<Integer>(Collections.nCopies(
        cutoffParam, 0));
    Integer iterParam = Integer.valueOf(params.getSettings().get(
        TrainingParameters.ITERATIONS_PARAM));
    List<Integer> iterList = new ArrayList<Integer>(Collections.nCopies(
        iterParam, 0));
    for (int c = 0; c < cutoffList.size() + 1; c++) {
      int start = Integer.valueOf(evalRange[0]);
      int iterRange = Integer.valueOf(evalRange[1]);

      for (int i = start + start; i < iterList.size() + start; i += iterRange) {
        // reading data for training and test
        ObjectStream<String> trainStream = InputOutputUtils
            .readInputData(trainData);
        ObjectStream<String> devStream = InputOutputUtils
            .readInputData(devData);
        ObjectStream<ChunkSample> aTrainSamples = new ChunkSampleStream(
            trainStream);
        ObjectStream<ChunkSample> devSamples = new ChunkSampleStream(devStream);
        ObjectStream<String> dictStream = InputOutputUtils
            .readInputData(trainData);
        ObjectStream<ChunkSample> aDictSamples = new ChunkSampleStream(
            dictStream);
        // dynamic creation of parameters
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(i));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(c));
        System.out.println("Trying with " + i + " iterations...");

        // training model
        ChunkerModel trainedModel = ChunkerME.train(lang, aTrainSamples, params,
            getChunkerFactory());
        // evaluate model
        ChunkerEvaluator posTaggerEvaluator = this.evaluate(trainedModel,
            devSamples);
        // TODO calculate sentence accuracy
        double result = posTaggerEvaluator.getFMeasure().getFMeasure();
        StringBuilder sb = new StringBuilder();
        sb.append("Iterations: ").append(i).append(" cutoff: ").append(c)
            .append(" ").append("Accuracy: ").append(result).append("\n");
        FileUtils.write(new File("pos-results.txt"), sb.toString(), true);
        List<Integer> bestParams = new ArrayList<Integer>();
        bestParams.add(i);
        bestParams.add(c);
        results.put(bestParams, result);
        System.out.println();
        System.out.println("Iterations: " + i + " cutoff: " + c);
        System.out.println(posTaggerEvaluator.getFMeasure());
      }
    }
    // print F1 results by iteration
    System.out.println();
    InputOutputUtils.printIterationResults(results);
    InputOutputUtils.getBestIterations(results, allParams);
    finalParams = allParams.get(0);
    System.out.println("Final Params " + finalParams.get(0) + " "
        + finalParams.get(1));
    return finalParams;
  }

  /*
   * (non-Javadoc)
   * @see
   * es.ehu.si.ixa.pipe.pos.train.Trainer#evaluate(opennlp.tools.postag.POSModel
   * , opennlp.tools.util.ObjectStream)
   */
  public final ChunkerEvaluator evaluate(final ChunkerModel trainedModel,
      final ObjectStream<ChunkSample> aTestSamples) {
    Chunker posTagger = new ChunkerME(trainedModel);
    ChunkerEvaluator posTaggerEvaluator = new ChunkerEvaluator(posTagger);
    try {
      posTaggerEvaluator.evaluate(aTestSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading test set for evaluation!");
      e.printStackTrace();
      System.exit(1);
    }
    return posTaggerEvaluator;
  }

  /**
   * Get the posTaggerFactory. Every extension of this class must provide an
   * implementation of the posTaggerFactory.
   * @return the posTaggerFactory
   */
  protected final ChunkerFactory getChunkerFactory() {
    return chunkerFactory;
  }

  /**
   * Set/implement the posTaggerFactory to be used in the pos tagger training.
   * @param aPosTaggerFactory
   *          the pos tagger factory implemented
   */
  protected final void setChunkerFactory(
      final ChunkerFactory aPosTaggerFactory) {
    this.chunkerFactory = aPosTaggerFactory;
  }

}
