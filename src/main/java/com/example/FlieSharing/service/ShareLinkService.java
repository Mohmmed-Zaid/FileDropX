package com.example.FlieSharing.service;

import com.example.FlieSharing.model.ShareLinkModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShareLinkService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ShareLinkModel> rowMapper = new RowMapper<ShareLinkModel>() {
        @Override
        public ShareLinkModel mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareLinkModel shareLink = new ShareLinkModel();
            shareLink.setId(rs.getLong("id"));
            shareLink.setFileId(rs.getLong("file_id"));
            shareLink.setUniqueId(rs.getString("unique_id"));
            shareLink.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            shareLink.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
            shareLink.setDownloadCount(rs.getInt("download_count"));
            return shareLink;
        }
    };

    public void createShareLink(ShareLinkModel shareLink) {
        String sql = "INSERT INTO share_links (file_id, unique_id, created_at, expires_at, download_count) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                shareLink.getFileId(),
                shareLink.getUniqueId(),
                shareLink.getCreatedAt(),
                shareLink.getExpiresAt(),
                shareLink.getDownloadCount());
    }

    public ShareLinkModel getShareLinkByUniqueId(String uniqueId) {
        String sql = "SELECT * FROM share_links WHERE unique_id = ?";
        List<ShareLinkModel> shareLinks = jdbcTemplate.query(sql, rowMapper, uniqueId);
        return shareLinks.isEmpty() ? null : shareLinks.get(0);
    }

    public void incrementDownloadCount(String uniqueId) {
        String sql = "UPDATE share_links SET download_count = download_count + 1 WHERE unique_id = ?";
        jdbcTemplate.update(sql, uniqueId);
    }

    public void deleteExpiredLinks() {
        String sql = "DELETE FROM share_links WHERE expires_at < ?";
        jdbcTemplate.update(sql, LocalDateTime.now());
    }
}