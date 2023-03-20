package com.invoice.dto.zakat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZakatInfoModel {

    private String type;
    private String category;
    private String code;
    private String message;
    private String status;
}
