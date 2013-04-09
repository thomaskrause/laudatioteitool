/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SplitTEI
{
  private static final ResourceBundle messages =
    ResourceBundle.getBundle("Messages");
  
  private final static Logger log = LoggerFactory.getLogger(SplitTEI.class);
  
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
    try
    {
      Document doc = sax.build(inputFile);
      Element corpusHeader = doc.getRootElement().getChild("teiHeader", null);
      if(corpusHeader != null)
      {
        File corpusDir = new File(outputDirectory, "CorpusHeader");
        if(!corpusDir.exists() && !corpusDir.mkdir())
        {
          throw new LaudatioException(messages.getString("COULD NOT CREATE DIRECTORY") 
            + corpusDir.getAbsolutePath());
        }
        
        // create the subtree for the global corpus header
        Namespace teiNS =  Namespace.getNamespace(
          "http://www.tei-c.org/ns/1.0");
        Element newRootForCorpus = new Element("TEI",teiNS);
        newRootForCorpus.addContent(corpusHeader.clone());
        Document corpusDoc = new Document(newRootForCorpus);
        
        // we need to append an empty "text" element after the header
        Element text = new Element("text", teiNS);
        text.setText("");
        newRootForCorpus.addContent(text);
        
        // we work with the copy from now
        corpusHeader = newRootForCorpus.getChild("teiHeader", null);
        if(corpusHeader.getChild("fileDesc", null) != null 
          && corpusHeader.getChild("fileDesc", null).getChild("titleStmt", null) != null
          && corpusHeader.getChild("fileDesc", null)
            .getChild("titleStmt", null)
            .getChildTextNormalize("title", null) != null)
        {
          String title =  corpusHeader.getChild("fileDesc", null)
            .getChild("titleStmt", null)
            .getChildTextNormalize("title", null);
          
          // save the file with the title as file name
          File outputFile = new File(corpusDir, title + ".xml");
          XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
          xmlOut.output(corpusDoc, new FileWriter(outputFile));
          log.info("Written corpus header {}", outputFile.getAbsolutePath());
        }
        else
        {
          throw new LaudatioException(messages.getString("ERROR NO CORPUS TITLE GIVEN"));
        }
      }
      
    }
    catch (JDOMException ex)
    {
      throw new LaudatioException(ex.getLocalizedMessage());
    }
    catch (IOException ex)
    {
      throw new LaudatioException(ex.getLocalizedMessage());
    }
    
    
  }
}
