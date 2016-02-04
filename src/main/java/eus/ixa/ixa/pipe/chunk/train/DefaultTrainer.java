package eus.ixa.ixa.pipe.chunk.train;

import java.io.IOException;

import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.util.TrainingParameters;

/**
 * Default OpenNLP feature training, kept for upstream compatibility.
 *
 * @author ragerri
 * @version 2014-07-08
 */
public class DefaultTrainer extends AbstractTrainer {

  public DefaultTrainer(final TrainingParameters params) throws IOException {
    super(params);
    setChunkerFactory(new ChunkerFactory());
  }

}
