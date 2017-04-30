package org.jas.plugins.utils;

import com.mongodb.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * Created by jabali on 3/4/17.
 */

@Validated
@Configuration
@EnableTransactionManagement
@ConfigurationProperties(prefix = "mongodb")
public class TestAppConfig extends AbstractMongoConfiguration {



    @NotNull
    private String mongoHost;

    @NotNull
    private Integer mongoPort;

    @NotNull
    private String mongoUser;

    @NotNull
    private String mongoPassword;

    @NotNull
    private String mongoDBName;

    @Bean
    public MongoCredential userCredentials() {
        return MongoCredential.createCredential(this.mongoUser, this.mongoDBName, this.mongoPassword.toCharArray());
    }

    @Bean
    public LoggingEventListener mappingEventsListener() {
        return new LoggingEventListener();
    }

    @Override
    protected String getDatabaseName() {
        return this.mongoDBName;
    }

    @Override
    public Mongo mongo() {
        return mongo(this.serverAddress(), this.userCredentials());
    }

    @Bean
    public ServerAddress serverAddress() {
        ServerAddress serverAddress;
        serverAddress = new ServerAddress(this.mongoHost, this.mongoPort);
        return serverAddress;
    }

    @Bean
    @Override
    public MongoDbFactory mongoDbFactory() {
        return this.mongoDbFactory(mongo(serverAddress(), userCredentials()));
    }

    @Bean
    public MongoDbFactory mongoDbFactory(MongoClient mongoClient) {
        return new SimpleMongoDbFactory(mongoClient, this.mongoDBName);
    }

    @Bean
    public MongoClient mongo(ServerAddress serverAddress, MongoCredential credential) {
        return new MongoClient(serverAddress, Arrays.asList(credential));
    }

    @Override
    @Bean
    public MongoTemplate mongoTemplate() {

        final MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);

        return mongoTemplate;
    }

    @Bean
    public MongoOperations mongoOperations() {
        return mongoTemplate();
    }

    @Override
    protected String getMappingBasePackage() {
        return "us.deloitteinnovation.cfsa";
    }

    public String getMongoHost() {
        return mongoHost;
    }

    public void setMongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
    }

    public Integer getMongoPort() {
        return mongoPort;
    }

    public void setMongoPort(Integer mongoPort) {
        this.mongoPort = mongoPort;
    }

    public String getMongoUser() {
        return mongoUser;
    }

    public void setMongoUser(String mongoUser) {
        this.mongoUser = mongoUser;
    }

    public String getMongoPassword() {
        return mongoPassword;
    }

    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }

    public String getMongoDBName() {
        return mongoDBName;
    }

    public void setMongoDBName(String mongoDBName) {
        this.mongoDBName = mongoDBName;
    }

}
