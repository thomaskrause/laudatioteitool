/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ResourceBundle;
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
import org.xml.sax.SAXException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class MergeTEI
{

  private static final ResourceBundle messages =
    ResourceBundle.getBundle(
    "de/huberlin/german/korpling/laudatioteitool/Messages");
  private final static Logger log = LoggerFactory.getLogger(MergeTEI.class);
  private File inputDir;
  private File outputFile;
  private TEIValidator validator;

  public MergeTEI(File inputDir, File outputFile)
  {
    this.inputDir = inputDir;
    this.outputFile = outputFile;
    this.validator = new TEIValidator();
  }

  public void merge() throws LaudatioException
  {
    try
    {
      if (outputFile.getParentFile() != null && !outputFile.getParentFile().
        mkdirs())
      {
        throw new LaudatioException(messages.getString(
          "COULD NOT CREATE MERGED OUTPUTFILE: I CAN'T CREATE THE DIRECTORIES"));
      }

      Namespace teiNS = Namespace.getNamespace("http://www.tei-c.org/ns/1.0");
      Element root = new Element("teiCorpus", teiNS);      
      Element documentRoot = new Element("teiCorpus", teiNS);
      Element preparationRoot = new Element("teiCorpus", teiNS);
      
      mergeMainCorpusHeader(root);
      mergeDocumentHeader(documentRoot);
      mergePreparationHeader(preparationRoot);

      root.addContent(documentRoot);
      documentRoot.addContent(documentRoot.getChildren().size(), preparationRoot);
      
      
      Document mergedDoc = new Document(root);

      // output the new XML
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(mergedDoc, new FileWriter(outputFile));
      log.info(messages.getString("WRITTEN MERGED TEI"), outputFile.getPath());
    }
    catch (SAXException ex)
    {
      throw new LaudatioException(TEIValidator.getSAXParserError(ex));
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

  private void mergeMainCorpusHeader(Element root) throws SAXException, JDOMException, IOException
  {
    // append global header
    
    File corpusHeaderDir = new File(inputDir, "CorpusHeader");
    Validate.isTrue(corpusHeaderDir.isDirectory());
    File[] corpusHeaderFiles = corpusHeaderDir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });
    Validate.isTrue(corpusHeaderFiles.length > 0);
    SAXBuilder sax = new SAXBuilder();
    Document corpusDoc = sax.build(corpusHeaderFiles[0]);

    validator.validateCorpus(corpusDoc);


    // remove the pending text element
    corpusDoc.getRootElement().removeChild("text", null);

    // append to our new root
    root.addContent(corpusDoc.getRootElement().getChild("teiHeader", null).
      clone());

  }
  
  private void mergeDocumentHeader(Element root) throws SAXException, JDOMException, IOException
  {
    // append document headers
    
    File documentHeaderDir = new File(inputDir, "DocumentHeader");
    Validate.isTrue(documentHeaderDir.isDirectory());
    File[] documentHeaderFiles = documentHeaderDir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });
    Validate.isTrue(documentHeaderFiles.length > 0);
    SAXBuilder sax = new SAXBuilder();
    
    for(File f : documentHeaderFiles)
    {
      Document documentDoc = sax.build(f);

      validator.validateDocument(documentDoc);

      // remove the pending text element
      documentDoc.getRootElement().removeChild("text", null);

      // append to our new root
      root.addContent(documentDoc.getRootElement().getChild("teiHeader", null).
        clone());
    }
  }
  
  private void mergePreparationHeader(Element root) throws SAXException, JDOMException, IOException
  {
    // append preparation headers
    
    File preparationHeaderDir = new File(inputDir, "PreparationHeader");
    Validate.isTrue(preparationHeaderDir.isDirectory());
    File[] preparationHeaderFiles = preparationHeaderDir.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });
    Validate.isTrue(preparationHeaderFiles.length > 0);
    SAXBuilder sax = new SAXBuilder();
    
    for(File f : preparationHeaderFiles)
    {
      Document preparation = sax.build(f);

      validator.validatePreparation(preparation);

      // remove the pending text element
      preparation.getRootElement().removeChild("text", null);

      // append to our new root
      root.addContent(preparation.getRootElement().getChild("teiHeader", null).
        clone());
    }
  }
}
