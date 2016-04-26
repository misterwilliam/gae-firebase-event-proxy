package com.example.GaeFirebaseEventProxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.firebase.security.token.TokenGenerator;

public class FirebaseEventProxy {

  private static final Logger log = Logger.getLogger(FirebaseEventProxy.class.getName());

  public FirebaseEventProxy() {
    String firebaseSecret = this.getFirebaseSecret();
    log.info("Token: " + this.getFirebaseAuthToken(firebaseSecret));
  }

  public void start() {

  }

  private String getFirebaseSecret() {
    Properties props = new Properties();
    try {
      // Read from src/main/webapp/firebase-secrets.properties
      InputStream inputStream = new FileInputStream("firebase-secret.properties");
      props.load(inputStream);
      return props.getProperty("firebaseSecret");
    } catch (java.net.MalformedURLException e) {
      throw new RuntimeException(
          "Error reading firebase secrets from file: src/main/webapp/firebase-sercrets.properties: "
              + e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException(
          "Error reading firebase secrets from file: src/main/webapp/firebase-sercrets.properties: "
              + e.getMessage());
    }
  }

  private String getFirebaseAuthToken(String firebaseSecret) {
    Map<String, Object> authPayload = new HashMap<String, Object>();
    // uid and provider will have to match what you have in your firebase security rules
    authPayload.put("uid", "gae-firebase-event-proxy");
    authPayload.put("provider", "com.example");
    TokenGenerator tokenGenerator = new TokenGenerator(firebaseSecret);
    return tokenGenerator.createToken(authPayload);
  }

}
