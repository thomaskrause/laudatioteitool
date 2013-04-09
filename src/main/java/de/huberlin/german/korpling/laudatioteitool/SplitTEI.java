/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import java.io.File;
import java.util.ResourceBundle;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SplitTEI
{
  private static final ResourceBundle messages =
    ResourceBundle.getBundle("Messages");
  
  private File inputFile;
  private File outputDirectory;
  public SplitTEI(File inputFile, File outputDirectory)
  {
    this.inputFile = inputFile;
    this.outputDirectory = outputDirectory;
  }
  
  public void split() throws LaudatioException
  {
    if(!outputDirectory.isDirectory() && !outputDirectory.mkdirs())
    {
      throw new LaudatioException(messages.getString("COULD NOT CREATE OUTPUT DIRECTORY."));
    }
    
    // check if input file exits
    if(!inputFile.isFile())
    {
      throw new LaudatioException(messages.getString("INPUT FILE DOES NOT EXIST"));
    }
    
    // read in file
    SAXBuilder sax = new SAXBuilder();
    //sax.build(inputFile);
    
    
  }
}
