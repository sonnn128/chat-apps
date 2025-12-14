package com.nnson128.chatservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.data.cassandra.config.*;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.lang.NonNull;

import java.util.List;

@Configuration
public class CassandraConfiguration extends AbstractCassandraConfiguration implements BeanClassLoaderAware {
    private static String orDefault(String value, String def) {
        return (value == null || value.isBlank()) ? def : value;
    }

    String host = orDefault(System.getenv("CASSANDRA_HOST"), "127.0.0.1");
    int port = Integer.parseInt(orDefault(System.getenv("CASSANDRA_PORT"), "9042"));

    @Override
    @NonNull
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace("chatapps")
            .with(KeyspaceOption.DURABLE_WRITES, true)
            .withSimpleReplication(1)
            .ifNotExists(true);
        return List.of(specification);
    }

    @Override
    @NonNull
    protected String getKeyspaceName() {
        return "chatapps";
    }

    @Override
    protected int getPort() {
        return port;
    }

    @Override
    protected String getLocalDataCenter() {
        return "datacenter1";
    }

    @Override
    @NonNull
    public String getContactPoints() {
        return host;
    }

    @Override
    @NonNull
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    @NonNull
    public String[] getEntityBasePackages() {
        return new String[]{"com.nnson128.chatservice.model"};
    }
}
