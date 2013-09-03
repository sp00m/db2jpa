/*
 * Copyright (c) 2013 db2jpa. All rights reserved.
 * This code is licensed under the LGPL 3.0 license,
 * available at the root application directory.
 */

package com.boogiedev.db2jpa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.cfg.reveng.DelegatingReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.TableIdentifier;

/**
 * Reverse engineering strategy based on the "remarks" property of the database tables and fields.<br>
 * <br>
 * 
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
 * 
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
 *     private int idUser;
 *     private String theEmail;
 *     private String username;
 *     private String password;
 *     private boolean needConfirmation;
 * 
 *     // ...
 * 
 * }
 * </pre>
 * 
 * @since 1.0
 * @version 1.0
 */
public class RemarksRevengStrategy extends DelegatingReverseEngineeringStrategy {

    /** Column index refering to the table name. */
    public static final String TABLE_NAME_INDEX = "TABLE_NAME";
    /** Column index refering to the column name. */
    public static final String COLUMN_NAME_INDEX = "COLUMN_NAME";
    /** Column index refering to the remarks. */
    public static final String COMMENT_INDEX = "REMARKS";

    /** Map containing every fields remarks. */
    private static final Map<String, Map<String, String>> fieldsComments = new HashMap<String, Map<String, String>>();
    /** Map containing every tables remarks. */
    private static final Map<String, String> tablesComments = new HashMap<String, String>();

    /**
     * Stores every fields remarks into the dedicated map.
     * 
     * @throws SQLException
     *         If an error occured when trying to access the fields remarks.
     * @since 1.0
     * @version 1.0
     */
    private static void buildFieldsComments() throws SQLException {
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseUtils.buildConnection();
            resultSet = connection.getMetaData().getColumns(null, null, "%", null);
            while (resultSet.next()) {
                final String tableName = clean(resultSet.getString(TABLE_NAME_INDEX));
                final String comment = clean(resultSet.getString(COMMENT_INDEX));
                if (isNotBlank(comment)) {
                    if (!fieldsComments.containsKey(tableName)) {
                        fieldsComments.put(tableName, new HashMap<String, String>());
                    }
                    fieldsComments.get(tableName).put(clean(resultSet.getString(COLUMN_NAME_INDEX)), comment);
                }
            }
        } finally {
            DatabaseUtils.close(resultSet);
            DatabaseUtils.close(connection);
        }
    }

    /**
     * Stores every tables remarks into the dedicated map.
     * 
     * @throws SQLException
     *         If an error occured when trying to access the tables remarks.
     * @since 1.0
     * @version 1.0
     */
    private static void buildTablesComments() throws SQLException {
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseUtils.buildConnection();
            resultSet = connection.getMetaData().getTables(null, null, "%", null);
            while (resultSet.next()) {
                final String comment = clean(resultSet.getString(COMMENT_INDEX));
                if (isNotBlank(comment)) {
                    tablesComments.put(clean(resultSet.getString(TABLE_NAME_INDEX)), comment);
                }
            }
        } finally {
            DatabaseUtils.close(resultSet);
            DatabaseUtils.close(connection);
        }
    }

    /**
     * Cleans a string.
     * 
     * @param string
     *        The string to clean.
     * @return The cleaned string.
     * @since 1.0
     * @version 1.0
     */
    private static String clean(final String string) {
        return string.toLowerCase().trim().replaceAll("  +", " ");
    }

    /**
     * Returns whether a string is blank or not.
     * 
     * @param string
     *        The string to check.
     * @return {@code false} if the string is {@code null} or empty, {@code true} otherwise.
     * @since 1.0
     * @version 1.0
     */
    private static boolean isNotBlank(final String string) {
        return string != null && !string.isEmpty();
    }

    /**
     * 1-arg constructor.
     * 
     * @param delegate
     *        The ReverseEngineeringStrategy the super class needs.
     * @throws SQLException
     *         If an error occured when trying to access the fields remarks.
     * @since 1.0
     * @version 1.0
     */
    public RemarksRevengStrategy(final ReverseEngineeringStrategy delegate) throws SQLException {
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
     * 
     * @since 1.0
     * @version 1.0
     */
    @Override
    public String columnToPropertyName(final TableIdentifier table, final String column) {
        final String tableName = clean(table.getName());
        final String columnName = clean(column);
        if (fieldsComments.containsKey(tableName)) {
            final Map<String, String> map = fieldsComments.get(tableName);
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
     * 
     * @since 1.0
     * @version 1.0
     */
    @Override
    public String tableToClassName(final TableIdentifier tableIdentifier) {
        final String tableName = clean(tableIdentifier.getName());
        if (tablesComments.containsKey(tableName)) {
            return super.tableToClassName(new TableIdentifier(tablesComments.get(tableName)));
        }
        return super.tableToClassName(tableIdentifier);
    }

}
