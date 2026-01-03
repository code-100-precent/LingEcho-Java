package com.lingecho.common.core.search.core;

import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JDBC存储目录实现
 * 将Lucene索引数据存储在关系数据库中，支持跨平台和分布式部署
 *
 * @author HibiscusSearch Team
 * @version 1.0.0
 */
public class JdbcDirectory extends Directory {
    private final String url;
    private final String user;
    private final String password;
    private final String table;
    private final LockFactory lockFactory = new SingleInstanceLockFactory();
    private final Set<String> pendingDeletions = new HashSet<>();
    
    // 改进的连接管理
    private volatile Connection connection;
    private final ReentrantLock connectionLock = new ReentrantLock();
    private volatile boolean tableInitialized = false;
    private final ReentrantLock tableInitLock = new ReentrantLock();

    /**
     * 构造函数
     *
     * @param url JDBC连接URL
     * @param user 数据库用户名
     * @param password 数据库密码
     * @param table 存储索引数据的表名
     */
    public JdbcDirectory(String url, String user, String password, String table) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.table = table;
        // 延迟初始化表结构，不在构造函数中执行
    }

        /**
     * 获取数据库连接，改进的连接管理
     */
    private Connection getConnection() throws SQLException {
        return getConnectionWithRetry(3); // 最多重试3次
    }
    
    /**
     * 带重试机制的连接获取
     */
    private Connection getConnectionWithRetry(int maxRetries) throws SQLException {
        int attempts = 0;
        SQLException lastException = null;
        
        while (attempts < maxRetries) {
            try {
                // 双重检查锁定模式
                if (connection == null || connection.isClosed()) {
                    connectionLock.lock();
                    try {
                        if (connection == null || connection.isClosed()) {
                            // 创建新连接
                            connection = createNewConnection();
                            // 设置连接属性
                            connection.setAutoCommit(true);
                            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                        }
                    } finally {
                        connectionLock.unlock();
                    }
                }
                
                // 验证连接是否有效
                if (!isConnectionValid(connection)) {
                    connectionLock.lock();
                    try {
                        if (!isConnectionValid(connection)) {
                            closeConnection(connection);
                            connection = createNewConnection();
                            connection.setAutoCommit(true);
                            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                        }
                    } finally {
                        connectionLock.unlock();
                    }
                }
                
                // 测试连接是否真的可用
                try (PreparedStatement testPs = connection.prepareStatement("SELECT 1")) {
                    testPs.executeQuery();
                }
                
                return connection;
                
            } catch (SQLException e) {
                lastException = e;
                attempts++;
                
                // 关闭可能损坏的连接
                if (connection != null) {
                    closeConnection(connection);
                    connection = null;
                }
                
                // 如果不是最后一次尝试，等待一段时间再重试
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempts); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("连接获取被中断", ie);
                    }
                }
            }
        }
        
        throw new SQLException("无法获取有效的数据库连接，已重试 " + maxRetries + " 次", lastException);
    }
    
    /**
     * 创建新的数据库连接
     */
    private Connection createNewConnection() throws SQLException {
        try {
            if (user != null && !user.isEmpty()) {
                return DriverManager.getConnection(url, user, password);
            } else {
                return DriverManager.getConnection(url);
            }
        } catch (SQLException e) {
            throw new SQLException("无法创建数据库连接: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证连接是否有效
     */
    private boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        try {
            return conn.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 安全关闭连接
     */
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // 忽略关闭时的异常
            }
        }
    }

    /**
     * 确保存储表存在，延迟初始化
     */
    private void ensureTableInitialized() {
        if (tableInitialized) {
            return;
        }
        
        tableInitLock.lock();
        try {
            if (tableInitialized) {
                return;
            }
            
            String sql = "CREATE TABLE IF NOT EXISTS " + table +
                    " (name VARCHAR(255) PRIMARY KEY, bytes BLOB, last_modified BIGINT, length BIGINT)";
            
            try (Connection c = getConnection(); 
                 Statement s = c.createStatement()) {
                s.execute(sql);
                tableInitialized = true;
            } catch (SQLException e) {
                throw new RuntimeException("初始化JdbcDirectory表失败: " + table, e);
            }
        } finally {
            tableInitLock.unlock();
        }
    }

    @Override
    public String[] listAll() throws IOException {
        ensureTableInitialized();
        
        String sql = "SELECT name FROM " + table;
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString(1));
            }
            return names.toArray(new String[0]);
        } catch (SQLException e) {
            throw new IOException("列出文件失败", e);
        }
    }

    @Override
    public void deleteFile(String name) throws IOException {
        ensureTableInitialized();
        
        String sql = "DELETE FROM " + table + " WHERE name = ?";
        try (Connection c = getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new NoSuchFileException(name);
            }
            pendingDeletions.remove(name);
        } catch (SQLException e) {
            throw new IOException("删除文件失败: " + name, e);
        }
    }

    @Override
    public long fileLength(String name) throws IOException {
        ensureTableInitialized();
        
        String sql = "SELECT length FROM " + table + " WHERE name = ?";
        try (Connection c = getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NoSuchFileException(name);
                }
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IOException("获取文件长度失败: " + name, e);
        }
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        ensureTableInitialized();
        return new JdbcIndexOutput(name);
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        ensureTableInitialized();
        String tempName = prefix + "_" + System.currentTimeMillis() + "_" + suffix;
        return new JdbcIndexOutput(tempName);
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        // 数据库事务保证持久化，这里不需要额外操作
    }

    @Override
    public void syncMetaData() throws IOException {
        // 数据库事务保证元数据持久化
    }

    @Override
    public void rename(String source, String dest) throws IOException {
        ensureTableInitialized();
        
        try (IndexInput in = openInput(source, IOContext.DEFAULT);
             IndexOutput out = createOutput(dest, IOContext.DEFAULT)) {
            byte[] buf = new byte[8192];
            long remaining = in.length();
            long pos = 0;
            while (pos < remaining) {
                int toRead = (int) Math.min(buf.length, remaining - pos);
                in.readBytes(buf, 0, toRead);
                out.writeBytes(buf, 0, toRead);
                pos += toRead;
            }
        }
        deleteFile(source);
    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        ensureTableInitialized();
        
        String sql = "SELECT bytes, length FROM " + table + " WHERE name = ?";
        try (Connection c = getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new NoSuchFileException(name);
                }
                byte[] data = rs.getBytes(1);
                long length = rs.getLong(2);
                return new ByteArrayIndexInput(name, data, length);
            }
        } catch (SQLException e) {
            throw new IOException("打开文件失败: " + name, e);
        }
    }

    @Override
    public Lock obtainLock(String name) throws IOException {
        return lockFactory.obtainLock(this, name);
    }

    @Override
    public void close() throws IOException {
        // 清理待删除文件
        pendingDeletions.clear();
        
        // 关闭数据库连接
        connectionLock.lock();
        try {
            closeConnection(connection);
            connection = null;
        } finally {
            connectionLock.unlock();
        }
    }

    @Override
    public Set<String> getPendingDeletions() throws IOException {
        return new HashSet<>(pendingDeletions);
    }

    /**
     * 内部类：写入缓冲区并持久化到数据库
     */
    private class JdbcIndexOutput extends IndexOutput {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream(1 << 20); // 1MB初始缓冲区
        private long pointer = 0L;
        private final String name;
        private boolean closed = false;

        protected JdbcIndexOutput(String name) {
            super("JdbcIndexOutput(" + table + ")", name);
            this.name = name;
        }

        @Override
        public void writeByte(byte b) throws IOException {
            if (closed) {
                throw new IOException("IndexOutput已关闭");
            }
            baos.write(b);
            pointer++;
        }

        @Override
        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            if (closed) {
                throw new IOException("IndexOutput已关闭");
            }
            baos.write(b, offset, length);
            pointer += length;
        }

        @Override
        public void writeBytes(byte[] b, int length) throws IOException {
            writeBytes(b, 0, length);
        }

        @Override
        public long getFilePointer() {
            return pointer;
        }

        @Override
        public long getChecksum() throws IOException {
            // 简单实现，返回0
            return 0;
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;

            byte[] data = baos.toByteArray();
            
            // 使用重试机制持久化数据
            persistDataWithRetry(name, data, 3);
        }
        
        /**
         * 带重试机制的数据持久化
         */
        private void persistDataWithRetry(String name, byte[] data, int maxRetries) throws IOException {
            int attempts = 0;
            IOException lastException = null;
            
            while (attempts < maxRetries) {
                try {
                    // 尝试MySQL语法
                    try (Connection c = getConnection()) {
                        String upsert = "INSERT INTO " + table + " (name, bytes, last_modified, length) VALUES (?,?,?,?) " +
                                "ON DUPLICATE KEY UPDATE bytes=VALUES(bytes), last_modified=VALUES(last_modified), length=VALUES(length)";
                        try (PreparedStatement ps = c.prepareStatement(upsert)) {
                            ps.setString(1, name);
                            ps.setBytes(2, data);
                            ps.setLong(3, System.currentTimeMillis());
                            ps.setLong(4, data.length);
                            ps.executeUpdate();
                            return;
                        }
                    } catch (SQLException e) {
                        // MySQL语法失败，继续尝试其他数据库
                    }

                    // 尝试PostgreSQL语法
                    try (Connection c = getConnection()) {
                        String postgresUpsert = "INSERT INTO " + table + " (name, bytes, last_modified, length) VALUES (?,?,?,?) " +
                                "ON CONFLICT(name) DO UPDATE SET bytes=EXCLUDED.bytes, last_modified=EXCLUDED.last_modified, length=EXCLUDED.length";
                        try (PreparedStatement ps = c.prepareStatement(postgresUpsert)) {
                            ps.setString(1, name);
                            ps.setBytes(2, data);
                            ps.setLong(3, System.currentTimeMillis());
                            ps.setLong(4, data.length);
                            ps.executeUpdate();
                            return;
                        }
                    } catch (SQLException e) {
                        // PostgreSQL语法失败，继续尝试其他数据库
                    }

                    // 如果都失败，使用简单的INSERT
                    try (Connection c = getConnection()) {
                        // 先删除旧数据
                        String deleteSql = "DELETE FROM " + table + " WHERE name = ?";
                        try (PreparedStatement deletePs = c.prepareStatement(deleteSql)) {
                            deletePs.setString(1, name);
                            deletePs.executeUpdate();
                        }

                        // 插入新数据
                        String insertSql = "INSERT INTO " + table + " (name, bytes, last_modified, length) VALUES (?,?,?,?)";
                        try (PreparedStatement insertPs = c.prepareStatement(insertSql)) {
                            insertPs.setString(1, name);
                            insertPs.setBytes(2, data);
                            insertPs.setLong(3, System.currentTimeMillis());
                            insertPs.setLong(4, data.length);
                            insertPs.executeUpdate();
                        }
                        return; // 成功插入，退出重试循环
                    } catch (SQLException e) {
                        lastException = new IOException("持久化数据到数据库失败: " + name, e);
                    }
                    
                } catch (Exception e) {
                    lastException = new IOException("持久化数据到数据库失败: " + name, e);
                }
                
                attempts++;
                
                // 如果不是最后一次尝试，等待一段时间再重试
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempts); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("数据持久化被中断: " + name, ie);
                    }
                }
            }
            
            // 所有重试都失败了
            throw new IOException("持久化数据到数据库失败，已重试 " + maxRetries + " 次: " + name, lastException);
        }
    }

    /**
     * 内部类：用于从数据库读取文件内容
     */
    private static class ByteArrayIndexInput extends IndexInput {
        private final byte[] data;
        private final long length;
        private long pos = 0;

        protected ByteArrayIndexInput(String resourceDesc, byte[] data, long length) {
            super(resourceDesc);
            this.data = data;
            this.length = length;
        }

        @Override
        public void close() {
            // 不需要关闭
        }

        @Override
        public long getFilePointer() {
            return pos;
        }

        @Override
        public void seek(long pos) {
            this.pos = pos;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public byte readByte() {
            return data[(int) pos++];
        }

        @Override
        public void readBytes(byte[] b, int offset, int len) {
            System.arraycopy(data, (int) pos, b, offset, len);
            pos += len;
        }

        @Override
        public IndexInput slice(String sliceDescription, long offset, long length) {
            return new ByteArrayIndexInput(sliceDescription, data, (int) offset, (int) length);
        }

        private ByteArrayIndexInput(String sliceDescription, byte[] data, int offset, int length) {
            super(sliceDescription);
            this.data = data;
            this.pos = offset;
            this.length = length;
        }
    }
}
