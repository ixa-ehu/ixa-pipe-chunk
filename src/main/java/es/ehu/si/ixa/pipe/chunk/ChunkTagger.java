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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Span;

public class ChunkTagger {

  private ChunkerME chunkerTagger;
  private static ConcurrentHashMap<String, ChunkerModel> chunkerModels = new ConcurrentHashMap<String, ChunkerModel>();
  private String lang;
  
  /**
   * Construct a chunktagger.
   * @param aLang the language
   * @param model the model
   */
  public ChunkTagger(final String aLang, final String model) {
    this.lang = aLang;
    ChunkerModel posModel = loadModel(model);
    chunkerTagger = new ChunkerME(posModel);
  }

  /**
   * Load model statically only if a model for the specified language is not already there.
   * @param model the model type
   * @return the model
   */
  private ChunkerModel loadModel(final String model) {
    InputStream trainedModelInputStream = null;
    try {
      if (!chunkerModels.containsKey(lang)) {
        if (model.equalsIgnoreCase("baseline")) {
          trainedModelInputStream = getBaselineModelStream(model);
        } else {
          trainedModelInputStream = new FileInputStream(model);
        }
        chunkerModels.put(lang, new ChunkerModel(trainedModelInputStream));
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
    return chunkerModels.get(lang);
  }

  /**
   * Back-off to baseline models for a language.
   * @param model the model type
   * @return the back-off model
   */
  private InputStream getBaselineModelStream(final String model) {
    InputStream trainedModelInputStream = null;
    if (lang.equalsIgnoreCase("en")) {
      trainedModelInputStream = getClass().getResourceAsStream(
          "/en/en-chunk-perceptron-c0-b3-dev.bin");
    }
    if (lang.equalsIgnoreCase("es")) {
      trainedModelInputStream = getClass().getResourceAsStream(
          "/en/en-chunk-perceptron-c0-b3.bin");
    }
    return trainedModelInputStream;
  }


 

  public String[] chunkToString(String[] tokens, String[] posTags) { 
    String[] chunks = chunkerTagger.chunk(tokens, posTags);
    return chunks;
  }
  
   public Span[] chunk(String[] tokens, String[] posTags) {
    Span[] chunks = chunkerTagger.chunkAsSpans(tokens, posTags);
    return chunks;
  }

}