package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.service.SettingsService;

@Component
public class EventParamHandler implements IParamHandler {
    public static final String EVENT = "event";

    public static final String MONTH = "m";

    private final SettingsService settingsService;

    public EventParamHandler(final SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {
        String step = this.settingsService.getStep(profile, pageParam.getStep());

        String[] tOffset;
        if (pageParam.getOffset() != null && pageParam.getOffset().length == 2) {
            if (MONTH.equals(step)) {
                LocalDateTime from = LocalDate
                        .parse(CommonUtil.decode(pageParam.getOffset()[0]), DateTimeFormatter.ISO_DATE_TIME)
                        .withDayOfMonth(1).atStartOfDay();
                String fromFormatted = from.format(DateTimeFormatter.ISO_DATE_TIME);

                tOffset = new String[] { fromFormatted, CommonUtil.decode(pageParam.getOffset()[1]) };
            } else {
                tOffset = new String[] { CommonUtil.decode(pageParam.getOffset()[0]),
                        CommonUtil.decode(pageParam.getOffset()[1]) };
            }
        } else {
            if (MONTH.equals(step)) {
                LocalDateTime from = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                String fromFormatted = from.format(DateTimeFormatter.ISO_DATE_TIME);
                tOffset = new String[] { fromFormatted, "1900-01-01" };
            } else {
                tOffset = new String[] { LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME),
                        "1900-01-01" };
            }
        }
        return new PageParam(tOffset, pageParam.getDirection(), step);
    }

    @Override
    public String getType() {
        return EVENT;
    }
}
