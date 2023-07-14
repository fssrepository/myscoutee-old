package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Link;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkDTO;
import com.raxim.myscoutee.profile.data.dto.rest.LinkInfoDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;

enum GroupAction {
    suspend("S"),
    friendOnly("F"),
    all("A"),
    leave("L"),
    invisible("I"),
    join("J"),
    accept("A"); // based on gracePeriod it can be A, if mincapacity is ok

    private final String type;

    GroupAction(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

@RepositoryRestController
@RequestMapping("user")
public class UserGroupRestController {
    private final GroupRepository groupRepository;

    public UserGroupRestController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    // add button on the group list screen
    @PostMapping("/groups")
    public ResponseEntity<GroupDTO> saveGroup(
            Authentication auth,
            @RequestBody Group group) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();

        ResponseEntity<GroupDTO> response = ControllerUtil.handle(
                (g, u) -> this.groupService.saveGroup(g, u),
                group, user,
                HttpStatus.CREATED);
        return response;
    }

    @PatchMapping("/groups/{id}")
    @Transactional
    public ResponseEntity<GroupDTO> patchGroup(
            Authentication auth,
            @PathVariable String id,
            @RequestBody Group group) {

        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();

        ResponseEntity<GroupDTO> response = ControllerUtil.handle(
                (g, u) -> this.groupService.saveGroup(g, u),
                group, user,
                HttpStatus.OK);
        return response;
    }

    // leave group/delete group etc. -> notify other users
    @PostMapping("/groups/{groupId}/{type}")
    public ResponseEntity<UserDTO> change(@PathVariable String groupId, @PathVariable String type,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();

        String actionType = MemberAction.valueOf(type).getType();

        return ControllerUtil.handle((u, g, s) -> userService.changeStatus(u, g, s),
                user, groupId, actionType,
                HttpStatus.OK);
    }

    // Type is a group link
    @GetMapping("/groups/{id}/share")
    @Transactional
    public ResponseEntity<LinkDTO> shareGroup(
            Authentication auth,
            @PathVariable String id) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        UUID profileId = principal.getUser().getProfile().getId();

        Link link = new Link();
        link.setKey(UUID.randomUUID());
        link.setRefId(UUID.fromString(id));
        link.setType("g");
        link.setCreatedBy(profileId);
        Link linkSaved = linkRepository.save(link);

        Optional<Group> group = groupRepository.findById(UUID.fromString(id));

        if (group.isPresent()) {
            Group groupReq = group.get();
            LinkInfoDTO linkInfo = new LinkInfoDTO("Please be invited for " + groupReq.getName() + " group!",
                    groupReq.getDesc());
            LinkDTO linkResp = new LinkDTO(linkSaved, linkInfo);
            return ResponseEntity.ok(linkResp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("groups")
    @Transactional
    public ResponseEntity<Object> groupsForUser() {

    }

}
