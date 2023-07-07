package com.raxim.myscoutee.profile.data.document.mongo;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.raxim.myscoutee.common.repository.GeoJsonPointDeserializer;
import com.raxim.myscoutee.profile.converter.Convertable;

@Document(collection = "items")
public class EventItem extends EventBase implements Convertable {
    @Id
    @JsonProperty(value = "key")
    private UUID id;

    // type is general, car, accommodation, private (like phone number)

    // google, booking.com etc., az alkalmazas belso url-je is
    // car is a urlRef - popupwindow with car details
    @JsonProperty(value = "urlRef")
    private String urlRef;

    // opcionalis esemenyek - members

    // Add getters and setters for all fields

    @GeoSpatialIndexed(name = "position", type = GeoSpatialIndexType.GEO_2DSPHERE)
    @JsonDeserialize(using = GeoJsonPointDeserializer.class)
    @JsonProperty(value = "position")
    private GeoJsonPoint position;

    public EventItem() {
    }

    public EventItem(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUrlRef() {
        return urlRef;
    }

    public void setUrlRef(String urlRef) {
        this.urlRef = urlRef;
    }

    public GeoJsonPoint getPosition() {
        return position;
    }

    public void setPosition(GeoJsonPoint position) {
        this.position = position;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventItem other = (EventItem) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public void sync() {
        int cnt = (int) getMembers().stream().filter(member -> "A".equals(member.getStatus())).count();
        setNum(cnt);

        if (getMembers() != null && getCapacity() != null) {
            int diff = getCapacity().getMax() - cnt;

            if (diff > 0) {
                getMembers().stream()
                        .filter(member -> "W".equals(member.getStatus()))
                        .sorted((m1, m2) -> m1.getUpdatedDate().compareTo(m2.getUpdatedDate()))
                        .limit(diff).map(member -> {
                            member.setStatus("J");
                            return member;
                        })
                        .collect(Collectors.toList());
            }

            int cntWithJ = (int) getMembers().stream()
                    .filter(member -> "J".equals(member.getStatus())).count();
            int diffWithJ = cntWithJ - getCapacity().getMax();

            if (diffWithJ > 0) {
                getMembers().stream()
                        .filter(member -> "J".equals(member.getStatus()))
                        .sorted((m1, m2) -> m1.getUpdatedDate().compareTo(m2.getUpdatedDate()))
                        .limit(diffWithJ).map(member -> {
                            member.setStatus("W");
                            return member;
                        })
                        .collect(Collectors.toList());
            }
        }
    }
}
