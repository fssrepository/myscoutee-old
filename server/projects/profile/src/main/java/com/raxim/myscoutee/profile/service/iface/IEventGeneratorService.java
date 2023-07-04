package com.raxim.myscoutee.profile.service.iface;

import java.util.List;

import com.raxim.myscoutee.profile.data.document.mongo.Event;

public interface IEventGeneratorService {
    List<Event> generate();
}
