package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.SettingUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.SettingDTO;
import com.raxim.myscoutee.profile.service.SettingsService;
import com.raxim.myscoutee.profile.util.AppConstants;

@Component
public class EventParamHandler implements IParamHandler {
    public static final String TYPE = "event";

    private final SettingsService settingsService;

    public EventParamHandler(final SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {
        System.out.println("uuid" + profile.getId());
        Optional<SettingDTO> settingDTO = this.settingsService.getSetting(profile.getId(), "/activity/events");
        Optional<String> optViewType = SettingUtil.getValue(settingDTO, AppConstants.GROUP_NAME);
        String viewType = optViewType.isPresent() ? optViewType.get() : AppConstants.DAY; // day is the default grouping

        LocalDate from = LocalDate.now();
        LocalDate createdDateFrom = LocalDate.now();
        LocalDate until = LocalDate.now();

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 2) {
            from = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[0]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            createdDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[1]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String groupKey = null;
        if (AppConstants.MONTH.equals(viewType)) {
            from = from.withDayOfMonth(1);
            until = from.plusMonths(1);
            groupKey = AppConstants.MONTH_FORMAT;
        } else if (AppConstants.WEEK.equals(viewType)) {
            groupKey = AppConstants.WEEK_FORMAT;
        } else {
            groupKey = AppConstants.DAY_FORMAT;
        }

        String fromF = from.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String createdDateF = createdDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String untilF = until.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String[] tOffset = new String[] { fromF, createdDateF, untilF };

        pageParam.setId(profile.getId());
        pageParam.setOffset(tOffset);
        pageParam.setType(viewType);
        pageParam.setGroupKey(groupKey);
        return pageParam;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
