/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import static de.huberlin.german.korpling.laudatioteitool.TEIValidator.schemaFactory;
import java.util.logging.Level;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class TEICorpusValidator extends TEIValidator
{
  private final static Logger log = LoggerFactory.getLogger(TEICorpusValidator.class);

  private Validator validator = null;
  
  public TEICorpusValidator()
  {
    
    StreamSource source =
      new StreamSource(
      TEICorpusValidator.class.getResourceAsStream(
      "teiODD_LAUDATIOCorpus.rnc"));
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
