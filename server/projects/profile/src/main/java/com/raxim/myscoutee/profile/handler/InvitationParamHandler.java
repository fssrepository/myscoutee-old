package com.raxim.myscoutee.profile.handler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.util.AppConstants;

@Component
public class InvitationParamHandler implements IParamHandler {
    public static final String TYPE = "invitation";

    @Override
    public PageParam handle(Profile profile, PageParam pageParam) {
        LocalDate updatedDateFrom = LocalDate.now();

        if (pageParam.getOffset() != null && pageParam.getOffset().length == 1) {
            updatedDateFrom = LocalDate.parse(CommonUtil.decode((String) pageParam.getOffset()[0]),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        String groupKey = AppConstants.DAY_FORMAT;

        String updatedDateFromF = updatedDateFrom.atStartOfDay(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String[] tOffset = new String[] { updatedDateFromF };

        pageParam.setId(profile.getId());
        pageParam.setOffset(tOffset);
        pageParam.setGroupKey(groupKey);
        return pageParam;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
