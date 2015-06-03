package com.hp.dbsync.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vertica.jdbc.SPreparedStatement;

/**
 * @author xul
 * 
 */
public class DBUtil {
	private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

	/**
	 * import data from file, ignore lines started with "#" eg. importData(con,"c:\\SGPTF\\prefix_define.ref"
	 * ,"lot_prefix;wks_product_type;Elt_route_name;wks_product_number:int" ,"facility:SGPTF"); prefix_define.ref: #PREFIX MAP-PRODUCT-TYPE
	 * ELT-PRODUCT-TYPE hgp margarit margarita hgk margarit margarita 3333 *
	 * 
	 * @param options
	 *            "uppercase": uppercase values
	 * @param delimiter
	 *            seperate values
	 * @param startLineNum
	 * @param fileName
	 * @param columns
	 *            : columns in file in sequence
	 * @param otherColumns
	 *            : columns not in file, should specify default value
	 */
	public static void importData(final Connection conn, final String options, final String delimiter, final int startLineNum, final String fileName,
			final String tableName, final String columnName, final String otherColumn) {
		final List lines = new ArrayList();
		FileUtil.processFileContent(fileName, "utf-8", new FileUtil.LineReader() {
			public void processLine(String line, int lineNum) {
				if (lineNum < startLineNum) {
					return;
				}
				if (line.startsWith("#") || StringUtils.isBlank(line)) {
					return;
				}
				if (lines.contains(line)) {
					return;
				}
				lines.add(line);
				boolean uppercase = false;
				if (options != null && options.indexOf("uppercase") != -1) {
					uppercase = true;
				}
				// line = line.replaceAll("\\s+", ";");
				String delimiter2 = delimiter;
				if (delimiter2 == null) {
					delimiter2 = "\\s+";
				}
				String[] columnNameSp = columnName.split(";");
				// System.out.println(line);
				String[] columnValueSp = line.split(delimiter2);
				// System.out.println(ArrayUtils.toString(columnValueSp));
				String[] otherColumnSp = otherColumn.split(";");
				String sql = "insert into " + tableName + "(";
				String values = " values(";
				int columnNum = Math.min(columnValueSp.length, columnNameSp.length);
				for (int i = 0; i < columnNum; i++) {
					if (i != 0) {
						sql += ",";
						values += ",";
					}
					String v = columnValueSp[i].trim();
					if (uppercase) {
						v = v.toUpperCase();
					}
					if (columnNameSp[i].split(":").length > 1 && columnNameSp[i].split(":")[1].equalsIgnoreCase("int")) {
						// number type
						sql += columnNameSp[i].split(":")[0].trim();
						values += v;
					} else {
						// string type
						sql += columnNameSp[i].trim();
						values += "'" + v + "'";
					}
				}
				for (int i = 0; i < otherColumnSp.length; i++) {
					sql += "," + otherColumnSp[i].split(":")[0].trim();
					values += "," + "'" + otherColumnSp[i].split(":")[1].trim() + "'";
				}
				sql = sql + ")" + values + ")";
				System.out.println(sql);
				try {
					runInsertSql(conn, sql);
				} catch (Exception e) {
					System.out.println(sql);
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});
	}

	/**
	 * DBUtil.getConnection("COM.ibm.db2.jdbc.app.DB2Driver","jdbc:db2:ehrdb", "ehr","xxx")
	 */
	public static Connection getConnection(String fullClass, String url, String user, String password) throws Exception {
		Class.forName(fullClass);
		Connection con = DriverManager.getConnection(url, user, password);
		return con;
	}

	public static void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void close(Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class TableMeta {
		public String name;
		public Map<String, ColumnMeta> columns;
		public Map<String, ColumnMeta> primaryKeys;
	}

	public static class ColumnMeta {
		public String name;
		public int datatype;
		public int position;
		public boolean isPrimaryKey;
	}

	/**
	 * 
	 * @param con
	 * @param tableName
	 * @return Map "columns","primeryKeys" Map, DatabaseMetaData,getColumns()
	 * @throws Exception
	 */
	public static TableMeta getTableInfo(Connection con, String tableName) throws Exception {
		if (tableName == null || tableName.trim().equals("")) {
			return null;
		}
		DatabaseMetaData md = con.getMetaData();
		ResultSet rs = null;

		rs = md.getTables(null, null, tableName, new String[] { "TABLE" });
		Map[] tableMaps = DBUtil.getRowMaps(rs);
		DBUtil.close(rs);

		if (tableMaps == null || tableMaps.length != 1) {
			throw new Exception("table(" + tableName + ") not exist!");
		}

		rs = md.getColumns(null, null, tableName, null);
		Map<String, String>[] columnMaps = DBUtil.getRowMaps(rs);
		// PrintUtil.printObject(columnMaps);
		DBUtil.close(rs);

		TableMeta tb = new TableMeta();
		tb.name = tableName;
		tb.columns = new HashMap<String, ColumnMeta>();
		tb.primaryKeys = new HashMap<String, ColumnMeta>();

		for (int i = 0; i < columnMaps.length; i++) {
			Map<String, String> c = columnMaps[i];
			ColumnMeta col = new ColumnMeta();
			col.datatype = Integer.parseInt(c.get("data_type"));
			col.name = c.get("column_name");
			col.position = Integer.parseInt(c.get("ordinal_position"));
			tb.columns.put(col.name, col);
		}

		rs = md.getPrimaryKeys(null, null, tableName);
		Map<String, String>[] primeryKeyMaps = DBUtil.getRowMaps(rs);
		// PrintUtil.printObject(primeryKeyMaps);
		DBUtil.close(rs);

		if (primeryKeyMaps == null || primeryKeyMaps.length == 0) {
			throw new RuntimeException("there is no primary key of table tableName!");
		}

		for (int i = 0; i < primeryKeyMaps.length; i++) {
			Map<String, String> c = primeryKeyMaps[i];
			ColumnMeta col = new ColumnMeta();
			col.name = c.get("column_name");
			col.datatype = tb.columns.get(col.name).datatype;
			col.position = Integer.parseInt(c.get("key_seq"));
			tb.primaryKeys.put(col.name, col);
			tb.columns.get(col.name).isPrimaryKey = true;
		}
		return tb;
	}

	public static Map[] getRowMaps(Connection con, String sql, Object... args) {
		logger.info("sql:" + sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement(sql);
			if (args.length > 0) {
				setVariable(pstmt, args);
			}
			rs = pstmt.executeQuery();
			Map[] ret = getRowMaps(rs);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.toString());
			throw new RuntimeException(e);
		} finally {
			close(rs);
			close(pstmt);
		}
	}

	public static void setVariable(PreparedStatement pstmt, Object... args) {
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Integer) {
					pstmt.setInt(i + 1, (Integer) args[i]);
				} else if (args[i] instanceof Long) {
					pstmt.setLong(i + 1, (Long) args[i]);
				} else if (args[i] instanceof Double) {
					pstmt.setDouble(i + 1, (Double) args[i]);
				} else if (args[i] instanceof Float) {
					pstmt.setFloat(i + 1, (Float) args[i]);
				} else if (args[i] instanceof String) {
					pstmt.setString(i + 1, args[i].toString());
				} else if (args[i] instanceof Date) {
					pstmt.setDate(i + 1, (Date) args[i]);
				}
			}
		} catch (SQLException e) {
			// e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Map[] getRowMaps(ResultSet rs) throws Exception {
		if (rs == null) {
			return null;
		}
		ArrayList al = new ArrayList();
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		while (rs.next()) {
			Map ht = new HashMap();
			for (int i = 1; i <= numberOfColumns; i++) {
				ht.put(rsmd.getColumnName(i).trim().toLowerCase(), rs.getString(i) == null ? null : rs.getString(i).trim());
			}
			al.add(ht);
		}
		return (Map[]) al.toArray(new Map[al.size()]);
	}

