package com.raxim.myscoutee.profile.repository.mongo;

import java.util.UUID;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public interface UserExtRepository {
    public void addProfile(UUID userId, Profile profile);
}
