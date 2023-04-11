package com.invoice.repository;

import com.invoice.mapper.EmailMapper;
import com.invoice.model.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@Slf4j
public class EmailRepositoryImpl {

    @Autowired
    @Qualifier("JdbcTemplate2")
    private JdbcTemplate jdbcTemplateSecondary;

    public List<Email> getPendingEmails(){
        try
        {
            String sql = "SELECT SEQ_ID,INV_ID,BUYER_EMAIL FROM email_queue where STS = 'C';";
            return jdbcTemplateSecondary.query(sql, new EmailMapper());

        }
        catch (Exception ex)
        {
            log.error("Exeption in EmailRepositoryImpl getPendingEmails : {}",ex.getMessage());
            return null;
        }
    }

    public void updateEmailsStatus(String status,long seqID){
        try
        {
            String sql = "UPDATE email_queue SET STS = ?, UPD_DT = now() WHERE SEQ_ID = ?;";

            jdbcTemplateSecondary.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, status);
                ps.setLong(2, seqID);

                return ps;
            });

        }
        catch (Exception ex)
        {
            log.error("Exeption in EmailRepositoryImpl updateEmailsStatus SEQID: {} STATUS: {} : {}",seqID,status,ex.getMessage());
        }
    }
    public long createEmail(Email email){
        try
        {
            log.info("EmailRepositoryImpl createEmail invoiceID: {}",email.getId());
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
