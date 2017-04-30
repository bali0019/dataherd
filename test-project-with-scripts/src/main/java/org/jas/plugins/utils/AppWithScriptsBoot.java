package org.jas.plugins.utils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.jas.plugins.utils.annotations.EnableDataHerd;

/**
 * Created by jabali on 3/3/17.
 */


@EnableDataHerd(scriptsBasePackage = "us.deloitteinnovation.utils", isBlocking = true)
@SpringBootApplication
@EnableAutoConfiguration
public class AppWithScriptsBoot {

    public static void main(String[] args) {
        SpringApplication.run(AppWithScriptsBoot.class, args);
    }

}
