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

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ResourceBundle;
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
  
  private String corpusSchemeURL, documentSchemeURL, preparationSchemeURL;

  public MergeTEI(File inputDir, File outputFile, 
    String corpusSchemeURL, String documentSchemeURL, String preparationSchemeURL)
  {
    this.inputDir = inputDir;
    this.outputFile = outputFile;
    this.corpusSchemeURL = corpusSchemeURL;
    this.documentSchemeURL = documentSchemeURL;
    this.preparationSchemeURL = preparationSchemeURL;
  }

  public void merge() throws LaudatioException
  {
    try
    {
      if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists() 
        && !outputFile.getParentFile().
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
      documentRoot.
        addContent(documentRoot.getChildren().size(), preparationRoot);


      Document mergedDoc = new Document(root);

      // output the new XML
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(mergedDoc, new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
      log.info(messages.getString("WRITTEN MERGED TEI"), outputFile.getPath());
    }
    catch (SAXException ex)
    {
      throw new LaudatioException(ex.getLocalizedMessage());
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

  private void mergeMainCorpusHeader(Element root) throws SAXException,
    IOException, LaudatioException, JDOMException
  {
    // append global header

    File corpusHeaderDir = new File(inputDir, "CorpusHeader");
    Preconditions.checkArgument(corpusHeaderDir.isDirectory());
    File[] corpusHeaderFiles = corpusHeaderDir.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });
    Preconditions.checkArgument(corpusHeaderFiles.length > 0);

    File headerFile = corpusHeaderFiles[0];
    TEIValidator validator = 
      corpusSchemeURL == null ? new TEICorpusValidator() : new FromURLValidator(corpusSchemeURL);
    if (validator.validate(headerFile))
    {
      SAXBuilder sax = new SAXBuilder();
      Document corpusDoc = sax.build(headerFile);
      // remove the pending text element
      corpusDoc.getRootElement().removeChild("text", null);

      // append to our new root
      root.addContent(corpusDoc.getRootElement().getChild("teiHeader", null).
        clone());
    }
    else
    {
      System.err.println(validator.toString());
      throw new LaudatioException("Corpus header is not valid");
    }

  }

  private void mergeDocumentHeader(Element root) throws SAXException,
    JDOMException, IOException, LaudatioException
  {
    // append document headers

    File documentHeaderDir = new File(inputDir, "DocumentHeader");
    Preconditions.checkArgument(documentHeaderDir.isDirectory());
    File[] documentHeaderFiles = documentHeaderDir.listFiles(
      new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });
    Preconditions.checkArgument(documentHeaderFiles.length > 0);
    SAXBuilder sax = new SAXBuilder();
    TEIValidator validator =
      documentSchemeURL == null ? new TEIDocumentValidator(): new FromURLValidator(documentSchemeURL);
    
    for (File f : documentHeaderFiles)
    {
      
      if (validator.validate(f))
      {
        Document documentDoc = sax.build(f);

        // remove the pending text element
        documentDoc.getRootElement().removeChild("text", null);

        // append to our new root
        root.addContent(
          documentDoc.getRootElement().getChild("teiHeader", null).
          clone());
      }
      else
      {
        System.err.println(validator.toString());
        throw new LaudatioException("A document header is not valid");
      }
    }
  }

  private void mergePreparationHeader(Element root) throws SAXException,
    JDOMException, IOException, LaudatioException
  {
    // append preparation headers

    File preparationHeaderDir = new File(inputDir, "PreparationHeader");
    Preconditions.checkState(preparationHeaderDir.isDirectory());
    File[] preparationHeaderFiles = preparationHeaderDir.listFiles(
      new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });
    Preconditions.checkState(preparationHeaderFiles.length > 0);
    SAXBuilder sax = new SAXBuilder();
    TEIValidator validator = 
      preparationSchemeURL == null ? new TEIPreparationValidator(): new FromURLValidator(preparationSchemeURL);
      
    for (File f : preparationHeaderFiles)
    {
      
      if(validator.validate(f))
      {
        Document preparation = sax.build(f);

        // remove the pending text element
        preparation.getRootElement().removeChild("text", null);

        // append to our new root
        root.addContent(preparation.getRootElement().getChild("teiHeader", null).
          clone());
      }
      else
      {
        System.err.println(validator.toString());
        throw new LaudatioException("A preparation header ist not valid.");
      }
    }
  }
}