	public static ResultSet getResultSet(PreparedStatement pst) {
		ResultSet rs = null;
		try {
			rs = pst.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			throw new RuntimeException(e);
		} 
		return rs;
	}

	public static String[][] getRow(Connection con, String sql, Object... args) {
		logger.info("sql:" + sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement(sql);
			if (args.length > 0) {
				setVariable(pstmt, args);
			}
			rs = pstmt.executeQuery();
			String[][] ret = getRow(rs);
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.toString());
			throw new RuntimeException(e);
		} finally {
			close(rs);
			close(pstmt);
		}
	}

	public static String[][] getRow(ResultSet rs) throws SQLException {
		if (rs == null) {
			return null;
		}
		ArrayList al = new ArrayList();
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		while (rs.next()) {
			String[] columns = new String[numberOfColumns];
			Map ht = new HashMap();
			for (int i = 1; i <= numberOfColumns; i++) {
				columns[i - 1] = rs.getString(i);
			}
			al.add(columns);
		}
		String[][] ret = (String[][]) al.toArray(new String[0][0]);
		return ret;
	}

	public static void setPst(PreparedStatement pst, ResultSet rs, int columnType, String columnName, int pos) {
		try {
			if (columnType == java.sql.Types.INTEGER) {
				pst.setInt(pos, rs.getInt(pos));
			} else if (columnType == java.sql.Types.SMALLINT) {
				pst.setInt(pos, rs.getInt(pos));
			} else if (columnType == java.sql.Types.DOUBLE) {
				pst.setDouble(pos, rs.getDouble(pos));
			} else if (columnType == java.sql.Types.FLOAT) {
				pst.setFloat(pos, rs.getFloat(pos));
			} else if (columnType == java.sql.Types.BOOLEAN) {
				pst.setBoolean(pos, rs.getBoolean(pos));
			} else if (columnType == java.sql.Types.CHAR) {
				pst.setString(pos, rs.getString(pos));
			} else if (columnType == java.sql.Types.VARCHAR) {
				pst.setString(pos, rs.getString(pos));
			} else if (columnType == java.sql.Types.DATE) {
				pst.setDate(pos, rs.getDate(pos));
			} else if (columnType == java.sql.Types.TIMESTAMP) {
				pst.setTimestamp(pos, rs.getTimestamp(pos));
			} else {
				throw new RuntimeException("no support column type(" + columnType + "):" + columnName);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void runQuery(PreparedStatement pst, ResultReader rr) {
		ResultSet rs = null;
		long lineNum = 0;
		try {
			// if (args.length > 0) {
			// setVariable(pstmt, args);
			// }
			rs = pst.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				rr.processResult(rs, rsmd, ++lineNum);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
			throw new RuntimeException(e);
		} finally {
			close(rs);
		}
	}

	public static void runQuery(Connection con, String sql, ResultReader rr) {
		logger.info("sql:" + sql);
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement(sql);
			runQuery(pst, rr);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(pst);
		}
	}

	public interface ResultReader {
		void processResult(ResultSet rs, ResultSetMetaData rsmd, long lineNum);
	}

	public static int[] executeBatch(PreparedStatement pst) throws SQLException {
		logger.info("executeBatch: " + ((SPreparedStatement) pst).toString());
		int[] ret = pst.executeBatch();
		pst.clearBatch();
		return ret;
	}

	public static int executeUpdate(PreparedStatement pst) throws SQLException {
		logger.info("executeUpdate: " + pst);
		int num = pst.executeUpdate();
		return num;
	}

	public static int runUpdateSql(Connection con, String sql, Object... args) throws SQLException {
		logger.info("sql:" + sql);
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(sql);
			if (args.length > 0) {
				setVariable(pstmt, args);
			}
			int num = pstmt.executeUpdate();
			return num;
		} finally {
			close(pstmt);
		}
	}

	public static int runInsertSql(Connection con, String sql, Object... args) throws SQLException {
		logger.info("sql:" + sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement(sql);
			if (args.length > 0) {
				setVariable(pstmt, args);
			}
			int num = pstmt.executeUpdate();
			return num;
		} finally {
			close(pstmt);
		}
	}

	/**
	 * Map ret = DBUtil.callProcedure(con, "proc_we_corpdetailbymonth", "CHAR:1001,CHAR:OUT:title"); System.out.println(ret.get("title"));
	 * System.out.println(ret.get("procRet")); Map[] maps=(Map[])ret.get("rsMaps"); for(int i=0;i <maps.length;i++){ EhrUtil.printMap(maps[i]); }
	 * INTEGER,CHAR,DOUBLE,DATE,TIMESTAMP:OUT
	 * 
	 * @param con
	 * @param procedure
	 * @param params
	 * @return @throws Exception
	 */
	public static Map callProcedure(Connection con, String procedure, String params) throws Exception {
		logger.info("procedure:" + procedure + ",param(" + params + ")");
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			String[] ps = params.split(",");
			procedure = " { ?=call " + procedure + "(?";
			for (int i = 1; i < ps.length; i++) {
				procedure += ",?";
			}
			procedure += ")}";
			cs = con.prepareCall(procedure);
			cs.registerOutParameter(1, getSqlType("INTEGER"));
			for (int i = 0; i < ps.length; i++) {
				String[] p = ps[i].split(":");
				if ("OUT".equals(p[1])) {
					cs.registerOutParameter(i + 2, getSqlType(p[0]));
				} else {
					setCsParam(cs, i + 2, p[1], p[0]);
				}
			}
			cs.execute();
			Map map = new HashMap();
			map.put("procRet", getCsParam(cs, 1, "INTEGER"));
			for (int i = 0; i < ps.length; i++) {
				String[] p = ps[i].split(":");
				if ("OUT".equals(p[1]) && p.length > 2) {
					map.put(p[2], getCsParam(cs, i + 2, p[0]));
				}
			}
			rs = cs.getResultSet();
			map.put("rs", rs);
			if (rs != null) {
				Map[] rsRowMaps = getRowMaps(rs);
				map.put("rsMaps", rsRowMaps);
			}
			/*
			 * do{ rs=cs.getResultSet(); }while(cs.getMoreResults());
			 */
			return map;
		} finally {
			close(rs);
			close(cs);
		}
	}

	/**
	 * 
	 * @param con
	 * @param procedure
	 * @return @throws Exception
	 */
	public static Map callProcedure(Connection con, String procedure) throws Exception {
		logger.info("procedure:" + procedure);
		procedure = " { ?=call " + procedure + "() } ";
		CallableStatement cs = con.prepareCall(procedure);
		ResultSet rs = null;
		cs.execute();
		rs = cs.getResultSet();
		Map map = new HashMap();
		map.put("procRet", getCsParam(cs, 1, "INTEGER"));
		if (rs != null) {
			Map[] rsRowMaps = getRowMaps(rs);
			map.put("rsMaps", rsRowMaps);
		}
		rs.close();
		cs.close();
		return map;
	}

	public static int setCsParam(CallableStatement cs, int index, String value, String type) throws Exception {
		if ("INTEGER".equals(type)) {
			cs.setInt(index, Integer.parseInt(value));
		} else if ("CHAR".equals(type)) {
			cs.setString(index, value);
		} else if ("DOUBLE".equals(type)) {
			cs.setDouble(index, Double.parseDouble(value));
		} else if ("DATE".equals(type)) {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd");
			cs.setDate(index, (java.sql.Date) formatDate.parse(value));
		} else if ("TIMESTAMP".equals(type)) {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHHmmss");
			cs.setTimestamp(index, (java.sql.Timestamp) formatDate.parse(value));
		}
		return 1;
	}

	public static String getCsParam(CallableStatement cs, int index, String type) throws Exception {
		String t = "";
		if ("INTEGER".equals(type)) {
			t = String.valueOf(cs.getInt(index));
		} else if ("CHAR".equals(type)) {
			t = String.valueOf(cs.getString(index));
		} else if ("DOUBLE".equals(type)) {
			t = String.valueOf(cs.getDouble(index));
		} else if ("DATE".equals(type)) {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd");
			t = String.valueOf(formatDate.format(cs.getDate(index)));
		} else if ("TIMESTAMP".equals(type)) {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHHmmss");
			t = String.valueOf(formatDate.format(cs.getTimestamp(index)));
		}
		return t;
	}

	public static int getSqlType(String type) throws Exception {
		int t = Types.CHAR;
		if ("INTEGER".equals(type)) {
			t = Types.INTEGER;
		} else if ("CHAR".equals(type)) {
			t = Types.CHAR;
		} else if ("DOUBLE".equals(type)) {
			t = Types.DOUBLE;
		} else if ("DATE".equals(type)) {
			t = Types.DATE;
		} else if ("TIMESTAMP".equals(type)) {
			t = Types.TIMESTAMP;
		}
		return t;
	}

}