package no.difi.vefa.validator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class XmlUtils
{
  private static final Pattern ROOT_TAG_PATTERN = Pattern.compile ("<(?!http[s]?://|!|\\?)(\\w*:?[^<>]*?)>",
                                                                   Pattern.MULTILINE);

  private static final Pattern NAMESPACE_PATTERN = Pattern.compile ("xmlns:?([A-Za-z0-9\\-]*)\\w*=\\w*[\"'](.+?)[\"']",
                                                                    Pattern.MULTILINE);

  private static final Pattern COMMENTS_PATTERN = Pattern.compile ("<!--(.*)-->", Pattern.MULTILINE);

  private XmlUtils ()
  {}

  @NonNull
  public static String removeComments (@NonNull final String xmlContent)
  {
    return COMMENTS_PATTERN.matcher (xmlContent).replaceAll ("");
  }

  @Nullable
  public static String extractRootNamespace (@NonNull final String xmlContent)
  {
    final Matcher matcher = ROOT_TAG_PATTERN.matcher (removeComments (xmlContent));
    if (matcher.find ())
    {
      final String rootElement = matcher.group (1).trim ().replace ("\n", " ").replace ("\r", "").replace ("\t", " ");
      // logger.debug("Root element: {}", rootElement);
      final String rootNs = rootElement.split (" ", 2)[0].contains (":") ? rootElement.substring (0,
                                                                                                  rootElement.indexOf (":"))
                                                                         : "";
      // logger.debug("Root ns: {}", rootNs);

      final Matcher nsMatcher = NAMESPACE_PATTERN.matcher (rootElement);
      while (nsMatcher.find ())
      {
        // logger.debug(nsMatcher.group(0));

        if (nsMatcher.group (1).equals (rootNs))
        {
          return nsMatcher.group (2);
        }
      }
    }

    return null;
  }

  @Nullable
  public static String extractLocalName (final String xmlContent)
  {
    final Matcher matcher = ROOT_TAG_PATTERN.matcher (removeComments (xmlContent));
    if (matcher.find ())
    {
      final String rootElement = matcher.group (1).trim ().replace ("\n", " ").replace ("\r", "").replace ("\t", " ");
      // logger.debug("Root element: {}", rootElement);
      return rootElement.split (" ", 2)[0].contains (":") ? rootElement.substring (rootElement.indexOf (":") + 1,
                                                                                   rootElement.indexOf (" "))
                                                          : rootElement.split (" ", 2)[0];
    }
    return null;
  }
}
