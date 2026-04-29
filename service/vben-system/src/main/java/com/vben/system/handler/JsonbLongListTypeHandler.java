package com.vben.system.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.List;

public class JsonbLongListTypeHandler extends BaseTypeHandler<List<Long>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType)
            throws SQLException {

        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");

        try {
            jsonObject.setValue(MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("jsonb转换失败", e);
        }

        ps.setObject(i, jsonObject);
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<Long> parse(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new SQLException("json解析失败", e);
        }
    }
}
