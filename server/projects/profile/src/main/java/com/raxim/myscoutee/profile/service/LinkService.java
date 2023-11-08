package com.raxim.myscoutee.profile.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.dto.rest.LinkDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkInfoDTO;
import com.raxim.myscoutee.profile.repository.mongo.LinkRepository;

@Service
public class LinkService {
    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public Optional<LinkDTO> saveLink(UUID profileId, String refId, Pair<String, String> info) {
        Link link = new Link();
        link.setKey(UUID.randomUUID());
        link.setRefId(UUID.fromString(refId));
        link.setType("g");
        link.setCreatedBy(profileId);
        Link linkSaved = linkRepository.save(link);

        LinkInfoDTO linkInfo = new LinkInfoDTO("Please be invited for " + info.getFirst() + " group!",
                info.getSecond());
        LinkDTO linkDTO = new LinkDTO(linkSaved, linkInfo);
        return Optional.of(linkDTO);
    }
}
