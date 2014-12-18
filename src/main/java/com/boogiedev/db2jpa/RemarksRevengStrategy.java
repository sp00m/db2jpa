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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.reveng.DelegatingReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.TableIdentifier;

/**
 * Reverse engineering strategy based on the "remarks" property of the database tables and fields.<br>
 * <br>
 * The transformation rules applied to the remarks are:
 *
 * <pre>
 * +------------+----------------+-------------------+
 * |   Remark   | Table to class | Field to property |
 * +------------+----------------+-------------------+
 * | the remark | TheRemark      | theRemark         |
 * | the_remark | TheRemark      | theRemark         |
 * | theremark  | Theremark      | theremark         |
 * | theRemark  | Theremark      | theremark         |
 * +------------+----------------+-------------------+
 * </pre>
 *
 * If no remark has been set, the corresponding table/field real name will be used to proceed the transformation.<br />
 * <br />
 * For example, given the below MySQL table named "tbl_user" with the comment "user":
 *
 * <pre>
 * +----------------------+--------------+-----+-------------------+
 * |         name         |     type     | ... |     comments      |
 * +----------------------+--------------+-----+-------------------+
 * | id_user              | int(11)      | ... |                   |
 * | tx_email             | varchar(100) | ... | the email         |
 * | tx_username          | varchar(20)  | ... | userName          |
 * | tx_password          | char(40)     | ... | password          |
 * | bl_need_confirmation | tinyint(1)   | ... | need_confirmation |
 * +----------------------+--------------+-----+-------------------+
 * </pre>
 *
 * The following entity will be generated:
 *
 * <pre>
 * public class User {
 *
 * 	private int idUser;
 * 	private String theEmail;
 * 	private String username;
 * 	private String password;
 * 	private boolean needConfirmation;
 *
 * 	// ...
 *
 * }
 * </pre>
 */
public class RemarksRevengStrategy extends DelegatingReverseEngineeringStrategy {

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

	/** Column index refering to the table name. */
	private static final String TABLE_NAME_INDEX = "TABLE_NAME";

	/** Column index refering to the column name. */
	private static final String COLUMN_NAME_INDEX = "COLUMN_NAME";

	/** Column index refering to the remarks. */
	private static final String COMMENT_INDEX = "REMARKS";

	/** Map containing every tables remarks. */
	private static final Map<String, String> TABLES_COMMENT = new HashMap<>();

	/** Map containing every fields remarks. */
	private static final Map<String, Map<String, String>> FIELDS_COMMENT = new HashMap<>();

	/**
	 * Creates a new connection.
	 *
	 * @return The connection.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, PROPERTIES);
	}

	/**
	 * Returns the columns metadata.
	 *
	 * @param connection
	 *          The connection.
	 * @return The columns metadata.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	private static ResultSet getColumnsMetadata(Connection connection) throws SQLException {
		return connection.getMetaData().getColumns(null, null, "%", null);
	}

	/**
	 * Returns the tables metadata.
	 *
	 * @param connection
	 *          The connection.
	 * @return The tables metadata.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	private static ResultSet getTablesMetadata(Connection connection) throws SQLException {
		return connection.getMetaData().getTables(null, null, "%", null);
	}

	/**
	 * Stores every fields remarks into the dedicated map.
	 *
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	private static void buildFieldsComments() throws SQLException {
		try (Connection connection = getConnection(); ResultSet resultSet = getColumnsMetadata(connection)) {
			while (resultSet.next()) {
				String tableName = format(resultSet.getString(TABLE_NAME_INDEX));
				String comment = format(resultSet.getString(COMMENT_INDEX));
				if (isNotBlank(comment)) {
					if (!FIELDS_COMMENT.containsKey(tableName)) {
						FIELDS_COMMENT.put(tableName, new HashMap<String, String>());
					}
					FIELDS_COMMENT.get(tableName).put(format(resultSet.getString(COLUMN_NAME_INDEX)), comment);
				}
			}
		}
	}

	/**
	 * Stores every tables remarks into the dedicated map.
	 *
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	private static void buildTablesComments() throws SQLException {
		try (Connection connection = getConnection(); ResultSet resultSet = getTablesMetadata(connection)) {
			while (resultSet.next()) {
				String comment = format(resultSet.getString(COMMENT_INDEX));
				if (isNotBlank(comment)) {
					TABLES_COMMENT.put(format(resultSet.getString(TABLE_NAME_INDEX)), comment);
				}
			}
		}
	}

	/**
	 * Formats a string.
	 *
	 * @param string
	 *          The string to format.
	 */
	private static String format(String string) {
		return string.toLowerCase().trim().replaceAll("  +", " ");
	}

	/**
	 * Returns whether a string is blank or not.
	 *
	 * @param string
	 *          The string to check.
	 * @return false if the string is null or empty, true otherwise.
	 */
	private static boolean isNotBlank(String string) {
		return string != null && !string.isEmpty();
	}

	/**
	 * Constructor.
	 *
	 * @param delegate
	 *          The ReverseEngineeringStrategy the super class needs.
	 * @throws SQLException
	 *           If a database access error occurs.
	 */
	public RemarksRevengStrategy(ReverseEngineeringStrategy delegate) throws SQLException {
		super(delegate);
		buildTablesComments();
		buildFieldsComments();
	}

	/**
	 * Manages the "database field to property name" transformation:
	 * <ul>
	 * <li>If a database field has a filled remark property, its content will be used</li>
	 * <li>Otherwise, the database field name will be used</li>
	 * </ul>
	 */
	@Override
	public String columnToPropertyName(TableIdentifier table, String column) {
		String tableName = format(table.getName());
		String columnName = format(column);
		if (FIELDS_COMMENT.containsKey(tableName)) {
			Map<String, String> map = FIELDS_COMMENT.get(tableName);
			if (map.containsKey(columnName)) {
				return super.columnToPropertyName(table, map.get(columnName));
			}
		}
		return super.columnToPropertyName(table, column);
	}

	/**
	 * Manages the "database table to class name" transformation:
	 * <ul>
	 * <li>If a database table has a filled remark property, its content will be used</li>
	 * <li>Otherwise, the database table name will be used</li>
	 * </ul>
	 */
	@Override
	public String tableToClassName(TableIdentifier tableIdentifier) {
		String tableName = format(tableIdentifier.getName());
		if (TABLES_COMMENT.containsKey(tableName)) {
			return super.tableToClassName(new TableIdentifier(TABLES_COMMENT.get(tableName)));
		}
		return super.tableToClassName(tableIdentifier);
	}

}
