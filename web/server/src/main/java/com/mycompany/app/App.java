package com.mycompany.app;
    
import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Route;

public class App {
   public static void main(String[] args) {
	   externalStaticFileLocation("/home/zshahid/workspace/DAFNA-EA/web/client");
	   
      get(new Route("/hello") {
		@Override
		public Object handle(Request arg0, Response arg1) {
			// TODO Auto-generated method stub
			return "Besmellah";
		}    	  
      });   
   }
}
