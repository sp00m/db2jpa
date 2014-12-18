/*
 * Copyright (c) boogiedev.com, all rights reserved.
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
 * Database utilities.
 */
public final class DatabaseUtils {

	/** Hibernate connection properties. */
	private static final Properties PROPERTIES;

	/** Hibernate URL property value. */
	private static final String URL;

	/**
	 * Accesses the Hibernate properties.
	 */
	static {
		Configuration configuration = new Configuration().configure();
		URL = configuration.getProperty("hibernate.connection.url");
		PROPERTIES = new Properties();
		PROPERTIES.setProperty("user", configuration.getProperty("hibernate.connection.username"));
		PROPERTIES.setProperty("password", configuration.getProperty("hibernate.connection.password"));
		PROPERTIES.setProperty("useInformationSchema", "true");
	}

	/**
	 * Builds a database connection from the Hibernate properties.
	 *
	 * @return The database connection.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	public static Connection buildConnection() throws SQLException {
		return DriverManager.getConnection(URL, PROPERTIES);
	}

	/**
	 * Closes a connection if not null.
	 *
	 * @param connection
	 *          The connection to close.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	public static void close(Connection connection) throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	/**
	 * Closes a result set if not null.
	 *
	 * @param resultSet
	 *          The result set to close.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	public static void close(ResultSet resultSet) throws SQLException {
		if (resultSet != null) {
			resultSet.close();
		}
	}

	/**
	 * Private nullary constructor.
	 */
	private DatabaseUtils() {
		// no-op
	}

}
