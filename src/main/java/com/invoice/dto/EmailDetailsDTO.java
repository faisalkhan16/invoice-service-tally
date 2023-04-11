package com.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDetailsDTO {

    private String recipient;
    private String msgBody;
    private String subject;
    private File attachment;
}