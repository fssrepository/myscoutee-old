package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;

@RepositoryRestController
@RequestMapping("groups")
public class GroupRestController {
    // list all available public groups (from /groups search button to
    // select)
    @GetMapping("")
    @Transactional
    public ResponseEntity<Object> groups(
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset,
            Authentication auth) {
        Object[] tOffset;
        if (offset != null && offset.length == 5) {
            tOffset = new Double[] {
                    Double.parseDouble(offset[0]),
                    Double.parseDouble(offset[1]),
                    Double.parseDouble(offset[2])
            };
        } else {
            tOffset = new Object[] { 0.0, "1900-01-01", 0.0 };
        }

        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        User user = firebasePrincipal.getUser();
        Profile profile = user.getProfile();

        if (profile.getPosition() != null) {
            Group group = groupRepository.findById(user.getGroup()).get();

            List<UUID> groupUUIDs = user.getProfiles()
                    .stream()
                    .filter(p -> !p.getStatus().equals("D"))
                    .map(Profile::getGroup)
                    .collect(Collectors.toList());

            List<GroupDTO> groups = groupRepository.findGroupByProfile(
                    group.getType(),
                    CommonUtil.point(profile.getPosition()),
                    20,
                    step != null ? step : 5,
                    groupUUIDs,
                    tOffset);

            // http://dolszewski.com/spring/how-to-bind-requestparam-to-object/

            List<Object> lOffset;
            if (!groups.isEmpty()) {
                lOffset = groups.get(groups.size() - 1).getOffset();
            } else {
                lOffset = Arrays.asList(tOffset);
            }

            return ResponseEntity.ok(
                    new PageDTO<>(groups, lOffset, 0));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    // join group
    @PostMapping("/{groupId}/{type}")
    public ResponseEntity<UserDTO> change(@PathVariable String groupId, @PathVariable String type,
            Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        User user = principal.getUser();

        String actionType = MemberAction.valueOf(type).getType();

        return ControllerUtil.handle((u, g, s) -> userService.changeStatus(u, g, s),
                user, groupId, actionType,
                HttpStatus.OK);
    }

}
