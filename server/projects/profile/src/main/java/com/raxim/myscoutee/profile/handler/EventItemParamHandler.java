package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@Component
public class EventItemParamHandler implements IParamHandler {
    public static final String TYPE = "eventItem";

    public static final String DAY_FORMAT = "%Y-%m-%d";

    public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {

        LocalDate from = LocalDate.now();
        LocalDate createdDateFrom = LocalDate.now();
        Integer stage = 0;

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 3) {
            from = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[0]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            createdDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[1]),
                    DateTimeFormatter.ISO_OFFSET_DATE);
            stage = Integer.valueOf(CommonUtil.decode((String) pageParam.getOffset()[2]));
        }

        String fromF = from.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String createdDateF = createdDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Object[] tOffset = new Object[] { fromF, createdDateF, stage };

        pageParam.setId(profile.getId());
        pageParam.setOffset(tOffset);
        pageParam.setGroupKey(DAY_FORMAT);
        return pageParam;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
