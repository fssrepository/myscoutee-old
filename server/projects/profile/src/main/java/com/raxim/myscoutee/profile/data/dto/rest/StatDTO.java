package com.raxim.myscoutee.profile.data.dto.rest;

public class StatDTO extends PageItemDTO {
    private String name;
    private Integer invited;
    private Integer msgReceived;
    private Integer likeReceived; // all the likes where $to.$id is the profile (even if it's double rated)

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInvited() {
        return invited;
    }

    public void setInvited(Integer invited) {
        this.invited = invited;
    }

    public Integer getMsgReceived() {
        return msgReceived;
    }

    public void setMsgReceived(Integer msgReceived) {
        this.msgReceived = msgReceived;
    }

    public Integer getLikeReceived() {
        return likeReceived;
    }

    public void setLikeReceived(Integer likeReceived) {
        this.likeReceived = likeReceived;
    }
}
