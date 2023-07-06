package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.service.SettingsService;

@Component
public class EventParamHandler implements IParamHandler {
    public static final String TYPE = "event";

    public static final String MONTH = "m";
    public static final String WEEK = "w";
    public static final String DAY = "d";

    public static final String MONTH_FORMAT = "%Y-%m";
    public static final String WEEK_FORMAT = "%Y %U";
    public static final String DAY_FORMAT = "%Y-%m-%d";

    public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

    private final SettingsService settingsService;

    public EventParamHandler(final SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {
        String viewType = this.settingsService.getViewType(profile, pageParam.getType());
        viewType = viewType != null ? viewType : DAY; // day is the default grouping

        LocalDate from = LocalDate.now();
        LocalDate createdDateFrom = DATE_MIN;
        LocalDate until = LocalDate.now();

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 2) {
            from = LocalDate.parse(CommonUtil.decode((String)pageParam.getOffset()[0]), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            createdDateFrom = LocalDate.parse(CommonUtil.decode((String)pageParam.getOffset()[1]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String groupKey = null;
        if (MONTH.equals(viewType)) {
            from = from.withDayOfMonth(1);
            until = from.plusMonths(1);
            groupKey = MONTH_FORMAT;
        } else if (WEEK.equals(viewType)) {
            groupKey = WEEK_FORMAT;
        } else if (DAY.equals(viewType)) {
            groupKey = DAY_FORMAT;
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
