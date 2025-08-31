package no.difi.vefa.validator.util;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringHelper;
import com.helger.io.resource.ClassPathResource;

/**
 * @author erlend
 */
public class ClasspathURIResolver implements URIResolver
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ClasspathURIResolver.class);

  private final String m_sPath;
  private final ClassLoader m_aClassLoader;

  public ClasspathURIResolver (final String path, final ClassLoader classLoader)
  {
    m_sPath = StringHelper.trimEnd (path, '/');
    m_aClassLoader = classLoader;
  }

  public Source resolve (final String href, final String base)
  {
    final String ret = !"".equals (base) ? null : m_sPath + "/" + href;
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Resolving href '" + href + "' with base '" + base + "' to: " + ret);
    return ret == null ? null : new StreamSource (ClassPathResource.getInputStream (ret, m_aClassLoader));
  }
}
