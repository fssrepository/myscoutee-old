package com.raxim.myscoutee.profile.data.document.mongo.iface;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public interface DCloneable<T> extends Cloneable {
    T clone(Profile profile)
            throws CloneNotSupportedException;
}
