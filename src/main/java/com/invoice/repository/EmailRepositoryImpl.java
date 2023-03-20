package com.invoice.repository;

import com.invoice.model.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@Slf4j
public class EmailRepositoryImpl {

    @Autowired
    @Qualifier("JdbcTemplate2")
    private JdbcTemplate jdbcTemplateSecondary;

    public long createEmail(Email email){
        try
        {
            String sql = "INSERT INTO email_queue(INV_ID,STS,CR_DT,BUYER_EMAIL) VALUES (?,'C',now(),?);";
            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, email.getId());
                ps.setString(2, email.getBuyerEmail());

                return ps;
            },generatedKeyHolder);

            long seqId = generatedKeyHolder.getKey().longValue();
            return seqId;
        }
        catch (Exception ex)
        {
            log.error("Exeption in EmailRepositoryImpl createEmail : {}",ex.getMessage());
            return 0;
        }
    }
}
