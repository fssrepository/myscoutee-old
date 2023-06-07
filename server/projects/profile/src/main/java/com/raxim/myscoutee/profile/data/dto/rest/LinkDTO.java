package com.raxim.myscoutee.profile.data.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Link;

@JsonRootName("link")
public class LinkDTO {
    @JsonProperty(value = "link")
    private Link link;

    @JsonProperty(value = "info")
    private LinkInfoDTO info;

    public LinkDTO() {
    }

    public LinkDTO(Link link, LinkInfoDTO info) {
        this.link = link;
        this.info = info;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public LinkInfoDTO getInfo() {
        return info;
    }

    public void setInfo(LinkInfoDTO info) {
        this.info = info;
    }
}
