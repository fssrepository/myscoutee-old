package com.raxim.myscoutee.profile.controller;

import java.util.List;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.User;
import com.raxim.myscoutee.profile.data.dto.rest.ErrorDTO;
import com.raxim.myscoutee.profile.data.dto.rest.GroupDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.ProfileDTO;
import com.raxim.myscoutee.profile.data.dto.rest.UserDTO;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.handler.TemplateParamHandler;
import com.raxim.myscoutee.profile.service.GroupService;
import com.raxim.myscoutee.profile.service.UserService;

@RepositoryRestController("groups")
public class GroupRestController {

    private final GroupService groupService;
    private final UserService userService;
    private final ParamHandlers paramHandlers;

    public GroupRestController(GroupService groupService, UserService userService, ParamHandlers paramHandlers) {
        this.groupService = groupService;
        this.userService = userService;
        this.paramHandlers = paramHandlers;
    }

    // list all available public groups (from /groups search button to
    // select)
    @GetMapping("")
    @Transactional
    public ResponseEntity<Object> groups(PageParam pageParam, Authentication auth) {
        FirebasePrincipal principal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = principal.getUser().getProfile();

        // override page param
        pageParam = paramHandlers.handle(profile, pageParam, TemplateParamHandler.TYPE);

        if (profile.getPosition() != null) {
            // temporirily Friends only filter is not available
            List<GroupDTO> groupDTOs = this.groupService.getAllGroups(
                    pageParam,
                    CommonUtil.point(profile.getPosition()),
                    "A");

            List<Object> lOffset = CommonUtil.offset(groupDTOs, pageParam.getOffset());

            return ResponseEntity.ok(new PageDTO<>(groupDTOs, lOffset));
        } else {
            return ResponseEntity.badRequest().body(new ErrorDTO(450, "err.no_profile"));
        }
    }

    // join group
    @PostMapping("/{groupId}/{type}")
    public ResponseEntity<ProfileDTO> change(@PathVariable String groupId, @PathVariable String type,
            Authentication auth) {
        Profile profile = ((FirebasePrincipal) auth.getPrincipal()).getUser().getProfile();

        String actionType = MemberAction.valueOf(type).getType();

        return ControllerUtil.handle((i, g, s) -> userService.changeStatus(i, g, s),
                profile.getId(), groupId, actionType,
                HttpStatus.OK);
    }

}
