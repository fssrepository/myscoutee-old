package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@Component
public class LikeParamHandler implements IParamHandler {
    public static final String TYPE = "like";

    public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {

        double minDistance = 0d;
        double rate = 0d;
        double distance = 0d;
        LocalDate createdDateFrom = LocalDate.now();

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 4) {
            minDistance = Double.valueOf(CommonUtil.decode((String) pageParam.getOffset()[0]));
            rate = Double.valueOf(CommonUtil.decode((String) pageParam.getOffset()[1]));
            distance = Double.valueOf(CommonUtil.decode((String) pageParam.getOffset()[2]));
            createdDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[3]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String createdDateF = createdDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Object[] tOffset = new Object[] { minDistance,  rate,  distance, createdDateF };
        
        pageParam.setId(profile.getId());
        pageParam.setOffset(tOffset);
        return pageParam;
    }

}
