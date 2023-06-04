package com.invoice.repository;

import com.invoice.model.Email;

import java.util.List;

public interface EmailRepository {
    public List<Email> getPendingEmails();

    public void updateEmailsStatus(String status,long seqID);
    public long createEmail(Email email);
}
