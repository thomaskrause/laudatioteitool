/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SplitTEI
{

  private static final ResourceBundle messages =
    ResourceBundle.getBundle("de/huberlin/german/korpling/laudatioteitool/Messages");
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
    if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs())
    {
      throw new LaudatioException(messages.getString(
        "COULD NOT CREATE OUTPUT DIRECTORY."));
    }

    // check if input file exits
    if (!inputFile.isFile())
    {
      throw new LaudatioException(messages.
        getString("INPUT FILE DOES NOT EXIST"));
    }

    // read in file
    SAXBuilder sax = new SAXBuilder();
    try
    {
      Document doc = sax.build(inputFile);
      
      extractMainCorpusHeader(doc);
      extractDocumentHeaders(doc);
      extractPreparationSteps(doc);
      

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

  private void extractMainCorpusHeader(Document doc) throws LaudatioException, IOException
  {

    Element corpusHeader = doc.getRootElement().getChild("teiHeader", null);
    if (corpusHeader != null)
    {
      File corpusDir = new File(outputDirectory, "CorpusHeader");
      if (!corpusDir.exists() && !corpusDir.mkdir())
      {
        throw new LaudatioException(messages.getString(
          "COULD NOT CREATE DIRECTORY")
          + corpusDir.getAbsolutePath());
      }

      // create the subtree for the global corpus header
      Namespace teiNS = Namespace.getNamespace(
        "http://www.tei-c.org/ns/1.0");
      Element newRootForCorpus = new Element("TEI", teiNS);
      newRootForCorpus.addContent(corpusHeader.clone());
      Document corpusDoc = new Document(newRootForCorpus);

      // we need to append an empty "text" element after the header
      Element text = new Element("text", teiNS);
      text.setText("");
      newRootForCorpus.addContent(text);

      // we work with the copy from now
      corpusHeader = newRootForCorpus.getChild("teiHeader", null);
      Validate.notNull(corpusHeader, messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));
      
      Validate.matchesPattern(corpusHeader.getAttributeValue("type"), "CorpusHeader");
      
      Validate.notNull(corpusHeader.getChild("fileDesc", null), messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));
      Validate.notNull(corpusHeader.getChild("fileDesc", null).getChild("titleStmt", null), messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));
      
      String title = corpusHeader.getChild("fileDesc", null)
        .getChild("titleStmt", null)
        .getChildTextNormalize("title", null);
      Validate.notNull(title, messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));

      // save the file with the title as file name
      File outputFile = new File(corpusDir, title + ".xml");
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(corpusDoc, new FileWriter(outputFile));
      log.info(messages.getString("WRITTEN CORPUS HEADER"), outputFile.getPath());

    }
  }
  
  private void extractDocumentHeaders(Document doc) throws LaudatioException, IOException
  {
    File documentDir = new File(outputDirectory, "DocumentHeader");
    if (!documentDir.exists() && !documentDir.mkdir())
    {
      throw new LaudatioException(messages.getString(
        "COULD NOT CREATE DIRECTORY")
        + documentDir.getAbsolutePath());
    }
    
    Element documentRoot = Validate.notNull(doc.getRootElement()
      .getChild("teiCorpus", null));
    
    
    for(Element docHeader : documentRoot.getChildren("teiHeader", null))
    {
      Validate.matchesPattern(docHeader.getAttributeValue("type"), "DocumentHeader");
      
      // create the subtree for the global corpus header
      Namespace teiNS = Namespace.getNamespace(
        "http://www.tei-c.org/ns/1.0");
      Element tei = new Element("TEI", teiNS);
      tei.addContent(docHeader.clone());
      Document newDoc = new Document(tei);
      
      // we need to append an empty "text" element after the header
      Element text = new Element("text", teiNS);
      text.setText("");
      tei.addContent(text);
      
      Element fileDesc = Validate.notNull(tei.getChild("teiHeader", null).getChild("fileDesc", null));
    
      
      String outName = UUID.randomUUID().toString();
      
      String id = fileDesc.getAttributeValue("id", Namespace.XML_NAMESPACE);
      if(id != null)
      {
        outName = id;
      }
      else
      {
        Element titleStmt = Validate.notNull(fileDesc.getChild("titleStmt", null));
        
        String title = titleStmt.getChildText("title", null);
        if(title != null)
        {
          outName = title;
        }
      }
      
      File outputFile = new File(documentDir, outName + ".xml");
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(newDoc, new FileWriter(outputFile));
      log.info(messages.getString("WRITTEN DOCUMENT HEADER"), outputFile.getPath());
      
    }
    
  }
  
  private void extractPreparationSteps(Document doc) throws LaudatioException, IOException
  {
    HashSet<String> knownPreparationTitles = new HashSet<String>();
    
    File documentDir = new File(outputDirectory, "PreparationHeader");
    if (!documentDir.exists() && !documentDir.mkdir())
    {
      throw new LaudatioException(messages.getString(
        "COULD NOT CREATE DIRECTORY")
        + documentDir.getAbsolutePath());
    }
    
    Validate.notNull(doc.getRootElement()
      .getChild("teiCorpus", null));
    Element preparationRoot = Validate.notNull(doc.getRootElement()
      .getChild("teiCorpus", null).getChild("teiCorpus", null));
    
    
    for(Element preparationHeader : preparationRoot.getChildren("teiHeader", null))
    {
      Validate.matchesPattern(preparationHeader.getAttributeValue("type"), "PreparationHeader");
      
      // create the subtree for the global corpus header
      Namespace teiNS = Namespace.getNamespace(
        "http://www.tei-c.org/ns/1.0");
      Element tei = new Element("TEI", teiNS);
      tei.addContent(preparationHeader.clone());
      Document newDoc = new Document(tei);
      
      // we need to append an empty "text" element after the header
      Element text = new Element("text", teiNS);
      text.setText("");
      tei.addContent(text);
      
      Element fileDesc = Validate.notNull(tei.getChild("teiHeader", null).getChild("fileDesc", null));
      
      String outName = UUID.randomUUID().toString();

      Element titleStmt = Validate.notNull(fileDesc.getChild("titleStmt", null));
      Element title = Validate.notNull(titleStmt.getChild("title", null));
      String corresp = title.getAttributeValue("corresp");
      if(corresp != null)
      {
        if(knownPreparationTitles.contains(corresp))
        {
          outName += corresp;
          log.warn(messages.getString("MORE THAN ONE PREPARATION HEADER"), corresp);
        }
        else
        {
          outName = corresp;
          knownPreparationTitles.add(corresp);
        }
      }
      
      File outputFile = new File(documentDir, outName + ".xml");
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(newDoc, new FileWriter(outputFile));
      log.info(messages.getString("WRITTEN PREPARATION HEADER"), outputFile.getPath());
      
    }
    
  }
  
}
