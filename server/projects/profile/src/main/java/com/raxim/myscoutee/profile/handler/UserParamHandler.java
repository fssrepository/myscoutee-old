package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@Component
public class UserParamHandler implements IParamHandler {
    public static final String TYPE = "user";

    public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {

        String role = "A";
        LocalDate updatedDateFrom = LocalDate.now();

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 2) {
            role = CommonUtil.decode((String) pageParam.getOffset()[0]);
            updatedDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[1]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String updatedDateFromF = updatedDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Object[] tOffset = new Object[] { role, updatedDateFromF };

        pageParam.setId(profile.getId());
        pageParam.setOffset(tOffset);
        return pageParam;
    }

}
