/*
 * Copyright (c) 2013 db2jpa. All rights reserved.
 * This code is licensed under the LGPL 3.0 license,
 * available at the root application directory.
 */

package com.boogiedev.db2jpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.cfg.Configuration;

/**
 * Database utils.
 * 
 * @since 1.0
 * @version 1.0
 */
public class DatabaseUtils {

    /** Hibernate URL property value. */
    private static final String url;
    /** Hibernate connection properties. */
    private static final Properties properties = new Properties();

    /**
     * Accesses the Hibernate properties.
     */
    static {
        final Configuration configuration = new Configuration().configure();
        url = configuration.getProperty("hibernate.connection.url");
        properties.setProperty("user", configuration.getProperty("hibernate.connection.username"));
        properties.setProperty("password", configuration.getProperty("hibernate.connection.password"));
        properties.setProperty("useInformationSchema", "true");
    }

    /**
     * Builds a database connection from the Hibernate properties.
     * 
     * @return the database connection.
     * @since 1.0
     * @version 1.0
     */
    public static Connection buildConnection() {
        try {
            return DriverManager.getConnection(url, properties);
        } catch (final SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Closes a database connection.
     * 
     * @param connection
     *        The database connection.
     * @since 1.0
     * @version 1.0
     */
    public static void close(final Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Closes a database result set.
     * 
     * @param resultSet
     *        The database result set.
     * @since 1.0
     * @version 1.0
     */
    public static void close(final ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (final SQLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Private nullary constructor.
     * 
     * @since 1.0
     * @version 1.0
     */
    private DatabaseUtils() {
    }

}
