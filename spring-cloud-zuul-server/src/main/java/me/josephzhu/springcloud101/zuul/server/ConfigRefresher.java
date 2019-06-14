package me.josephzhu.springcloud101.zuul.server;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

//@Service
public class ConfigRefresher/* implements ApplicationContextAware*/ {
  /*private ApplicationContext applicationContext;

  @ApolloConfig
  private Config config;

  @PostConstruct
  private void initialize() {
    refresher(config.getPropertyNames());
  }

  @ApolloConfigChangeListener
  private void onChange(ConfigChangeEvent changeEvent) {
    refresher(changeEvent.changedKeys());
  }

  private void refresher(Set<String> changedKeys) {

    for (String changedKey : changedKeys) {
      System.out.println("this key is changed:"+changedKey);
    }
    this.applicationContext.publishEvent(new EnvironmentChangeEvent(changedKeys));

  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }*/
}
