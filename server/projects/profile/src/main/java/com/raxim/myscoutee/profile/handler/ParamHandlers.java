package com.raxim.myscoutee.profile.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

@Service
public class ParamHandlers {
    private final Map<String, IParamHandler> paramHandlers;

    public ParamHandlers(List<IParamHandler> paramHandlers) {
        this.paramHandlers = paramHandlers.stream()
                .collect(Collectors.toMap(IParamHandler::getType, paramHandler -> paramHandler));
    }

    public boolean canHandle(String type) {
        return this.paramHandlers.containsKey(type);
    }

    public PageParam handle(Profile profile, PageParam pageParam, String type) {
        return this.paramHandlers.get(type).handle(profile, pageParam);
    }

}
