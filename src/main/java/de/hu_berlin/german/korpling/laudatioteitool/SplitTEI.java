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
package de.hu_berlin.german.korpling.laudatioteitool;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ResourceBundle;
import java.util.UUID;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
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
public class SplitTEI
{

  private static final ResourceBundle messages =
    ResourceBundle.getBundle("de/huberlin/german/korpling/laudatioteitool/Messages");
  private final static Logger log = LoggerFactory.getLogger(SplitTEI.class);
  
  private File inputFile;
  private File outputDirectory;
  private String corpusSchemeURL, documentSchemeURL, preparationSchemeURL;
  
  public SplitTEI(File inputFile, File outputDirectory, 
    String corpusSchemeURL, String documentSchemeURL, String preparationSchemeURL)
  {
    this.inputFile = inputFile;
    this.outputDirectory = outputDirectory;
    this.corpusSchemeURL = corpusSchemeURL;
    this.documentSchemeURL = documentSchemeURL;
    this.preparationSchemeURL = preparationSchemeURL;
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
      
      TEIValidator.Errors errors = new TEIValidator.Errors();
     
      errors.putAll(extractMainCorpusHeader(doc));
      errors.putAll(extractDocumentHeaders(doc));
      errors.putAll(extractPreparationSteps(doc));
      
      if(!errors.isEmpty())
      {
        System.out.println(TEIValidator.formatParserExceptions(errors));
        throw new LaudatioException("Source document was invalid");
      }

    }
    catch (JDOMException ex)
    {
      throw new LaudatioException(ex.getLocalizedMessage());
    }
    catch (SAXException ex)
    {
      throw new LaudatioException(TEIValidator.getSAXParserError(ex));
    }
    catch (IOException ex)
    {
      throw new LaudatioException(ex.getLocalizedMessage());
    }
  }

  private TEIValidator.Errors extractMainCorpusHeader(Document doc) throws LaudatioException, IOException, SAXException
  {
    TEIValidator validator = 
      corpusSchemeURL == null ? new TEICorpusValidator() : new FromURLValidator(corpusSchemeURL);
    
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

      if(corpusSchemeURL == null)
      {
        corpusDoc.addContent(0, new ProcessingInstruction("xml-model", 
          "href=\"" + TEICorpusValidator.DEFAULT_SCHEME_URL + "\""));
      }
      else
      {
        corpusDoc.addContent(0, new ProcessingInstruction("xml-model", 
          "href=\"" + corpusSchemeURL + "\""));
      }
      
      // we need to append an empty "text" element after the header
      Element text = new Element("text", teiNS);
      text.setText("");
      newRootForCorpus.addContent(text);

      // we work with the copy from now
      corpusHeader = newRootForCorpus.getChild("teiHeader", null);
      Preconditions.checkNotNull(corpusHeader, messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));
      
      Preconditions.checkState("CorpusHeader".equals(corpusHeader.getAttributeValue("type")));
      
      Preconditions.checkNotNull(corpusHeader.getChild("fileDesc", null), messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));
      Preconditions.checkNotNull(corpusHeader.getChild("fileDesc", null).getChild("titleStmt", null), messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));
      
      String title = corpusHeader.getChild("fileDesc", null)
        .getChild("titleStmt", null)
        .getChildTextNormalize("title", null);
      Preconditions.checkNotNull(title, messages.getString(
          "ERROR NO CORPUS TITLE GIVEN"));

      // save the file with the title as file name
      File outputFile = new File(corpusDir, title + ".xml");
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(corpusDoc, new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
      log.info(messages.getString("WRITTEN CORPUS HEADER"), outputFile.getPath());
      
      
      validator.validate(outputFile);
      
    }
    return validator.getErrors();
  }
  
  private TEIValidator.Errors extractDocumentHeaders(Document doc) throws LaudatioException, IOException, SAXException
  {
    TEIValidator validator = 
      documentSchemeURL == null ? new TEIDocumentValidator(): new FromURLValidator(documentSchemeURL);
    
    File documentDir = new File(outputDirectory, "DocumentHeader");
    if (!documentDir.exists() && !documentDir.mkdir())
    {
      throw new LaudatioException(messages.getString(
        "COULD NOT CREATE DIRECTORY")
        + documentDir.getAbsolutePath());
    }
    
    Element documentRoot = Preconditions.checkNotNull(doc.getRootElement()
      .getChild("teiCorpus", null));
    
    
    for(Element docHeader : documentRoot.getChildren("teiHeader", null))
    {
      Preconditions.checkState("DocumentHeader".equals(docHeader.getAttributeValue("type")));
      
      // create the subtree for the global corpus header
      Namespace teiNS = Namespace.getNamespace(
        "http://www.tei-c.org/ns/1.0");
      Element tei = new Element("TEI", teiNS);
      tei.addContent(docHeader.clone());
      Document newDoc = new Document(tei);
      
      if(documentSchemeURL == null)
      {
        newDoc.addContent(0, new ProcessingInstruction("xml-model", 
          "href=\"" + TEIDocumentValidator.DEFAULT_SCHEME_URL + "\""));
      }
      else
      {
        newDoc.addContent(0, new ProcessingInstruction("xml-model", 
          "href=\"" + documentSchemeURL + "\""));
      }
      
      // we need to append an empty "text" element after the header
      Element text = new Element("text", teiNS);
      text.setText("");
      tei.addContent(text);
      
      Element fileDesc = Preconditions.checkNotNull(tei.getChild("teiHeader", null).getChild("fileDesc", null));
      
      String outName = UUID.randomUUID().toString();
      
      String id = fileDesc.getAttributeValue("id", Namespace.XML_NAMESPACE);
      if(id != null)
      {
        outName = id;
      }
      else
      {
        Element titleStmt = Preconditions.checkNotNull(fileDesc.getChild("titleStmt", null));
        
        String title = titleStmt.getChildText("title", null);
        if(title != null)
        {
          outName = title;
        }
      }
      
      
      File outputFile = new File(documentDir, outName + ".xml");
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      xmlOut.output(newDoc, new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
      log.info(messages.getString("WRITTEN DOCUMENT HEADER"), outputFile.getPath());
      
      
      validator.validate(outputFile);
      
    }
    return validator.getErrors();
  }
  
  private TEIValidator.Errors extractPreparationSteps(Document doc) throws LaudatioException, IOException, SAXException
  {
    TEIValidator validator = 
      preparationSchemeURL == null ? new TEIPreparationValidator(): new FromURLValidator(preparationSchemeURL);
    Multiset<String> knownPreparationTitles = HashMultiset.create();
    
    File documentDir = new File(outputDirectory, "PreparationHeader");
    if (!documentDir.exists() && !documentDir.mkdir())
    {
      throw new LaudatioException(messages.getString(
        "COULD NOT CREATE DIRECTORY")
        + documentDir.getAbsolutePath());
    }
    
    Preconditions.checkNotNull(doc.getRootElement()
      .getChild("teiCorpus", null));
    Element preparationRoot = Preconditions.checkNotNull(doc.getRootElement()
      .getChild("teiCorpus", null).getChild("teiCorpus", null));
    
    for(Element preparationHeader : preparationRoot.getChildren("teiHeader", null))
    {
      Preconditions.checkState("PreparationHeader".equals(preparationHeader.getAttributeValue("type")));
      
      // create the subtree for the global corpus header
      Namespace teiNS = Namespace.getNamespace(
        "http://www.tei-c.org/ns/1.0");
      Element tei = new Element("TEI", teiNS);
      tei.addContent(preparationHeader.clone());
      Document newDoc = new Document(tei);
      
      
      if(preparationSchemeURL == null)
      {
        newDoc.addContent(0, new ProcessingInstruction("xml-model", 
          "href=\"" + TEIPreparationValidator.DEFAULT_SCHEME_URL + "\""));
      }
      else
      {
        newDoc.addContent(0, new ProcessingInstruction("xml-model", 
          "href=\"" + preparationSchemeURL + "\""));
      }
      
      // we need to append an empty "text" element after the header
      Element text = new Element("text", teiNS);
      text.setText("");
      tei.addContent(text);
      
      Element fileDesc = Preconditions.checkNotNull(tei.getChild("teiHeader", null).getChild("fileDesc", null));
      
      String outName = UUID.randomUUID().toString();

      Element titleStmt = Preconditions.checkNotNull(fileDesc.getChild("titleStmt", null));
      Element title = Preconditions.checkNotNull(titleStmt.getChild("title", null));
      String corresp = title.getAttributeValue("corresp");
      if(corresp != null)
      {
        if(knownPreparationTitles.contains(corresp))
        {
          knownPreparationTitles.add(corresp);
          outName = corresp +  "_" + knownPreparationTitles.count(corresp);
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
      xmlOut.output(newDoc, new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
      log.info(messages.getString("WRITTEN PREPARATION HEADER"), outputFile.getPath());
      
      validator.validate(outputFile);
      
    }
    return validator.getErrors();
  }
  
}
