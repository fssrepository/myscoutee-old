package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.FormItem;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Setting;
import com.raxim.myscoutee.profile.repository.mongo.SettingRepository;

@Service
public class SettingsService {
    private final SettingRepository settingRepository;

    public SettingsService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @SuppressWarnings("unchecked")
    public String getStep(Profile profile, String pStep) {
        Setting setting = settingRepository.findSettingByProfileAndKey(profile.getId(), "/activity/events");

        String step = pStep;
        if (setting != null) {
            FormItem group = setting.getItems().stream()
                    .filter(item -> item.getName().equals("group"))
                    .findFirst().orElse(null);

            if (group != null) {
                Integer key = ((ArrayList<Integer>) group.getData()).get(0);
                step = setting.getItems().get(0).getOptions().get(key).getValue();
            }
        }
        return step;
    }
}
