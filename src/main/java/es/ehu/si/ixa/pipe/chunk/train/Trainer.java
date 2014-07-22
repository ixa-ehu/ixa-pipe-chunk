package es.ehu.si.ixa.pipe.chunk.train;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkerEvaluator;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * Interface for chunker trainers.
 * @author ragerri
 * @version 2014-07-08
 */
public interface Trainer {

  /**
   * Train a chunker model with a parameters file.
   * @param params
   *          the parameters file
   * @return the {@code ChunkerModel} trained
   */
  ChunkerModel train(TrainingParameters params);

  /**
   * Training via cross evaluation.
   * @param trainData
   *          the training data
   * @param devData
   *          the development data
   * @param params
   *          the parameters file
   * @param evalRange
   *          the range at which to perform each evaluation
   * @return the {@code Model} trained
   */
  ChunkerModel trainCrossEval(String trainData, String devData,
      TrainingParameters params, String[] evalRange);

  /**
   * Evaluate the trained model.
   * @param trainedModel
   *          the {@code POSModel} to evaluate
   * @param testSamples
   *          the test set
   * @return the accuracy of the model
   */
  ChunkerEvaluator evaluate(ChunkerModel trainedModel,
      ObjectStream<ChunkSample> testSamples);

}
