/*
 * Copyright 2014 Rodrigo Agerri

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.util.TrainingParameters;

import org.jdom2.JDOMException;

import com.google.common.io.Files;

import eus.ixa.ixa.pipe.chunk.eval.CrossValidator;
import eus.ixa.ixa.pipe.chunk.eval.Evaluate;
import eus.ixa.ixa.pipe.chunk.train.DefaultTrainer;
import eus.ixa.ixa.pipe.chunk.train.Flags;
import eus.ixa.ixa.pipe.chunk.train.InputOutputUtils;
import eus.ixa.ixa.pipe.chunk.train.Trainer;

/**
 * Main class of ixa-pipe-chunk, the chunker of ixa-pipes
 * (ixa2.si.ehu.es/ixa-pipes). The annotate method is the main entry point.
 * 
 * @author ragerri
 * @version 2014-07-08
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
      "ixa-pipe-chunk-" + version + ".jar").description(
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
   * The parser that manages the training sub-command.
   */
  private Subparser trainParser;
  /**
   * The parser that manages the evaluation sub-command.
   */
  private Subparser evalParser;
  /**
   * The parser that manages the cross validation sub-command.
   */
  private final Subparser crossValidateParser;
  /**
   * Default beam size for decoding.
   */
  public static final int DEFAULT_BEAM_SIZE = 3;

  /**
   * Construct a CLI object with the three sub-parsers to manage the command
   * line parameters.
   */
  public CLI() {
    annotateParser = subParsers.addParser("tag").help("Tagging CLI");
    loadAnnotateParameters();
    trainParser = subParsers.addParser("train").help("Training CLI");
    loadTrainingParameters();
    evalParser = subParsers.addParser("eval").help("Evaluation CLI");
    loadEvalParameters();
    this.crossValidateParser = this.subParsers.addParser("cross").help(
        "Cross validation CLI");
    loadCrossValidateParameters();
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
      } else if (args[0].equals("eval")) {
        eval();
      } else if (args[0].equals("train")) {
        train();
      } else if (args[0].equals("cross")) {
        crossValidate();
      }
    } catch (ArgumentParserException e) {
      argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-chunk-" + version
          + ".jar (tag|train|eval|cross) -help for details");
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

    String model;
    if (parsedArguments.get("model") == null) {
      model = "baseline";
    } else {
      model = parsedArguments.getString("model");
    }
    String outputFormat = parsedArguments.get("outputFormat");
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
        System.exit(1);
      }
    } else {
      lang = kaf.getLang();
    }
    final Properties properties = setAnnotateProperties(model, lang);
    final Annotate annotator = new Annotate(properties);
    // annotate to KAF
    if (outputFormat.equalsIgnoreCase("conll")) {
      bwriter.write(annotator.annotateChunksToCoNLL(kaf));
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
    annotateParser
        .addArgument("--conll")
        .action(Arguments.storeFalse())
        .help(
            "Do not print tokens in NAF format, but conll tabulated format.\n");
  }

  /**
   * Main entry point for training.
   * 
   * @throws IOException
   *           throws an exception if errors in the various file inputs.
   */
  public final void train() throws IOException {
    // load training parameters file
    final String paramFile = this.parsedArguments.getString("params");
    final TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    String outModel = null;
    if (params.getSettings().get("OutputModel") == null
        || params.getSettings().get("OutputModel").length() == 0) {
      outModel = Files.getNameWithoutExtension(paramFile) + ".bin";
      params.put("OutputModel", outModel);
    } else {
      outModel = Flags.getModel(params);
    }
    final Trainer chunkerTrainer = new DefaultTrainer(params);
    final ChunkerModel trainedModel = chunkerTrainer.train(params);
    CmdLineUtil.writeModel("ixa-pipe-chunk", new File(outModel), trainedModel);
  }

  /**
   * Loads the parameters for the training CLI.
   */
  private void loadTrainingParameters() {
    this.trainParser.addArgument("-p", "--params").required(true)
        .help("Load the training parameters file\n");
  }

  /**
   * Main entry point for evaluation.
   * 
   * @throws IOException
   *           the io exception thrown if errors with paths are present
   */
  public final void eval() throws IOException {
    String testFile = parsedArguments.getString("testSet");
    String model = parsedArguments.getString("model");

    Evaluate evaluator = new Evaluate(testFile, model);
    if (parsedArguments.getString("evalReport") != null) {
      if (parsedArguments.getString("evalReport").equalsIgnoreCase("brief")) {
        evaluator.evaluate();
      } else if (parsedArguments.getString("evalReport").equalsIgnoreCase(
          "error")) {
        evaluator.evalError();
      } else if (parsedArguments.getString("evalReport").equalsIgnoreCase(
          "detailed")) {
        evaluator.detailEvaluate();
      }
    } else {
      evaluator.detailEvaluate();
    }
  }

  /**
   * Load the evaluation parameters of the CLI.
   */
  public final void loadEvalParameters() {
    evalParser.addArgument("-m", "--model").required(true).help("Choose model");
    evalParser.addArgument("-t", "--testSet").required(true)
        .help("Input testset for evaluation");
    evalParser.addArgument("--evalReport").required(false)
        .choices("brief", "detailed", "error")
        .help("Choose type of evaluation report; defaults to detailed");
  }

  /**
   * Main access to the cross validation.
   * 
   * @throws IOException
   *           input output exception if problems with corpora
   */
  public final void crossValidate() throws IOException {

    final String paramFile = this.parsedArguments.getString("params");
    final TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    final CrossValidator crossValidator = new CrossValidator(params);
    crossValidator.crossValidate(params);
  }

  /**
   * Create the main parameters available for training NERC models.
   */
  private void loadCrossValidateParameters() {
    this.crossValidateParser.addArgument("-p", "--params").required(true)
        .help("Load the Cross validation parameters file\n");
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
