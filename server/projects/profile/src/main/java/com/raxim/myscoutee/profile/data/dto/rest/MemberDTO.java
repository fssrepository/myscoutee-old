package com.raxim.myscoutee.profile.data.dto.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.raxim.myscoutee.profile.data.document.mongo.Member;

@JsonRootName("member")
public class MemberDTO extends PageItemDTO {
    @JsonProperty(value = "member")
    private Member member;

    @JsonProperty(value = "role")
    private String role;

    // promotion rate member - only show, what the promoter rated
    @JsonProperty(value = "rate")
    private Integer rate;

    public MemberDTO() {
    }

    public MemberDTO(Member member) {
        this.member = member;
    }

    public MemberDTO(Member member, List<Object> offset) {
        setOffset(offset);
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }
}
