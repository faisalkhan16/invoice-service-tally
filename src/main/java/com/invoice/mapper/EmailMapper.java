package com.invoice.mapper;

import com.invoice.model.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class EmailMapper implements RowMapper<Email> {
    @Override
    public Email mapRow(ResultSet resultSet, int i) throws SQLException {
        Email email = new Email();
        try {
            email.setSeqId(resultSet.getLong("SEQ_ID"));
            email.setId(resultSet.getString("INV_ID"));
            email.setBuyerEmail(resultSet.getString("BUYER_EMAIL"));
        }catch (Exception ex){
            log.error("Exception in EmailMapper {}",ex.getMessage());
        }
        return email;
    }
}
