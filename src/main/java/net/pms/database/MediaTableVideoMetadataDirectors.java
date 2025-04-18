/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.database;

import com.google.gson.JsonArray;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.pms.media.video.metadata.ApiStringArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MediaTableVideoMetadataDirectors extends MediaTable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaTableVideoMetadataDirectors.class);
	public static final String TABLE_NAME = "VIDEO_METADATA_DIRECTORS";

	/**
	 * Table version must be increased every time a change is done to the table
	 * definition. Table upgrade SQL must also be added to
	 * {@link #upgradeTable(Connection, int)}
	 *
	 * Version notes:
	 * - 3: FILEID and TVSERIESID as BIGINT
	 */
	private static final int TABLE_VERSION = 3;

	/**
	 * COLUMNS NAMES
	 */
	private static final String COL_ID = "ID";
	private static final String COL_FILEID = MediaTableFiles.CHILD_ID;
	private static final String COL_TVSERIESID = MediaTableTVSeries.CHILD_ID;
	private static final String COL_DIRECTOR = "DIRECTOR";

	/**
	 * COLUMNS with table name
	 */
	public static final String TABLE_COL_FILEID = TABLE_NAME + "." + COL_FILEID;
	private static final String TABLE_COL_TVSERIESID = TABLE_NAME + "." + COL_TVSERIESID;
	public static final String TABLE_COL_DIRECTOR = TABLE_NAME + "." + COL_DIRECTOR;

	/**
	 * SQL Queries
	 */
	private static final String SQL_GET_DIRECTOR_FILEID = SELECT + TABLE_COL_DIRECTOR + FROM + TABLE_NAME + WHERE + TABLE_COL_FILEID + EQUAL + PARAMETER;
	private static final String SQL_GET_DIRECTOR_TVSERIESID = SELECT + TABLE_COL_DIRECTOR + FROM + TABLE_NAME + WHERE + TABLE_COL_TVSERIESID + EQUAL + PARAMETER;
	private static final String SQL_GET_ALL_FILEID = SELECT_ALL + FROM + TABLE_NAME + WHERE + TABLE_COL_FILEID + EQUAL + PARAMETER;
	private static final String SQL_GET_ALL_TVSERIESID = SELECT_ALL + FROM + TABLE_NAME + WHERE + TABLE_COL_TVSERIESID + EQUAL + PARAMETER;

	/**
	 * Checks and creates or upgrades the table as needed.
	 *
	 * @param connection the {@link Connection} to use
	 *
	 * @throws SQLException
	 */
	protected static void checkTable(final Connection connection) throws SQLException {
		if (tableExists(connection, TABLE_NAME)) {
			Integer version = MediaTableTablesVersions.getTableVersion(connection, TABLE_NAME);
			if (version != null) {
				if (version < TABLE_VERSION) {
					upgradeTable(connection, version);
				} else if (version > TABLE_VERSION) {
					LOGGER.warn(LOG_TABLE_NEWER_VERSION_DELETEDB, DATABASE_NAME, TABLE_NAME, DATABASE.getDatabaseFilename());
				}
			} else {
				LOGGER.warn(LOG_TABLE_UNKNOWN_VERSION_RECREATE, DATABASE_NAME, TABLE_NAME);
				dropTable(connection, TABLE_NAME);
				createTable(connection);
				MediaTableTablesVersions.setTableVersion(connection, TABLE_NAME, TABLE_VERSION);
			}
		} else {
			createTable(connection);
			MediaTableTablesVersions.setTableVersion(connection, TABLE_NAME, TABLE_VERSION);
		}
	}

	/**
	 * This method <strong>MUST</strong> be updated if the table definition are
	 * altered. The changes for each version in the form of
	 * <code>ALTER TABLE</code> must be implemented here.
	 *
	 * @param connection the {@link Connection} to use
	 * @param currentVersion the version to upgrade <strong>from</strong>
	 *
	 * @throws SQLException
	 */
	private static void upgradeTable(final Connection connection, final int currentVersion) throws SQLException {
		LOGGER.info(LOG_UPGRADING_TABLE, DATABASE_NAME, TABLE_NAME, currentVersion, TABLE_VERSION);
		for (int version = currentVersion; version < TABLE_VERSION; version++) {
			LOGGER.trace(LOG_UPGRADING_TABLE, DATABASE_NAME, TABLE_NAME, version, version + 1);
			switch (version) {
				case 1 -> {
					//index with all columns ??
					executeUpdate(connection, "DROP INDEX IF EXISTS FILENAME_DIRECTOR_TVSERIESID_IDX");
					executeUpdate(connection, ALTER_TABLE + TABLE_NAME + " ADD COLUMN IF NOT EXISTS " + COL_FILEID + " INTEGER");
					if (isColumnExist(connection, TABLE_NAME, "FILENAME")) {
						executeUpdate(connection, UPDATE + TABLE_NAME + SET + COL_FILEID + "=(SELECT " + MediaTableFiles.TABLE_COL_ID + FROM + MediaTableFiles.TABLE_NAME + WHERE + MediaTableFiles.TABLE_COL_FILENAME + " = " + TABLE_NAME + ".FILENAME) WHERE " + TABLE_NAME + ".FILENAME != ''");
						executeUpdate(connection, ALTER_TABLE + TABLE_NAME + " DROP COLUMN IF EXISTS FILENAME");
					}
					executeUpdate(connection, ALTER_TABLE + TABLE_NAME + " ALTER COLUMN IF EXISTS " + COL_TVSERIESID + " DROP DEFAULT");

					executeUpdate(connection, UPDATE + TABLE_NAME + SET + COL_FILEID + " = NULL WHERE " + TABLE_COL_FILEID + " = -1");
					executeUpdate(connection, UPDATE + TABLE_NAME + SET + COL_TVSERIESID + " = NULL WHERE " + TABLE_COL_TVSERIESID + " = -1");

					executeUpdate(connection, ALTER_TABLE + TABLE_NAME + " ADD CONSTRAINT " + TABLE_NAME + CONSTRAINT_SEPARATOR + COL_FILEID + FK_MARKER + FOREIGN_KEY + "(" + COL_FILEID + ")" + REFERENCES + MediaTableVideoMetadata.TABLE_NAME + "(" + MediaTableVideoMetadata.COL_FILEID + ") ON DELETE CASCADE");
					executeUpdate(connection, ALTER_TABLE + TABLE_NAME + " ADD CONSTRAINT " + TABLE_NAME + CONSTRAINT_SEPARATOR + COL_TVSERIESID + FK_MARKER + FOREIGN_KEY + "(" + COL_TVSERIESID + ")" + REFERENCES + MediaTableTVSeries.TABLE_NAME + "(" + MediaTableTVSeries.COL_ID + ") ON DELETE CASCADE");
				}
				case 2 -> {
					executeUpdate(connection, ALTER_TABLE + TABLE_NAME + ALTER_COLUMN + IF_EXISTS + COL_FILEID + BIGINT);
					executeUpdate(connection, ALTER_TABLE + TABLE_NAME + ALTER_COLUMN + IF_EXISTS + COL_TVSERIESID + BIGINT);
				}
				default -> {
					throw new IllegalStateException(getMessage(LOG_UPGRADING_TABLE_MISSING, DATABASE_NAME, TABLE_NAME, version, TABLE_VERSION));
				}
			}
		}
		MediaTableTablesVersions.setTableVersion(connection, TABLE_NAME, TABLE_VERSION);
	}

	private static void createTable(final Connection connection) throws SQLException {
		LOGGER.info(LOG_CREATING_TABLE, DATABASE_NAME, TABLE_NAME);
		execute(connection,
			CREATE_TABLE + TABLE_NAME + "(" +
				COL_ID            + IDENTITY          + PRIMARY_KEY + COMMA +
				COL_TVSERIESID    + BIGINT                          + COMMA +
				COL_FILEID        + BIGINT                          + COMMA +
				COL_DIRECTOR      + VARCHAR_1024      + NOT_NULL    + COMMA +
				CONSTRAINT + TABLE_NAME + CONSTRAINT_SEPARATOR + COL_FILEID + FK_MARKER + FOREIGN_KEY + "(" + COL_FILEID + ")" + REFERENCES + MediaTableVideoMetadata.REFERENCE_TABLE_COL_FILE_ID + ON_DELETE_CASCADE + COMMA +
				CONSTRAINT + TABLE_NAME + CONSTRAINT_SEPARATOR + COL_TVSERIESID + FK_MARKER + FOREIGN_KEY + "(" + COL_TVSERIESID + ")" + REFERENCES + MediaTableTVSeries.REFERENCE_TABLE_COL_ID + ON_DELETE_CASCADE +
			")"
		);
	}

	/**
	 * Sets a new row if it doesn't already exist.
	 *
	 * @param connection the db connection
	 * @param fileId
	 * @param directors
	 * @param tvSeriesID
	 */
	public static void set(final Connection connection, final Long fileId, final ApiStringArray directors, final Long tvSeriesID) {
		if (directors == null) {
			return;
		}
		final String sqlSelect;
		final String tableColumn;
		final long id;
		if (tvSeriesID != null) {
			sqlSelect = SQL_GET_ALL_TVSERIESID;
			tableColumn = COL_TVSERIESID;
			id = tvSeriesID;
		} else if (fileId != null) {
			sqlSelect = SQL_GET_ALL_FILEID;
			tableColumn = COL_FILEID;
			id = fileId;
		} else {
			return;
		}

		List<String> newDirectors = new ArrayList<>(directors);
		try {
			try (PreparedStatement ps = connection.prepareStatement(sqlSelect, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
				ps.setLong(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String director = rs.getString(COL_DIRECTOR);
						if (newDirectors.contains(director)) {
							LOGGER.trace("Record \"{}\" already exists {} {} {}", director, tableColumn, id);
							newDirectors.remove(director);
						} else {
							LOGGER.trace("Removing record \"{}\" for {} {}", director, tableColumn, id);
							rs.deleteRow();
						}
					}
					for (String actor : newDirectors) {
						rs.moveToInsertRow();
						rs.updateLong(tableColumn, id);
						rs.updateString(COL_DIRECTOR, actor);
						rs.insertRow();
						LOGGER.trace("Set new entry \"{}\" successfully in " + TABLE_NAME + " with {} {}", actor, tableColumn, id);
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(LOG_ERROR_WHILE_IN_FOR, DATABASE_NAME, "writing", TABLE_NAME, fileId, e.getMessage());
			LOGGER.trace("", e);
		}
	}

	public static ApiStringArray getDirectorsForFile(final Connection connection, final long fileId) {
		ApiStringArray result = new ApiStringArray();
		try {
			try (PreparedStatement ps = connection.prepareStatement(SQL_GET_DIRECTOR_FILEID)) {
				ps.setLong(1, fileId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error in " + TABLE_NAME + " for \"{}\": {}", fileId, e.getMessage());
			LOGGER.trace("", e);
		}
		return result;
	}

	public static JsonArray getJsonArrayForFile(final Connection connection, final long fileId) {
		JsonArray result = new JsonArray();
		try {
			try (PreparedStatement ps = connection.prepareStatement(SQL_GET_DIRECTOR_FILEID)) {
				ps.setLong(1, fileId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error in " + TABLE_NAME + " for \"{}\": {}", fileId, e.getMessage());
			LOGGER.trace("", e);
		}
		return result;
	}

	public static ApiStringArray getDirectorsForTvSeries(final Connection connection, final long tvSerieId) {
		ApiStringArray result = new ApiStringArray();
		try {
			try (PreparedStatement ps = connection.prepareStatement(SQL_GET_DIRECTOR_TVSERIESID)) {
				ps.setLong(1, tvSerieId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error in " + TABLE_NAME + " for TV serie ID \"{}\": {}", tvSerieId, e.getMessage());
			LOGGER.trace("", e);
		}
		return result;
	}

	public static JsonArray getJsonArrayForTvSeries(final Connection connection, final long tvSerieId) {
		JsonArray result = new JsonArray();
		try {
			try (PreparedStatement ps = connection.prepareStatement(SQL_GET_DIRECTOR_TVSERIESID)) {
				ps.setLong(1, tvSerieId);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						result.add(rs.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error in " + TABLE_NAME + " for \"{}\": {}", tvSerieId, e.getMessage());
			LOGGER.trace("", e);
		}
		return result;
	}

}
