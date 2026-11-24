package no.difi.vefa.validator.expectation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlExpectation extends AbstractExpectation
{
  private static final Logger log = LoggerFactory.getLogger (XmlExpectation.class);

  public XmlExpectation (final byte [] bytes)
  {
    description = "XML expectation [" + bytes.length + " bytes]";

    String content = new String (bytes);
    if (!content.contains ("<!--") || !content.contains ("-->"))
      return;

    // Get the content of the XML comment
    content = content.substring (content.indexOf ("<!--") + 4, content.indexOf ("-->"));

    for (final String section : content.replaceAll ("\\r", "")
                                       .replaceAll ("\\t", " ")
                                       .replace ("  ", "")
                                       /*
                                        * .replaceAll(" \\n", "\\n").replaceAll("\\n ", "\\n")
                                        */
                                       .trim ()
                                       .split ("\\n\\n"))
    {
      final String [] parts = section.split (":", 2);
      switch (parts[0].toLowerCase ())
      {
        case "content", "description" -> description = parts[1].trim ().replaceAll ("\\n", " ").replace ("  ", " ");
        case "success", "successes" -> extractRules (parts, successes);
        case "warning", "warnings" -> extractRules (parts, warnings);
        case "error", "errors" -> extractRules (parts, errors);
        case "fatal", "fatals" -> extractRules (parts, fatals);
        case "scope" -> extractList (parts, scopes);
        default ->
        {
          // Unknown sections are ignored
        }
      }
    }
  }

  private void extractRules (final String [] parts, final Map <String, Integer> target)
  {
    for (final String p : parts[1].replace (" x ", " ")
                                  .replaceAll (" * ", " ")
                                  .replace ("None", "")
                                  .replace ("(", "")
                                  .replace (")", "")
                                  .replace ("times", "")
                                  .replace ("time", "")
                                  .replace ("  ", "")
                                  .trim ()
                                  .split ("\\n"))
    {
      try
      {
        if (!p.trim ().isEmpty ())
        {
          final String [] r = p.trim ().split (" ");
          if (!target.containsKey (r[0]))
            target.put (r[0], 0);
          target.put (r[0],
                      Integer.valueOf (target.get (r[0]).intValue () + (r.length == 1 ? 1 : Integer.parseInt (r[1]))));
        }
      }
      catch (final Exception e)
      {
        log.warn (e.getMessage ());
      }
    }
  }

  private void extractList (final String [] parts, final List <String> target)
  {
    for (final String part : parts[1].split ("\\n"))
      if (!part.trim ().isEmpty ())
        target.add (part.trim ());
  }
}
