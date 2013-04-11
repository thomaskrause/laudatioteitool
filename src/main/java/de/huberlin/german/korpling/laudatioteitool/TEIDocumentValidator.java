/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import static de.huberlin.german.korpling.laudatioteitool.TEIValidator.schemaFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TEIDocumentValidator extends TEIValidator
{

  private final static Logger log = LoggerFactory.getLogger(
    TEIDocumentValidator.class);
  
  private Validator validator = null;

  public TEIDocumentValidator()
  {
    StreamSource source =
      new StreamSource(
      TEIDocumentValidator.class.getResourceAsStream(
      "teiODD_LAUDATIODocument.rnc"));
    try
    {
      this.validator = schemaFactory.newSchema(source).newValidator();
    }
    catch (SAXException ex)
    {
      log.error(null, ex);
    }
  }

  @Override
  public Validator getValidator()
  {
    return validator;
  }
  
  
}
