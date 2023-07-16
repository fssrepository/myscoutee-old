package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Setting;

@JsonRootName("setting")
public class SettingDTO extends PageItemDTO {
    @JsonProperty(value = "setting")
    private Setting setting;

    public SettingDTO() {
    }

    public SettingDTO(Setting setting) {
        this.setting = setting;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }
}
