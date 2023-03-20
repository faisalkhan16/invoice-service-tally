package com.invoice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    private long seqId;
    private String id;

    private String status;

    private String buyerEmail;
    private LocalDateTime crDT;

    private LocalDateTime updDT;
}
