package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@Component
public class MemberParamHandler implements IParamHandler {
    public static final String TYPE = "member";

    public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {
        String status = "A";
        Integer score = Integer.MAX_VALUE;
        LocalDate createdDateFrom = DATE_MIN;

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 3) {
            status = CommonUtil.decode((String) pageParam.getOffset()[0]);
            score = Integer.valueOf(CommonUtil.decode((String) pageParam.getOffset()[1]));
            createdDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[2]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String createdDateF = createdDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Object[] tOffset = { status, score, createdDateF };
        pageParam.setOffset(tOffset);
        pageParam.setId(profile.getId());
        
        return pageParam;
    }

}
