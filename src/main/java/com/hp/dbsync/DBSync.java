package com.hp.dbsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.dbsync.config.ConfigData;
import com.hp.dbsync.config.ConfigData.TableConfig;
import com.hp.dbsync.util.DBUtil;
import com.hp.dbsync.util.DBUtil.TableMeta;
import com.hp.dbsync.util.DateTimeUtil;

public class DBSync {
	private static final Logger logger = LoggerFactory.getLogger(DBSync.class);

	static Connection con_src;
	static Connection con_tgt;
	static Map<String, DBUtil.TableMeta> m_tableMeta = new HashMap<String, TableMeta>();
	static Map<String, PstSync> psts = new HashMap<String, PstSync>();

	static class PstSync {
		public PreparedStatement inst2Tmp;
		public PreparedStatement selFromSrc;
		public PreparedStatement delByTmp;
		public PreparedStatement instByTmp;
		public PreparedStatement selTs;

		public String inst2Tmp_sql;
		public String selFromSrc_sql;
		public String delByTmp_sql;
		public String instByTmp_sql;

		public Timestamp tsMin;
		public Timestamp tsMax;
	}

	static {
		init();
	}

	public static void init() {
		try {
			logger.info("init con_src...");
			con_src = DBUtil.getConnection(ConfigData.db.source.classname, ConfigData.db.source.url, ConfigData.db.source.username,
					ConfigData.db.source.password);
			con_src.setAutoCommit(false);

			logger.info("init con_tgt...");
			con_tgt = DBUtil.getConnection(ConfigData.db.target.classname, ConfigData.db.target.url, ConfigData.db.target.username,
					ConfigData.db.target.password);
			con_tgt.setAutoCommit(false);

			logger.info("init TableMeta...");
			for (TableConfig tc : ConfigData.tablesConfig) {
				logger.info("init TableMeta: " + tc.name);
				TableMeta tbMeta = DBUtil.getTableInfo(con_src, tc.name);
				m_tableMeta.put(tc.name, tbMeta);

				PstSync pstSync = new PstSync();
				psts.put(tc.name, pstSync);

				String sql = String.format("select min(update_dm) min_ts,max(update_dm) max_ts from %s", tc.name);
				pstSync.selTs = con_src.prepareStatement(sql);

				sql = String.format("insert /*+ direct*/ into tmp_%s values(%s)", tc.name, StringUtils.repeat("?", ",", tbMeta.columns.size()));
				pstSync.inst2Tmp = con_tgt.prepareStatement(sql);
				pstSync.inst2Tmp_sql = sql;

				sql = String.format("select top 2000 * from %s where %s>=? and %s<?", tc.name, tc.tsCol, tc.tsCol);
				pstSync.selFromSrc = con_src.prepareStatement(sql);
				pstSync.selFromSrc_sql = sql;

				String primaryKeys = StringUtils.join(tbMeta.primaryKeys.keySet(), ",");
				sql = String.format(String.format("delete /*+ direct*/ from %s where (%s) in (select %s from tmp_%s)", tc.name, primaryKeys,
						primaryKeys, tc.name));
				pstSync.delByTmp = con_tgt.prepareStatement(sql);
				pstSync.delByTmp_sql = sql;

				sql = String.format(String.format("insert /*+ direct*/ into %s select * from tmp_%s", tc.name, tc.name));
				pstSync.instByTmp = con_tgt.prepareStatement(sql);
				pstSync.instByTmp_sql = sql;

			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setTs() throws SQLException {
		for (String tablename : psts.keySet()) {
			PstSync pst = psts.get(tablename);
			if (pst.tsMax == null) {
				pst.tsMin = ConfigData.jobStartTs;
			} else {
				pst.tsMin = pst.tsMax;
			}

			ResultSet rs = DBUtil.getResultSet(pst.selTs);
			if (rs.next()) {
				pst.tsMax = rs.getTimestamp("max_ts");
			} else {
				pst.tsMax = DateTimeUtil.getMaxSqlTimestamp();
			}
			DBUtil.close(rs);

			logger.info(String.format("%s : tsMin:%s, tsMax:%s", tablename, pst.tsMin, pst.tsMax));
			pst.selFromSrc.setTimestamp(1, pst.tsMin);
			pst.selFromSrc.setTimestamp(2, pst.tsMax);
		}
	}

	public static void commit(PstSync pst) throws SQLException {
		// pst_del.executeBatch();

		logger.info("executeBatch: " + pst.inst2Tmp_sql);
		pst.inst2Tmp.executeBatch();

		logger.info("executeBatch: " + pst.delByTmp_sql);
		pst.delByTmp.executeUpdate();

		logger.info("executeBatch: " + pst.instByTmp_sql);
		pst.instByTmp.executeUpdate();

		con_tgt.commit();
	}

	public static void runSync() throws SQLException {
		logger.info("sync start...");

		setTs();
		for (String tablename : psts.keySet()) {
			long t1 = DateTimeUtil.curTimeMillis();
			final PstSync pst = psts.get(tablename);
			logger.info("refreshing table: " + tablename);
			logger.info("selFromSrc: " + pst.selFromSrc_sql);
			DBUtil.runQuery(pst.selFromSrc, new DBUtil.ResultReader() {
				public void processResult(ResultSet rs, ResultSetMetaData rsmd, long lineNum) {
					try {
						int columnCount = rsmd.getColumnCount();
						for (int i = 1; i <= columnCount; i++) {
							int columnType = rsmd.getColumnType(i);
							DBUtil.setPst(pst.inst2Tmp, rs, columnType, rsmd.getColumnTypeName(i), i);
						}
						// for(String k:die_link.primaryKeys.keySet()){
						// Column c = die_link.primaryKeys.get(k);
						// DBUtil.setPst(pst_del, rs, c.datatype,c.name, c.position);
						// }

						// pst_del.addBatch();
						pst.inst2Tmp.addBatch();

						if (lineNum % ConfigData.batchSize == 0) {
							commit(pst);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});

			commit(pst);

			long t2 = DateTimeUtil.curTimeMillis();
			DateTimeUtil.printDiffTimeOfms(t1, t2);
		}

		logger.info("sync end...");
	}

	public static void startTimer() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					runSync();
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}, 1, ConfigData.jobInterval, TimeUnit.SECONDS);
	}

	public static void main(String[] args) {
		try {
			startTimer();
			// runSync();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
