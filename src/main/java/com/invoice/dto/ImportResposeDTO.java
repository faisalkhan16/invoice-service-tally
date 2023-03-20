package com.invoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportResposeDTO {

    @JsonProperty(value = "total_records")
    private String recordsCount;

    @JsonProperty(value = "import_records")
    private String importCount;

    @JsonProperty(value = "fail_records")
    private String failCount;
}
