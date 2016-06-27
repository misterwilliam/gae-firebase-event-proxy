package com.example.GaeFirebaseEventProxy;

import java.io.IOException;
import java.io.OutputStream;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.StringValue;

public class CloudDataStoreStream extends OutputStream {

  private String buffer;
  private final Datastore datastore = DatastoreOptions.defaultInstance().service();
  private final KeyFactory keyFactory = datastore.newKeyFactory().kind("LogEntry");


  @Override
  public void write(int b) throws IOException {
    char newline = "\n".charAt(0);
    if ((char) b != newline) {
      this.buffer += String.valueOf((char) b);
    } else {
      this.writeToDataStore(this.buffer);
      this.buffer = "";
    }
  }

  @Override
  public void flush() {
    // Do nothing
  }

  private void writeToDataStore(String message) {
    Key key = datastore.allocateId(keyFactory.newKey());
    Entity task = Entity.builder(key)
        .set("message", StringValue.builder(message).excludeFromIndexes(true).build())
        .set("timestamp", DateTime.now()).build();
    datastore.put(task);
  }

}
