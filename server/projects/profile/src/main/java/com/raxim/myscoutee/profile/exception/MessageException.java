package com.raxim.myscoutee.profile.exception;

import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;

public class MessageException extends Exception {
    private final ErrorDTO errorDTO;

    public MessageException(String errMsg) {
        this.errorDTO = new ErrorDTO(450, errMsg);
    }

    public ErrorDTO getErrorDTO() {
        return errorDTO;
    }
}
