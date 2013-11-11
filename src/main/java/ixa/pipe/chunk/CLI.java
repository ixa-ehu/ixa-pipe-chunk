/*
 * Copyright 2013 Rodrigo Agerri

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

package ixa.pipe.chunk;

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import net.didion.jwnl.JWNLException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.jdom2.JDOMException;

/**
 * ixa-pipe-chunk provides chunking with models trained using Apache OpenNLP Machine Learning API. 
 * @author ragerri
 * @version 1.0
 *
 */

public class CLI {

  /**
   *
   *
   * BufferedReader (from standard input) and BufferedWriter are opened. The
   * module takes KAF and reads the header, text and terms elements and uses
   * Annotate class to annotate Chunk tags. The <chunk> elements
   * with chunk annotation are then added to the KAF received via standard input.
   * Finally, the modified KAF document is passed via standard output.
   *
   * @param args
   * @throws IOException
   * @throws JDOMException
   * @throws JWNLException
   */

  public static void main(String[] args) throws IOException, JDOMException {

    Namespace parsedArguments = null;

    // create Argument Parser
    ArgumentParser parser = ArgumentParsers
        .newArgumentParser("ixa-pipe-chunk-1.0.jar")
        .description(
            "ixa-pipe-chunk-1.0 is a multiligual Chunker module developed by IXA NLP Group.\n");
    // specify language
    parser
        .addArgument("-l", "--lang")
        .choices("en", "es")
        .required(false)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-chunk");
    
    parser.addArgument("-k","--nokaf").action(Arguments.storeFalse()).help("Do not print parse in KAF format, but plain text.");
    parser.addArgument("-o", "--outputFormat").choices("conll", "pretty")
            .setDefault("pretty")
            .required(false)
            .help("Choose between CoNLL style or pretty output; this option only works if '--nokaf' is also on.");


    /*
     * Parse the command line arguments
     */

    // catch errors and print help
    try {
      parsedArguments = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar target/ixa-pipe-chunk-1.0.jar -help for details");
      System.exit(1);
    }

    /*
     * Load language parameters and construct annotators, read
     * and write kaf
     */
    
    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    try {
      breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));

      // read KAF from standard input
      KAFDocument kaf = KAFDocument.createFromStream(breader);

      // language parameter
      String lang;
      if (parsedArguments.get("lang") == null) {
	  lang = kaf.getLang();
      }
      else {
	  lang =  parsedArguments.getString("lang");
      }
      // add already contained header plus this module linguistic
      // processor
      kaf.addLinguisticProcessor("chunks","ixa-pipe-chunk-"+lang,"1.0");
      Annotate annotator = new Annotate(lang);

      // annotate
      String text = null;
      if (parsedArguments.getBoolean("nokaf")) {
        text = annotator.chunkToKAF(kaf);
      }
      else { 
        if (parsedArguments.getString("outputFormat").equalsIgnoreCase("conll")) { 
          text = annotator.annotateChunksToCoNLL(kaf);
        }
        else { 
          text = annotator.annotateChunks(kaf);
        }
      }
      bwriter.write(text);
      bwriter.close();
      breader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
