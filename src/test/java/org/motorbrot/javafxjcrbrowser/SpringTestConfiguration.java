package org.motorbrot.javafxjcrbrowser;

import org.motorbrot.javafxjcrbrowser.services.JcrService;
import org.motorbrot.javafxjcrbrowser.services.JcrServiceMock;
import org.motorbrot.javafxjcrbrowser.services.SpringDIConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * return a jcr-mock in unit-tests
 */
@Configuration
@Import(JavaFxJcrBrowserApplication.class)
public class SpringTestConfiguration extends SpringDIConfiguration {
  
  @Bean
  @Override
	public JcrService jcrService(){
		return new JcrServiceMock();
	}
  
}
