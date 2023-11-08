package com.raxim.myscoutee.profile.data.document.mongo.iface;

import java.util.List;

public interface Tree<T> extends DCloneable<T> {

    List<T> flatten();

    void shift();

    void sync();
}
