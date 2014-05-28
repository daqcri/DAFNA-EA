package com.mycompany.app;
    
import static spark.Spark.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import spark.Request;
import spark.Response;
import spark.Route;

public class App {
   public static void main(String[] args) {
	   externalStaticFileLocation("/Users/hammady/Workspace/DAFNA-EA/web/client");
	   
      get(new Route("/hello") {
		@Override
		public Object handle(Request arg0, Response arg1) {
			// TODO Auto-generated method stub
			return "Besmellah";
		}    	  
      });   

   
      post(new Route("/upload") {
		@Override
		public Object handle(Request request, Response response) {
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
		    request.raw().setAttribute("org.eclipse.multipartConfig", multipartConfigElement);
		    InputStream istream = null;
		    ObjectOutputStream ostream = null;
		    try {
				Part filePart = request.raw().getPart("csv");
			    String filename = getFilename(filePart);
			    istream = filePart.getInputStream();
			    ostream = new ObjectOutputStream(
			    	    new FileOutputStream(filename));
			    ostream.writeObject(istream.read());
			    ostream.flush();
			    return "OK";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
			    if (istream != null)
					try {
						istream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    if (ostream != null)
					try {
						ostream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		    return "Error";
		}
      });   
   }
   
   private static String getFilename(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
}
