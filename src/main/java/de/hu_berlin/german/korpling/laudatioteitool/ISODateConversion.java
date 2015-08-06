/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hu_berlin.german.korpling.laudatioteitool;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * A class that transform TEI "when" attributes from the original
 * TEI standard to ISO 8601.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ISODateConversion
{
  private static final DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();
  
  public static String toISO(String orig)
  {
    DateTime t = DateTime.parse(orig);
    return t.toString(isoFormatter);
  }
}
