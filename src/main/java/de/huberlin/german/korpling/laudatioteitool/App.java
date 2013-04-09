package de.huberlin.german.korpling.laudatioteitool;

import java.io.File;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Hello world!
 *
 */
public class App
{
  private static final ResourceBundle messages =
    ResourceBundle.getBundle("Messages");

  public static void main(String[] args)
  {
    Options opts = new Options()
       .addOption(new Option("merge", true, messages.getString("MERGE CONTENT FROM INPUT DIRECTORY INTO ONE TEI HEADER")))
       .addOption(new Option("split", true, messages.getString("SPLIT ONE TEI HEADER INTO SEVERAL HEADER FILES")));
    
    HelpFormatter fmt = new HelpFormatter();
    
    try
    {
      CommandLineParser cliParser = new PosixParser();
      
     
      CommandLine cmd = cliParser.parse(opts, args);
      
      if(cmd.hasOption("merge"))
      {
        if(cmd.getArgs().length != 1)
        {
          System.out.println(messages.getString("YOU NEED TO GIVE AT AN OUTPUT FILE AS ARGUMENT"));
          System.exit(-1);
        }
        MergeTEI merge = new MergeTEI(new File(cmd.getOptionValue("merge")), 
          new File(cmd.getArgs()[0]));
        merge.merge();
      }
      else if(cmd.hasOption("split"))
      {
        if(cmd.getArgs().length != 1)
        {
          System.out.println(messages.getString("YOU NEED TO GIVE AT AN OUTPUT DIRECTORY AS ARGUMENT"));
          System.exit(-1);
        }
        SplitTEI split = new SplitTEI(new File(cmd.getOptionValue("split")), 
          new File(cmd.getArgs()[0]));
        split.split();
      }
      
      System.exit(0);
      
    }
    catch (ParseException ex)
    {
      System.err.println(ex.getMessage());
    }
    catch (LaudatioException ex)
    {
      System.err.println(ex.getMessage());
    }
    fmt.printHelp("java -jar teitool.jar [options] [output directory/file]", opts);
    
    System.exit(1);
    
  }
}
