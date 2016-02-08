/*Copyright 2013 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.chunk;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Span;

public class ChunkTagger {

  private ChunkerME chunkerTagger;
  private static ConcurrentHashMap<String, ChunkerModel> chunkerModels = new ConcurrentHashMap<String, ChunkerModel>();

  /**
   * Construct a chunk tagger.
   * 
   * @param properties the language and model
   */
  public ChunkTagger(Properties properties) {
    String lang = properties.getProperty("language");
    final String model = properties.getProperty("model");
    ChunkerModel posModel = loadModel(lang, model);
    chunkerTagger = new ChunkerME(posModel);
  }

  /**
   * Loads statically the probabilistic model. Every instance of this finder
   * will share the same model.
   * 
   * @param lang
   *          the language
   * @param model
   *          the model to be loaded
   * @return the model as a {@link ChunkerModel} object
   */
  private ChunkerModel loadModel(final String lang, final String model) {
    final long lStartTime = new Date().getTime();
    try {
      synchronized (chunkerModels) {
        if (!chunkerModels.containsKey(lang)) {
          chunkerModels.put(lang, new ChunkerModel(new FileInputStream(model)));
        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    final long lEndTime = new Date().getTime();
    final long difference = lEndTime - lStartTime;
    System.err.println("ixa-pipe-chunk model loaded in: " + difference
        + " miliseconds ... [DONE]");
    return chunkerModels.get(lang);
  }

  /**
   * Get chunks into an String array.
   * 
   * @param tokens
   *          the tokens
   * @param posTags
   *          the pos tags
   * @return the array containing the chunks
   */
  public String[] chunkToString(String[] tokens, String[] posTags) {
    String[] chunks = chunkerTagger.chunk(tokens, posTags);
    return chunks;
  }

  /**
   * Get chunks into an Span array.
   * 
   * @param tokens
   *          the tokens
   * @param posTags
   *          the pos tags
   * @return the chunk spans
   */
  public Span[] chunk(String[] tokens, String[] posTags) {
    Span[] chunks = chunkerTagger.chunkAsSpans(tokens, posTags);
    return chunks;
  }

}