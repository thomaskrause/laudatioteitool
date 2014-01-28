/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.huberlin.german.korpling.laudatioteitool;

import static de.huberlin.german.korpling.laudatioteitool.TEIValidator.compactSchemaFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class FromURLValidator extends TEIValidator
{
  private final static Logger log = LoggerFactory.getLogger(
    FromURLValidator.class);
  private static final ResourceBundle messages =
    ResourceBundle.getBundle("de/huberlin/german/korpling/laudatioteitool/Messages");
  
  private Validator validator = null;

  public FromURLValidator(String url)
  {
    try
    {
      if(url.endsWith(".rng"))
      {
        this.validator = xmlSchemaFactory.newSchema(new URL(url)).newValidator();
      }
      else
      {
        this.validator = compactSchemaFactory.newSchema(new URL(url)).newValidator();
      }
    }
    catch (MalformedURLException ex)
    {
      log.error(messages.getString("MALFORMED URL"), url, ex.getMessage());
    }
    catch (SAXException ex)
    {
      log.error(messages.getString("PARSING EXCEPTION"), ex.getMessage());
    }
  }

  
  @Override
  public Validator getValidator()
  {
    return validator;
  }
  
}
