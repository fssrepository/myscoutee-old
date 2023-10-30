package com.raxim.myscoutee.common.util;

import java.util.ArrayList;
import java.util.Optional;

import com.raxim.myscoutee.profile.data.document.mongo.FormItem;
import com.raxim.myscoutee.profile.data.document.mongo.Setting;
import com.raxim.myscoutee.profile.data.dto.rest.SettingDTO;

public class SettingUtil {
    public static Optional<String> getValue(Optional<SettingDTO> dbSetting, String valueName) {
        if (dbSetting.isPresent()) {
            Setting setting = dbSetting.get().getSetting();

            if(setting == null) {
                return Optional.empty();
            }

            Optional<FormItem> optFormItem = setting.getItems().stream()
                    .filter(item -> item.getName().equals(valueName))
                    .findFirst();

            if (optFormItem.isPresent()) {
                FormItem formItem = optFormItem.get();

                // based on formItem type - at the moment only option is supported
                Integer key = Integer.valueOf(((ArrayList<String>) formItem.getData()).get(0));
                String value = setting.getItems().get(0).getOptions().get(key).getValue();
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }
}
