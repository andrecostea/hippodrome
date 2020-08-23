package org.racerdfix

import org.racerdfix.inferAPI.InterpretJson
import org.racerdfix.language.PrettyPrinting
import org.racerdfix.utils.FileManipulation

class Config(val infer: String,
             val infer_opt: Seq[String],
             val json_path: String,
             val infer_target_files: Seq[String],
             val prio_files: List[String],
             val iterations: Int
            )
case class RunConfig(fixConfig: FixConfig, fileName: String)

case class FixConfig(
                      // Modus Operandi params
                      interactive: Boolean      = false,
                      testing:     Boolean      = false,
                      intellij:    Boolean      = false,
                      log:         Boolean      = true,
                      iterations:  Int          = Globals.no_iter,
                      // Files
                      json_path: String         = Globals.results_out_dir,
                      json_bugs:   String       = Globals.json_bugs_file,
                      json_summaries: String    = Globals.json_summ_path,
                      json_patches: String      = Globals.json_patches,
                      java_sources_path: String = Globals.def_src_path,
                      log_file: String          = Globals.log_file,
                      // Infer
                      config_file: String       = Globals.config_file,
                      infer: String             = Globals.def_infer,
                      infer_opt: Seq[String]    = Globals.def_infer_options,
                      infer_target_files: Seq[String]  = Globals.def_target_files,
                      prio_files: List[String]  = Nil  // checks only the bugs in prio_files if the list in non-empty

  ) extends PrettyPrinting {

  val fm = new FileManipulation

  override def pp: String =
    ( (List(s"interactive = $interactive"))
      ++ (List(s"testing = $testing"))
      ).mkString(", ")

  def getJsonBugsResults = {
    if (json_patches != Globals.json_patches) json_patches
    else fm.getPath(json_path, Globals.json_patches_filename)
  }

  def getJsonBugs = {
    if (json_bugs != Globals.json_bugs_file) json_bugs
    else fm.getFile(json_path, Globals.json_bugs_filename)
  }

  def getJsonSummariesPath = {
    if (json_summaries != Globals.json_summ_path) json_summaries
    else fm.getPath(json_path,Globals.json_summaries_dir)
  }

}

object ArgParser {
  def argsParser =  new {

  } with scopt.OptionParser[RunConfig](Globals.SCRIPTNAME) {
    opt[String]("fileName").action {(x, c) =>
      c.copy(fileName = x)
    }.text("a synthesis file name (the file under the specified folder, called filename.syn)")

    opt[Boolean]('i', "interactive").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(interactive = b))
    }.text("runs RacerDFix in interactive mode - the user is expected to choose a patch")

    opt[Boolean]("testing").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(testing = b))
    }.text("runs RacerDFix in testing mode - generated fixes do not overwrite the original file")

    opt[Boolean]( "intellij").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(intellij = b))
    }.text("runs RacerDFix in IntelliJ mode - runs infer only once")

    opt[Int]("interations").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(iterations = b))
    }.text("the number of times racerdfix attempts to create patches until all the bugs are solved. The default value is " + Globals.no_iter)

    opt[Boolean]('l', "log").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(log = b))
    }.text("logs all the applied patches")

    opt[String]("json_bugs").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(json_bugs = b))
    }.text("sets the file which provides the bug details. The default one is " + Globals.json_bugs_file)

    opt[String]("json_summaries").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(json_summaries = b))
    }.text("sets the file which provides the methods' summaries. The default one is " + Globals.json_summ_path)

    opt[String]("json_patches").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(json_patches = b))
    }.text("sets the file where the generated patches will be stored. The default one is " + Globals.json_patches)

    opt[String]("log_file").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(json_patches = b))
    }.text("sets the logging. The default one is " + Globals.log_file)

    opt[String]('j', "java_sources_path").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(java_sources_path = b))
    }.text("the path to the source files. The default one is " + Globals.def_src_path)

    opt[String]("infer").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(infer = b))
    }.text("the infer process to be called for generating json and validating. The default one is " + Globals.def_infer)

    opt[Seq[String]]("infer_opt").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(infer_opt = b))
    }.text("the options infer runs with. The default one is " + Globals.def_infer_options)

    opt[Seq[String]]('f', "infer_target_files").action { (b, rc) =>
      rc.copy(fixConfig = rc.fixConfig.copy(infer_target_files = b))
    }.text("the target files to analyse and fix. The default one is " + Globals.def_target_files)

    opt[String]('c', "config_file").action { (b, rc) =>
      rc.copy(fixConfig = {
        val jsonTranslator = new InterpretJson(rc.fixConfig.copy(config_file = b))
        val infer_config = jsonTranslator.getJsonConfig()
        rc.fixConfig.copy(config_file = b,
          infer = infer_config.infer,
          infer_opt = infer_config.infer_opt,
          infer_target_files = infer_config.infer_target_files,
          json_path = infer_config.json_path,
          prio_files = infer_config.prio_files,
          iterations = infer_config.iterations
        )})
    }.text("the config file to setup infers. The default one is " + Globals.config_file)

    help("help").text("prints this usage text")
  }
}

case class RacerDFixException(msg: String) extends Exception(msg)

