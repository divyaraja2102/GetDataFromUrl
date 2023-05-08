package com.Timertest.getdata;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@Component
@SpringBootApplication
public class GetdataApplication {

	public static void main(String[] args) throws Exception {
		try {
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(new OpenAIPostsPuller());
			context.start();
			Thread.sleep(60000); // Keep the context running for 1 minute
			context.stop();
		} catch (Exception e) {
		}
	}
}
