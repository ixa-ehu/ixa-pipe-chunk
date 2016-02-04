/*
 * Copyright 2016 Rodrigo Agerri

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

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.util.Span;

/**
 * @author ragerri
 *
 */
public class Annotate {

  private ChunkTagger chunker;
  private String lang;


  public Annotate(String aLang, String model) throws IOException {
    if (model.equalsIgnoreCase("baseline")) {
      System.err.println("Backing-off to default model!");
    }
    this.lang = aLang;
    chunker = new ChunkTagger(lang, model);
  }

  public String chunkToKAF(KAFDocument kaf) throws IOException {
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      List<Term> terms = kaf.getTermsBySent(kaf.getSentence());
      /* Get an array of token forms from a list of WF objects. */
      String posTags[] = new String[terms.size()];
      String tokens[] = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
        posTags[i] = terms.get(i).getMorphofeat();
      }
      Span[] chunks = chunker.chunk(tokens, posTags);
      for (int i = 0; i < chunks.length; i++) {
        String type = chunks[i].getType();
        Integer start_index = chunks[i].getStart();
        Integer end_index = chunks[i].getEnd();
        // TODO use new functions and proper heads
        List<Term> chunkTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays.copyOfRange(tokenIds, start_index, end_index)));
        kaf.createChunk(chunkTerms.get(chunkTerms.size()-1), type, chunkTerms);        
      }
    }
    return kaf.toString();
  }
  
  private List<ChunkSample> getChunks(KAFDocument kaf)
              throws IOException {
    List<ChunkSample> chunkList = new ArrayList<ChunkSample>();
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      List<Term> terms = kaf.getSentenceTerms(kaf.getSentence());
      /* Get an array of token forms from a list of WF objects. */
      String posTags[] = new String[terms.size()];
      String tokens[] = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        posTags[i] = terms.get(i).getMorphofeat();
      }
      String[] chunks = chunker.chunkToString(tokens, posTags);
      ChunkSample chunkSample = new ChunkSample(tokens,posTags,chunks);
      chunkList.add(chunkSample);
    }
    return chunkList;
  }
  
  public String annotateChunks(KAFDocument kaf) throws IOException {
    List<ChunkSample> chunkList = getChunks(kaf);
    StringBuilder sb = new StringBuilder();
    for (ChunkSample chunkSample : chunkList) { 
      String text = chunkSample.nicePrint();
      sb.append(text).append("\n");
    }    
    return sb.toString();
  }

  public String annotateChunksToCoNLL(KAFDocument kaf) throws IOException {
    List<ChunkSample> chunkList = getChunks(kaf);
    StringBuilder sb = new StringBuilder();
    for (ChunkSample chunkSample : chunkList) { 
      sb.append(chunkSample).append("\n");
    }    
    return sb.toString();
  }
}
