package org.jas.plugins.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Created by jabali on 3/1/17.
 */
public class ReleaseContext implements ApplicationContextAware {

  public static ApplicationContext applicationContext;

  @Autowired
  private ApplicationEventPublisher publisher;


  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }


  public ApplicationEventPublisher getSpringEventPublisher() {
    return publisher;
  }
}
