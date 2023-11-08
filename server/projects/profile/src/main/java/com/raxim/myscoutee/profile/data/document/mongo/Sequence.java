package com.raxim.myscoutee.profile.data.document.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "seqs")
public class Sequence {
    @Id
    private String _id;

    private long cnt;

    public Sequence(String _id, long cnt) {
        this._id = _id;
        this.cnt = cnt;
    }

    public Sequence() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

}
