package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Set;

public class EventItemWithCandidates {

    private EventItem item;
    private Set<Member> candidates;

    public EventItem getItem() {
        return item;
    }

    public void setItem(EventItem item) {
        this.item = item;
    }

    public Set<Member> getCandidates() {
        return candidates;
    }

    public void setCandidates(Set<Member> candidates) {
        this.candidates = candidates;
    }
}
