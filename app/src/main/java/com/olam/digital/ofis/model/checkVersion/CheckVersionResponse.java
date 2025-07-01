package com.olam.digital.ofis.model.checkVersion;

import lombok.Data;

@Data
public class CheckVersionResponse {
    private String status;
    private AppData appData;

    public class AppData {
        private String versionNo;
        private String downloadUrl;
        private String downloadLinkUrl;
    }

}
