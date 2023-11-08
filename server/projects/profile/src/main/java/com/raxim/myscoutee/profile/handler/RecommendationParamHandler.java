package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@Component
public class RecommendationParamHandler implements IParamHandler {
    public static final String TYPE = "rec";

    public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {

        double minDistance = 0d;
        LocalDate from = LocalDate.now();
        LocalDate updatedDateFrom = LocalDate.now();

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 3) {
            minDistance = Double.valueOf(CommonUtil.decode((String) pageParam.getOffset()[0]));
            from = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[1]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            updatedDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[2]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String updatedDateFromF = updatedDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String fromF = from.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Object[] tOffset = new Object[] { minDistance, fromF, updatedDateFromF };

        pageParam.setId(profile.getId());
        pageParam.setOffset(tOffset);
        return pageParam;
    }

}
