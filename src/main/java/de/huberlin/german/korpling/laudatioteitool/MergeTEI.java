/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import java.io.File;
import java.util.ResourceBundle;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class MergeTEI
{
  private static final ResourceBundle messages =
    ResourceBundle.getBundle("Messages");
  
  private File inputDir;
  private File outputFile;
  
  public MergeTEI(File inputDir, File outputFile)
  {
    this.inputDir = inputDir; 
    this.outputFile = outputFile;
  }
  
  public void merge() throws LaudatioException
  {
    if(outputFile.mkdirs())
    {
      // check if each file exits
      throw new UnsupportedOperationException(messages.getString("MERGING NOT IMPLEMENTED YET"));
    }
    else
    {
      throw new LaudatioException(messages.getString("COULD NOT CREATE MERGED OUTPUTFILE: I CAN'T CREATE THE DIRECOTRIES"));
    }
  }
}
