package eus.ixa.ixa.pipe.chunk.train;

import java.io.IOException;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerEvaluator;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

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
   * ChunkerFactory features need to be implemented by any class extending
   * this one.
   */
  private ChunkerFactory chunkerFactory;
  /**
   * Construct an AbstractTrainer. In the params parameter there is information
   * about the language, the featureset, algorithm, and so on.
   * @param params
   *          the training parameters
   * @throws IOException
   *           the io exceptions
   */
  public AbstractTrainer(final TrainingParameters params) throws IOException {
    this.lang = Flags.getLanguage(params);
    final String trainData = Flags.getDataSet("TrainSet", params);
    final String testData = Flags.getDataSet("TestSet", params);
    final ObjectStream<String> trainStream = InputOutputUtils.readFileIntoMarkableStreamFactory(trainData);
    this.trainSamples = new ChunkSampleStream(trainStream);
    final ObjectStream<String> testStream = InputOutputUtils.readFileIntoMarkableStreamFactory(testData);
    this.testSamples = new ChunkSampleStream(testStream);
  }

  /* (non-Javadoc)
 * @see eus.ixa.ixa.pipe.chunk.train.Trainer#train(opennlp.tools.util.TrainingParameters)
 */
public final ChunkerModel train(final TrainingParameters params) {
    // features
    if (getChunkerFactory() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractTrainer must "
              + " create a ChunkerFactory features!");
    }
    // training model
    ChunkerModel trainedModel = null;
    ChunkerEvaluator chunkerEvaluator = null;
    try {
      trainedModel = ChunkerME.train(lang, trainSamples, params,
          getChunkerFactory());
      final Chunker chunker = new ChunkerME(trainedModel);
      chunkerEvaluator = new ChunkerEvaluator(chunker);
      chunkerEvaluator.evaluate(this.testSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final result: " + chunkerEvaluator.getFMeasure());
    return trainedModel;
  }

  /**
   * Get the chunkerFactory. Every extension of this class must provide an
   * implementation of the ChunkerFactory
   * @return the chunkerFactory
   */
  protected final ChunkerFactory getChunkerFactory() {
    return chunkerFactory;
  }

  /**
   * Set/implement the chunkerFactory to be used in the training.
   * @param aChunkerFactory
   *          the chunker factory implemented
   */
  protected final void setChunkerFactory(
      final ChunkerFactory aChunkerFactory) {
    this.chunkerFactory = aChunkerFactory;
  }

}
