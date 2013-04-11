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

import com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public abstract class TEIValidator
{

  private final static org.slf4j.Logger log = LoggerFactory.getLogger(
    TEIValidator.class);
  private Errors errors;
  
  protected final static CompactSyntaxSchemaFactory schemaFactory = new CompactSyntaxSchemaFactory();

  protected TEIValidator()
  {
    this.errors = new Errors();

  }

  public void clearErrors()
  {
    errors.clear();
  }

  public boolean validate(final File file) throws IOException
  {
    Validator validator = getValidator();
    Validate.notNull(validator);

    validator.setErrorHandler(new ErrorHandler()
    {
      @Override
      public void warning(SAXParseException exception) throws SAXException
      {
        // ignore warnings
      }

      @Override
      public void error(SAXParseException exception) throws SAXException
      {
        errors.addError(file, exception);
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXException
      {
        errors.addError(file, exception);
      }
    });

    boolean valid = true;
    try
    {
      validator.validate(new SAXSource(
        new InputSource(new FileInputStream(file))));
    }
    catch (SAXException ex)
    {
      // not well-formed
      valid = false;
    }
    return valid && errors.size() == 0;
  }

  public Errors getErrors()
  {
    return errors;
  }
  
  public abstract Validator getValidator();

  @Override
  public String toString()
  {
    if (errors == null && errors.isEmpty())
    {
      return "valid";
    }
    else
    {
      return formatParserExceptions(errors);
    }
  }

  public static String getSAXParserError(Throwable ex)
  {
    if (ex == null)
    {
      return "unknown validation exception";
    }
    else if (ex instanceof SAXParseException)
    {
      return ((SAXParseException) ex).toString();
    }
    else
    {
      return getSAXParserError(ex.getCause());
    }
  }

  public static String formatParserExceptions(Errors errors)
  {
    if (errors.isEmpty())
    {
      return "";
    }

    StringBuilder sb = new StringBuilder();
      
    for (Map.Entry<File, List<SAXParseException>> entry : errors.entrySet())
    {
      List<?> linesRaw = new LinkedList<String>();
      try
      {
        // split lines so we can reuse them later
        linesRaw = FileUtils.readLines(entry.getKey(), "UTF-8");
      }
      catch (IOException ex)
      {
        log.error("UTF-8 is an unknown encoding on this computer", entry.getKey());
      }
      String[] lines = linesRaw.toArray(new String[linesRaw.size()]);


      String header = entry.getKey().getPath() + " has " + entry.getValue().size()
        + (entry.getValue().size() > 1 ? " errors" : " error");
      sb.append(header).append("\n");

      sb.append(StringUtils.repeat("=", header.length())).append("\n");

      ListIterator<SAXParseException> it = entry.getValue().listIterator();
      while (it.hasNext())
      {
        SAXParseException ex = it.next();

        int columnNr = ex.getColumnNumber();
        int lineNr = ex.getLineNumber();

        sb.append("[line ").append(lineNr).append("/column ").append(columnNr).
          append("]").append("\n");
        String caption = ex.getLocalizedMessage();

        sb.append(caption).append("\n");

        // output complete affected line 
        String line = lines[lineNr - 1];
        sb.append(line).append("\n");

        // output a marker for the columns
        sb.append(StringUtils.leftPad("^", columnNr - 1)).append("\n");

        sb.append(StringUtils.repeat("-", Math.min(80, Math.
          max(caption.length(),
          line.length()))))
          .append("\n");
      }
    }


    return sb.toString();
  }

  public static class Errors extends TreeMap<File, List<SAXParseException>>
  {

    public void addError(File file, SAXParseException ex)
    {
      if (!containsKey(file))
      {
        put(file, new LinkedList<SAXParseException>());
      }
      get(file).add(ex);
    }

    public List<SAXParseException> getErrors(File file)
    {
      if (!containsKey(file))
      {
        put(file, new LinkedList<SAXParseException>());
      }
      return get(file);
    }
  }
}
