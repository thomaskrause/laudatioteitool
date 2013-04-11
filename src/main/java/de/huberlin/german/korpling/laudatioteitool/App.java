/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    ResourceBundle.getBundle("de/huberlin/german/korpling/laudatioteitool/Messages");

  public static void main(String[] args)
  {
    Options opts = new Options()
       .addOption(new Option("merge", true, messages.getString("MERGE CONTENT FROM INPUT DIRECTORY INTO ONE TEI HEADER")))
       .addOption(new Option("split", true, messages.getString("SPLIT ONE TEI HEADER INTO SEVERAL HEADER FILES")))
       .addOption(new Option("help", false, messages.getString("SHOW THIS HELP")));
    
    HelpFormatter fmt = new HelpFormatter();
    String usage = "java -jar teitool.jar [options] [output directory/file]";
    
    try
    {
      CommandLineParser cliParser = new PosixParser();
      
     
      CommandLine cmd = cliParser.parse(opts, args);
      
      if(cmd.hasOption("help"))
      {
        fmt.printHelp(usage, opts);
      }
      else if(cmd.hasOption("merge"))
      {
        if(cmd.getArgs().length != 1)
        {
          System.out.println(messages.getString("YOU NEED TO GIVE AT AN OUTPUT FILE AS ARGUMENT"));
          System.exit(-1);
        }
        MergeTEI merge = new MergeTEI(new File(cmd.getOptionValue("merge")), 
          new File(cmd.getArgs()[0]));
        merge.merge();
        
        System.exit(0);
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
        System.exit(0);
      }
      else
      {
        fmt.printHelp(usage, opts);
      }
      
      
    }
    catch (ParseException ex)
    {
      System.err.println(ex.getMessage());
      fmt.printHelp(usage, opts);
    }
    catch (LaudatioException ex)
    {
      System.err.println(ex.getMessage());
    }
    catch (UnsupportedOperationException ex)
    {
      System.err.println(ex.getMessage());
    }
    
    System.exit(1);
    
  }
}
