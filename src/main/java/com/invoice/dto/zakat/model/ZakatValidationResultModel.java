package com.invoice.dto.zakat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("validationResults")
public class ZakatValidationResultModel {

    @JsonProperty("warningMessages")
    private List<ZakatInfoModel> warningMessages;

    @JsonProperty("errorMessages")
    private List<ZakatInfoModel> errorMessages;

    @JsonProperty("infoMessages")
    private List<ZakatInfoModel> infoMessages;

    @JsonProperty("status")
    private String status;
}
