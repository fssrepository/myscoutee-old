package com.raxim.myscoutee.profile.data.dto.rest;

public class CloneDTO {
    private int numberOfCopies;

    public CloneDTO() {

    }

    public CloneDTO(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setNumberOfCopies(int numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }
}
