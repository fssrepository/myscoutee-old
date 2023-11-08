package com.raxim.myscoutee.common.data;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;

public class TestEvent extends Event {
    @Override
    @JsonProperty("items")
    public void setItems(List<Event> items) {
        super.setItems(items);
    }

    @Override
    @JsonProperty("members")
    public void setMembers(Set<Member> members) {
        super.setMembers(members);
    }
}
