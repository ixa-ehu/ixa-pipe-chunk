package eus.ixa.ixa.pipe.chunk.train;

import opennlp.tools.chunker.ChunkerModel;
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

}
