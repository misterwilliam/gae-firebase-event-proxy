package com.example.GaeFirebaseEventProxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
public class HelloServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
    out.println("Status up");

    Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
    for (Map.Entry<Thread, StackTraceElement[]> entry : threads.entrySet()) {
      for (StackTraceElement element : entry.getValue()) {
        out.println(element.toString());
      }
    }
  }
}
