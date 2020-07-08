package com.sns.gr.testbase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBLibrary {

private static Connection connection = null;
	
	public static Connection getDBConnection() throws SQLException, ClassNotFoundException {
		if(connection != null) {
			return connection;
		}
		else {
			String dbUrl = "jdbc:mysql://localhost:3306/grdatabase";
			String username = "root";
			String password = "rootpwd";
				
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(dbUrl,username,password);
			
			return connection;
			
//			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
//		    String connectionURL = "jdbc:derby:grdatabase;";
//		    Class.forName(driver);
//            connection = DriverManager.getConnection(connectionURL);
//
//            return connection;
		}
	}
		
	public static List<Map<String, Object>> dbAction(String action, String query) throws ClassNotFoundException, SQLException {
		List<Map<String, Object>> result_list = new ArrayList<Map<String, Object>>();
		Statement stmt;
		connection = getDBConnection();
		stmt = connection.createStatement();
				
		if(action.equals("insert") || action.equals("update")) {
			stmt.executeUpdate(query);
		}
		else if(action.equals("fetch")) {
			ResultSet rs = stmt.executeQuery(query);
			result_list = resultSetToList(rs);
		}
		return result_list;
	}
		
	public static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
	    ResultSetMetaData md = rs.getMetaData();
	    int columns = md.getColumnCount();
	    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
	    while (rs.next()){
	        Map<String, Object> row = new HashMap<String, Object>(columns);
	        for(int i = 1; i <= columns; ++i){
	            row.put(md.getColumnName(i), rs.getObject(i));
	        }
	        rows.add(row);
	    }
	    return rows;
	}
 }
