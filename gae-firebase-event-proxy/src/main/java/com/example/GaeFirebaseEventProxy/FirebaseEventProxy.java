package com.example.GaeFirebaseEventProxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.utils.SystemProperty;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger.Level;
import com.google.firebase.database.ValueEventListener;

public class FirebaseEventProxy {

  private static final Logger log = Logger.getLogger(FirebaseEventProxy.class.getName());

  public FirebaseEventProxy() {
    String FIREBASE_LOCATION = "https://crackling-torch-392.firebaseio.com";
    Map<String, Object> databaseAuthVariableOverride = new HashMap<String, Object>();
    // uid and provider will have to match what you have in your firebase security rules
    databaseAuthVariableOverride.put("uid", "gae-firebase-event-proxy");
    databaseAuthVariableOverride.put("provider", "com.example");
    try {
      FirebaseOptions options = new FirebaseOptions.Builder()
          .setServiceAccount(new FileInputStream("WEB-INF/gae-firebase-secrets.json"))
          .setDatabaseUrl(FIREBASE_LOCATION)
          .setDatabaseAuthVariableOverride(databaseAuthVariableOverride).build();
      FirebaseApp.initializeApp(options);
    } catch (IOException e) {
      throw new RuntimeException(
          "Error reading firebase secrets from file: src/main/webapp/WEB-INF/gae-firebase-secrets.json: "
              + e.getMessage());
    }
  }

  public void start() {
    // Redirect stdout to CloudDataStoreStream
    System.setOut(new PrintStream(new CloudDataStoreStream()));
    System.setErr(new PrintStream(new CloudDataStoreStream()));

    FirebaseDatabase.getInstance().setLogLevel(Level.DEBUG);
    DatabaseReference firebase = FirebaseDatabase.getInstance().getReference();
    log.severe("Starting --------------------------");

    // Subscribe to value events. Depending on use case, you may want to subscribe to child events
    // through childEventListener.
    firebase.child("data").addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot snapshot) {
        if (snapshot.exists()) {
          try {
            DatabaseReference dataClone =
                FirebaseDatabase.getInstance().getReference().child("clone");
            dataClone.push().setValue(new Date().toString());
            // Convert value to JSON using Jackson
            String json = new ObjectMapper().writeValueAsString(snapshot.getValue(false));
            // Replace the URL with the url of your own listener app.
            URL dest = new URL("http://gae-firebase-listener-python.appspot.com/log");
            HttpURLConnection connection = (HttpURLConnection) dest.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            // Rely on X-Appengine-Inbound-Appid to authenticate. Turning off redirects is
            // required to enable.
            connection.setInstanceFollowRedirects(false);
            // Fill out header if in dev environment
            if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
              connection.setRequestProperty("X-Appengine-Inbound-Appid", "dev-instance");
            }
            // Put Firebase data into http request
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("&fbSnapshot=");
            stringBuilder.append(URLEncoder.encode(json, "UTF-8"));
            connection.getOutputStream().write(stringBuilder.toString().getBytes());
            if (connection.getResponseCode() != 200) {
              log.severe("Forwarding failed");
            } else {
              log.info("Sent: " + json);
            }
          } catch (JsonProcessingException e) {
            log.severe("Unable to convert Firebase response to JSON: " + e.getMessage());
          } catch (IOException e) {
            log.severe("Error in connecting to app engine: " + e.getMessage());
          }
        }
      }

      @Override
      public void onCancelled(DatabaseError error) {
        log.severe("Firebase connection cancelled: " + error.getMessage());
      }
    });
  }

}
