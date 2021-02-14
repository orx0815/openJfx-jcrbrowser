package org.motorbrot.javafxjcrbrowser.services;

import org.motorbrot.javafxjcrbrowser.JavaFxJcrBrowserApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** 
 * Spring Dependency Injection Configuration with Annotations
 */
@Configuration
@Import(JavaFxJcrBrowserApplication.class)
public class SpringDIConfiguration {
  
  /**
   * @return the mocked session
   */
  @Bean
	public JcrService jcrService(){
		return new JcrService();
	}
  
}
