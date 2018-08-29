package com.ps.druiddemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations={"classpath:druid-test.spring.xml"})
public class Config {
}
