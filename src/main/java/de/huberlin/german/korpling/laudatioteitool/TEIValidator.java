/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.apache.commons.lang3.Validate;
import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TEIValidator
{
  private final static org.slf4j.Logger log = LoggerFactory.getLogger(TEIValidator.class);
  
  private CompactSyntaxSchemaFactory schemaFactory;
  private Validator corpusValidator;
  private Validator documentValidator;
  private Validator preparationValidator;
  
  public TEIValidator()
  {
    this.schemaFactory = new CompactSyntaxSchemaFactory();
    try
    {
      Schema corpusSchema = schemaFactory.newSchema(
        new StreamSource(TEIValidator.class.getResourceAsStream(
        "teiODD_LAUDATIOCorpus.rnc")));
      corpusValidator = corpusSchema.newValidator();
      
      Schema documentSchema = schemaFactory.newSchema(
        new StreamSource(TEIValidator.class.getResourceAsStream(
        "teiODD_LAUDATIODocument.rnc")));
      documentValidator = documentSchema.newValidator();
      
      Schema preparationSchema = schemaFactory.newSchema(
        new StreamSource(TEIValidator.class.getResourceAsStream(
        "teiODD_LAUDATIOPreparation.rnc")));
      preparationValidator = preparationSchema.newValidator();
    }
    catch (SAXException ex)
    {
      log.error(null, ex);
    }
  }
  
  public void validateCorpus(Document doc) throws SAXException, IOException
  {
    Validate.notNull(corpusValidator);
    corpusValidator.validate(new JDOMSource(doc));
  }
  
  public void validateDocument(Document doc) throws SAXException, IOException
  {
    Validate.notNull(documentValidator);
    documentValidator.validate(new JDOMSource(doc));
  }
  
  public void validatePreparation(Document doc) throws SAXException, IOException
  {
    Validate.notNull(preparationValidator);
    preparationValidator.validate(new JDOMSource(doc));
  }
  
  public static String getSAXParserError(Throwable ex)
  {
    if(ex == null)
    {
      return "unknown validation exception";
    }
    else if(ex instanceof SAXParseException)
    {
      return ((SAXParseException) ex).toString();
    }
    else
    {
      return getSAXParserError(ex.getCause());
    }
  }
}
