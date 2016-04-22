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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import org.jdom2.JDOMException;

import com.google.common.io.Files;

/**
 * Main class of ixa-pipe-chunk which uses ixa-pipe-ml API for chunking. The
 * annotate method is the main entry point.
 * 
 * @author ragerri
 * @version 2016-04-22
 */
public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-chunk by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage()
      .getImplementationVersion();
  /**
   * Get the git commit of the ixa-pipe-pos compiled by looking at the MANIFEST
   * file.
   */
  private final String commit = CLI.class.getPackage()
      .getSpecificationVersion();
  /**
   * The CLI arguments.
   */
  private Namespace parsedArguments = null;
  /**
   * The argument parser.
   */
  private ArgumentParser argParser = ArgumentParsers.newArgumentParser(
      "ixa-pipe-chunk-" + version + "-exec.jar").description(
      "ixa-pipe-chunk-" + version
          + " is a multilingual chunker developed by IXA NLP Group.\n");
  /**
   * Sub parser instance.
   */
  private Subparsers subParsers = argParser.addSubparsers().help(
      "sub-command help");
  /**
   * The parser that manages the tagging sub-command.
   */
  private Subparser annotateParser;

  /**
   * Construct a CLI object with the three sub-parsers to manage the command
   * line parameters.
   */
  public CLI() {
    annotateParser = subParsers.addParser("tag").help("Tagging CLI");
    loadAnnotateParameters();
  }

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   * @throws IOException
   *           the input output exception if not file is available
   * @throws JDOMException
   *           as the input is a NAF file, a JDOMException could be thrown
   */
  public static void main(final String[] args) throws IOException,
      JDOMException {

    CLI cmdLine = new CLI();
    cmdLine.parseCLI(args);
  }

  /**
   * Parse the command interface parameters with the argParser.
   * 
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException
   *           exception if problems with the incoming data
   * @throws JDOMException
   *           if xml exception
   */
  public final void parseCLI(final String[] args) throws IOException,
      JDOMException {
    try {
      parsedArguments = argParser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
      if (args[0].equals("tag")) {
        annotate(System.in, System.out);
      }
    } catch (ArgumentParserException e) {
      argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-chunk-" + version
          + "-exec.jar (tag|server|client) -help for details");
      System.exit(1);
    }
  }

  /**
   * Main entry point for annotation. Takes System.in as input and outputs
   * annotated text via System.out.
   * 
   * @param inputStream
   *          the input stream
   * @param outputStream
   *          the output stream
   * @throws IOException
   *           the exception if not input is provided
   * @throws JDOMException
   *           if xml exception
   */
  public final void annotate(final InputStream inputStream,
      final OutputStream outputStream) throws IOException, JDOMException {

    final String model = parsedArguments.getString("model");
    final String outputFormat = parsedArguments.get("outputFormat");
    BufferedReader breader = null;
    BufferedWriter bwriter = null;
    breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));

    final KAFDocument kaf = KAFDocument.createFromStream(breader);
    // language
    String lang;
    if (this.parsedArguments.getString("language") != null) {
      lang = this.parsedArguments.getString("language");
      if (!kaf.getLang().equalsIgnoreCase(lang)) {
        System.err.println("Language parameter in NAF and CLI do not match!!");
      }
    } else {
      lang = kaf.getLang();
    }
    final Properties properties = setAnnotateProperties(model, lang);
    final Annotate annotator = new Annotate(properties);
    // annotate to KAF
    if (outputFormat.equalsIgnoreCase("conll00")) {
      bwriter.write(annotator.annotateChunksToCoNLL00(kaf));
    } else {
      KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
          "terms", "ixa-pipe-chunk-" + Files.getNameWithoutExtension(model)
              + this.version + "-" + this.commit);
      newLp.setBeginTimestamp();
      annotator.chunkToKAF(kaf);
      newLp.setEndTimestamp();
      bwriter.write(kaf.toString());
    }
    bwriter.close();
    breader.close();
  }

  /**
   * Generate the annotation parameter of the CLI.
   */
  private void loadAnnotateParameters() {
    annotateParser.addArgument("-m", "--model").required(true)
        .help("Choose model to perform chunk tagging.");
    annotateParser.addArgument("-l", "--lang").choices("en").required(false)
        .help("Choose a language to perform annotation with ixa-pipe-chunk.");
    annotateParser.addArgument("-o", "--outputFormat").required(false)
        .choices("naf", "conll00").setDefault("naf")
        .help("Choose between NAF and conll format; it defaults to NAF.\n");
  }

  /**
   * Generate Properties objects for CLI usage.
   * 
   * @param model
   *          the model to perform the annotation
   * @param language
   *          the language
   * @return a properties object
   */
  private Properties setAnnotateProperties(final String model,
      final String language) {
    final Properties annotateProperties = new Properties();
    annotateProperties.setProperty("model", model);
    annotateProperties.setProperty("language", language);
    return annotateProperties;
  }
}
