package com.yurt.patterns.bridge;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseBridge {
    Connection connect() throws SQLException;

    void ensureTablesExist(Connection conn) throws SQLException;
}
