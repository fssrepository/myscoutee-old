package com.raxim.myscoutee.profile.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Form;
import com.raxim.myscoutee.profile.data.document.mongo.FormItem;
import com.raxim.myscoutee.profile.data.document.mongo.Setting;
import com.raxim.myscoutee.profile.data.dto.rest.SettingDTO;
import com.raxim.myscoutee.profile.repository.mongo.FormRepository;
import com.raxim.myscoutee.profile.repository.mongo.SettingRepository;

@Service
public class SettingsService {
    private final FormRepository formRepository;
    private final SettingRepository settingRepository;

    public SettingsService(SettingRepository settingRepository, FormRepository formRepository) {
        this.settingRepository = settingRepository;
        this.formRepository = formRepository;
    }

    public Optional<SettingDTO> getSetting(UUID profileId, String key) {
        Optional<Setting> setting = settingRepository.findSettingByProfileAndKey(profileId, key);

        if (setting == null) {
            Optional<Form> form = formRepository.findFormByKey(key);
            if (form.isPresent()) {
                Form sForm = form.get();

                Setting settingToSave = new Setting();
                settingToSave.setId(UUID.randomUUID());
                settingToSave.setKey(key);
                settingToSave.setProfile(profileId);
                settingToSave.setItems(sForm.getItems());
                Setting savedSetting = settingRepository.save(settingToSave);
                return Optional.of(new SettingDTO(savedSetting));
            }
        }
        return Optional.empty();
    }

    public Optional<SettingDTO> saveSetting(UUID profileId, String key, Setting setting)
            throws CloneNotSupportedException {
        Optional<Setting> dbSetting = settingRepository.findSettingByProfileAndKey(profileId, key);
        if (dbSetting.isPresent()) {
            Setting clonedSetting = (Setting) dbSetting.get().clone();
            clonedSetting.setItems(setting.getItems());
            Setting settingsSaved = settingRepository.save(clonedSetting);
            dbSetting = Optional.of(settingsSaved);
        }
        return Optional.of(new SettingDTO(setting));
    }

    @SuppressWarnings("unchecked")
    public Optional<String> getValue(UUID profileId, String pKey, String valueName) {
        Optional<Setting> dbSetting = settingRepository.findSettingByProfileAndKey(profileId, pKey);
        if (dbSetting.isPresent()) {
            Setting setting = dbSetting.get();

            Optional<FormItem> optFormItem = setting.getItems().stream()
                    .filter(item -> item.getName().equals(valueName))
                    .findFirst();

            if (optFormItem.isPresent()) {
                FormItem formItem = optFormItem.get();

                // based on formItem type - at the moment only option is supported
                Integer key = ((ArrayList<Integer>) formItem.getData()).get(0);
                String step = setting.getItems().get(0).getOptions().get(key).getValue();
                return Optional.of(step);
            }
        }

        return Optional.empty();
    }
}
