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

package es.ehu.si.ixa.pipe.chunk;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Span;

/**
 * Chunk Class based on Apache OpenNLP API
 * 
 * Models trained by IXA NLP Group.
 * 
 * @author ragerri 2013/11/30
 * 
 */

public class Chunk {

  private ChunkerModel chunkModel;
  private ChunkerME chunker;

  /**
   * It constructs an object Chunk from the Chunk class. First it loads a model,
   * then it initializes the ChunkerModel and finally it creates a chunker
   * using such model.
   */
  public Chunk(InputStream trainedModel) {

    try {
      chunkModel = new ChunkerModel(trainedModel);

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }
    chunker = new ChunkerME(chunkModel);
  }

  public String[] chunkToString(String[] tokens, String[] posTags) { 
    String[] chunks = chunker.chunk(tokens, posTags);
    return chunks;
  }
  
   public Span[] chunk(String[] tokens, String[] posTags) {
    Span[] chunks = chunker.chunkAsSpans(tokens, posTags);
    return chunks;
  }

}