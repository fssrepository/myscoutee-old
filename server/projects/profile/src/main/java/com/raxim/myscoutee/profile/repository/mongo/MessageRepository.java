package com.raxim.myscoutee.profile.repository.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import com.raxim.myscoutee.profile.data.document.mongo.DBMessage;
import com.raxim.myscoutee.profile.data.dto.rest.MessageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;

public interface MessageRepository extends MongoRepository<DBMessage, UUID> {

    @Aggregation(pipeline = "findLastMessageByChannels")
    public List<MessageDTO> findLastMessageByChannels(@Param("param") PageParam param);

    @Aggregation(pipeline = "findMessagesByChannel")
    public List<MessageDTO> findMessagesByChannel(@Param("eventId") UUID eventId, @Param("param") PageParam param);
}
