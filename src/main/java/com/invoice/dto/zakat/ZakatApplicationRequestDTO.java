package com.invoice.dto.zakat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZakatApplicationRequestDTO {

    private String invoiceHash;
    private String uuid;
    private String invoice;
}
