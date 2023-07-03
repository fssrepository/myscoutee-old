package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.Set;

public class EventWithCandidates {

    private Event event;
    private Set<Member> candidates;

    public Set<Member> getCandidates() {
        return candidates;
    }

    public void setCandidates(Set<Member> candidates) {
        this.candidates = candidates;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
