/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.xtec.jclic.report.servlet;

import edu.xtec.jclic.report.rp.ReportsRequestProcessor;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 *
 * @author Francesc Busquets (fbusquets@xtec.cat)
 * @version 13.09.09
 */
public class ContextParamsReader implements ServletContextListener {

  public static final String PREFIX_ENV_KEY = "PREFIX_ENV_KEY";
  public static final String PREFIX_KEY = "PREFIX_KEY";
  public static final String PREFIX_SEP = "_";

  public void contextInitialized(ServletContextEvent sce) {
    javax.servlet.ServletContext sc = sce.getServletContext();

    try {
      // Read defaut values
      ReportsRequestProcessor.loadProperties(null);
      Properties prop = ReportsRequestProcessor.getProperties();

      String prefixKey = null;

      // Try to find the name of the environment variable containing
      // the prefix key
      String prefixEnvKey = sc.getInitParameter(PREFIX_ENV_KEY);
      if (prefixEnvKey == null) prefixEnvKey = prop.getProperty(PREFIX_ENV_KEY);

      if (prefixEnvKey != null) prefixKey = System.getenv(prefixEnvKey);

      // No environment variable set, then try to find the prefix key
      // directly
      if (prefixKey == null) {
        prefixKey = sc.getInitParameter(PREFIX_KEY);
        if (prefixKey == null) prefixKey = prop.getProperty(PREFIX_KEY);
      }

      if (prefixKey != null && !prefixKey.endsWith(PREFIX_SEP)) prefixKey = prefixKey + PREFIX_SEP;

      // Read properties stored in web.xml
      Enumeration en = sc.getInitParameterNames();
      while (en.hasMoreElements()) {
        String key = (String) en.nextElement();
        if (prefixKey == null || key.startsWith(prefixKey) || key.indexOf(PREFIX_SEP) < 0) {
          String value = sc.getInitParameter(key);
          if (value != null && value.length() > 0) {
            String realKey =
                prefixKey == null || key.indexOf(PREFIX_SEP) < 0
                    ? key
                    : key.substring(prefixKey.length());
            prop.put(realKey, value);
            // System.out.println("Clau: "+realKey+" - Valor: "+value);
          }
        }
      }

      // Values stored in local properties file have always preference
      ReportsRequestProcessor.loadLocalProperties(null);

    } catch (Exception ex) {
      System.err.println("Error reading initial parameters: " + ex);
    }
  }

  public void contextDestroyed(ServletContextEvent sce) {}
}
