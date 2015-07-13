/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hu_berlin.german.korpling.laudatioteitool;

import static de.hu_berlin.german.korpling.laudatioteitool.TEIValidator.compactSchemaFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TEIPreparationValidator extends TEIValidator
{

  private final static Logger log = LoggerFactory.getLogger(
    TEIPreparationValidator.class);
  
  public static final String DEFAULT_SCHEME_URL 
    = "http://korpling.german.hu-berlin.de/schemata/laudatio/teiODD_LAUDATIOPreparation_Scheme7.rng";
  
  private Validator validator = null;

  
  public TEIPreparationValidator()
  {
    StreamSource source = new StreamSource(
      TEIPreparationValidator.class.getResourceAsStream(
      "default_preparation.rng"));
    try
    {
      this.validator = xmlSchemaFactory.newSchema(source).newValidator();
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
