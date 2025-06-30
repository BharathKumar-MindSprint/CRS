package com.olam.digital.ofis.model.checkVersion;

import lombok.Data;

@Data
public class CheckVersionRequest {
    private long enumerator_id;
    private String current_version;

}
