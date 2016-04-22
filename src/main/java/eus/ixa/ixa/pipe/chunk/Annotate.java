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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import eus.ixa.ixa.pipe.ml.StatisticalSequenceLabeler;
import eus.ixa.ixa.pipe.ml.utils.Span;

/**
 * Annotation class. Use this as example for using ixa-pipe-ml API for chunking.
 * 
 * @author ragerri
 * @version 2016-04-22
 */
public class Annotate {

  /**
   * The sequence labeler.
   */
  private StatisticalSequenceLabeler chunker;

  /**
   * Annotate constructor.
   * 
   * @param properties
   *          the properties
   * @throws IOException
   *           if io problems
   */
  public Annotate(Properties properties) throws IOException {
    chunker = new StatisticalSequenceLabeler(properties);
  }

  /**
   * Produce chunks into NAF document.
   * 
   * @param kaf
   *          the naf document
   * @throws IOException
   *           if io problems
   */
  public void chunkToKAF(KAFDocument kaf) throws IOException {
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      /* Get an array of token forms from a list of WF objects. */
      String[] posTags = new String[sentence.size()];
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
        List<Term> terms = kaf.getTermsBySent(sentence.get(i).getSent());
        posTags[i] = terms.get(i).getMorphofeat();
      }
      Span[] chunks = chunker.seqToSpans(tokens);
      for (int i = 0; i < chunks.length; i++) {
        String type = chunks[i].getType();
        Integer startIndex = chunks[i].getStart();
        Integer endIndex = chunks[i].getEnd();
        // TODO use new functions and proper heads
        List<Term> chunkTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays
            .copyOfRange(tokenIds, startIndex, endIndex)));
        kaf.createChunk(chunkTerms.get(chunkTerms.size() - 1), type, chunkTerms);
      }
    }
  }

  /**
   * Produce chunks into CoNLL 2000 format.
   * 
   * @param kaf
   *          the NAF document
   * @return the chunks in conll00 format
   * @throws IOException
   *           if io problems
   */
  public String annotateChunksToCoNLL00(KAFDocument kaf) throws IOException {
    StringBuilder sb = new StringBuilder();
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      /* Get an array of token forms from a list of WF objects. */
      String[] posTags = new String[sentence.size()];
      String[] tokens = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        List<Term> terms = kaf.getTermsBySent(sentence.get(i).getSent());
        posTags[i] = terms.get(i).getMorphofeat();
      }
      String[] chunks = chunker.seqToStrings(tokens);
      for (int i = 0; i < chunks.length; i++) {
        sb.append(tokens[i]).append("\t").append(posTags[i]).append("\t")
            .append(chunks[i]).append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
