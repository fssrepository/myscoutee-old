package com.raxim.myscoutee.profile.handler;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

public interface IParamHandler {
    String getType();

    PageParam handle(Profile profile, PageParam pageParam);
}
