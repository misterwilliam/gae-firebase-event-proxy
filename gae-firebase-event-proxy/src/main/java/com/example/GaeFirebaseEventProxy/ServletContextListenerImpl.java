package com.example.GaeFirebaseEventProxy;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextListenerImpl implements ServletContextListener {

  private static final Logger log = Logger.getLogger(ServletContextListener.class.getName());
  static boolean hasInitialized = false;

  @Override
  public void contextInitialized(ServletContextEvent event) {
    if (!hasInitialized) {
      log.info("Starting ....");
      FirebaseEventProxy proxy = new FirebaseEventProxy();
      proxy.start();
      hasInitialized = true;
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    // App Engine does not currently invoke this method.
  }
}
