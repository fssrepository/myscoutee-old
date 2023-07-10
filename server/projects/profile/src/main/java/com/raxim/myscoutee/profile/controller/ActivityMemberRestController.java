package com.raxim.myscoutee.profile.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.CodeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.handler.MemberParamHandler;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.service.ProfileService;
import com.raxim.myscoutee.profile.service.StatusService;

enum MemberAction {
    join("J"),
    wait("W"),
    leave("L"),
    accept("A"),
    kick("K"),
    reject("R");

    private final String type;

    MemberAction(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

@RepositoryRestController
@RequestMapping("activity")
public class ActivityMemberRestController {
    private final ProfileService profileService;
    private final StatusService statusService;
    private final ParamHandlers paramHandlers;
    private final EventService eventService;

    public ActivityMemberRestController(
            ProfileService profileService,
            StatusService statusService,
            ParamHandlers paramHandlers,
            EventService eventService) {
        this.profileService = profileService;
        this.statusService = statusService;
        this.paramHandlers = paramHandlers;
        this.eventService = eventService;
    }

    // when the promoter accepts, can see the members, before that not!
    // instead of "clone" use name "pick or select"
    // TODO: promotion fix -> statusService.changeStatusForEvent not prepared to
    // handle
    // when event locked, promoter will be invited to the event,
    // hence the events will appear in the invitations tab, not the promotion tab
    // the promoter does not receive the chat messages, separate chat tab with
    // admin?
    // a normal event can cloned to a template and can be used for promotion later
    // on
    // promotion can be time based or just recommend something
    // (what you can show on recommendation tab -> like approximate time)
    // can't rerecommend already promotion event - recommended event hasn't any
    // promoter member also, it's just an empty event
    // group event is also on recommendation tab -> different colouring
    // job/idea, is a promotion category, what you can filter on recommendation tab
    // job is a group event of business, not dating -> advertise an event you need
    // to have "A" = advertiser role
    // dropdown box on profile editor, whether only advertiser for a group or
    // participants also
    // there is no separate job group
    // promotion editor is on event tab
    // group event will be shown at invitations tab
    // (however members are not added to the event as invited members to simiplify)
    // to join to a group is on profile screen (separate button etc.) -> remove from
    // recommendations screen
    // recommendation tab will be removed (when click on +, you can select -> it
    // shows recommended
    // and promotional event in the same screen, no separate tab)
    // recommmended, promotion event needs geo position (longitude, lattitude
    // built in map willbe later on -> the user coordinates to set for the time
    // being)

    // wait is when we have invited more members than what capacity we have set, if
    // it's filled, we can accept with wait
    // no separate promotions tab!!!
    @PostMapping({ "events/{id}/{type}", "invitations/{id}/{type}",
            "events/{eventId}/items/{id}/{type}" })
    public ResponseEntity<EventDTO> changeStatusForEvent(@PathVariable String id,
            @PathVariable String type,
            Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        String actionType = MemberAction.valueOf(type).getType();

        return ControllerUtil.handle((i, s, p) -> statusService.change(i, s, p),
                id, profile.getId(), actionType,
                HttpStatus.OK);
    }

    @PostMapping({ "events/{eventId}/members/{memberId}/{type}",
            "events/{eventId}/items/{id}/members/{memberId}/{type}" })
    public ResponseEntity<?> manageStatusForEvent(@PathVariable String eventId, @PathVariable String itemId,
            @PathVariable String memberId, @PathVariable String type, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        String actionType = MemberAction.valueOf(type).getType();

        return ControllerUtil.handle((i, m, s, p) -> statusService.change(i, m, s, p),
                eventId, memberId, profile.getId(), actionType,
                HttpStatus.OK);
    }

    @GetMapping(value = { "events/{eventId}/items/{itemId}/members" })
    public ResponseEntity<PageDTO<MemberDTO>> getMembersForItem(@PathVariable String itemId, PageParam pageParam,
            Authentication auth) {

        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, MemberParamHandler.TYPE);
        List<MemberDTO> members = this.eventService.getMembersByEvent(pageParam, itemId);
        List<Object> lOffset = CommonUtil.offset(members, pageParam.getOffset());

        return ResponseEntity.ok(new PageDTO<>(members, lOffset));
    }

    @GetMapping(value = { "events/{id}/members", "invitations/{id}/members",
            "events/{eventId}/items/{id}/members" })
    public ResponseEntity<PageDTO<MemberDTO>> getMembersForEvent(@PathVariable String id,
            PageParam pageParam, Authentication auth) {

        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        pageParam = paramHandlers.handle(profile, pageParam, MemberParamHandler.TYPE);
        List<MemberDTO> members = eventService.getMembersByEvent(pageParam, id);
        List<Object> lOffset = CommonUtil.offset(members, pageParam.getOffset());
        Object lRole = !members.isEmpty() ? members.get(members.size() - 1).getRole() : null;

        return ResponseEntity.ok(new PageDTO<>(members, lOffset, 1, null, lRole));
    }

    @PostMapping("events/{eventId}/members")
    @Transactional
    public ResponseEntity<EventDTO> addMembers(
            @PathVariable String eventId, @RequestBody List<String> profileids, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        return ControllerUtil.handle((i, s, p) -> eventService.inviteMembersForEvent(i, s, p),
                eventId, profileids, profile.getId(),
                HttpStatus.CREATED);
    }

    @GetMapping("events/{id}/code")
    public ResponseEntity<CodeDTO> code(@PathVariable String id, Authentication auth) {
        FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
        Profile profile = firebasePrincipal.getUser().getProfile();

        return ControllerUtil.handle((i, p) -> eventService.getCodeByEvent(i, p),
                id, profile.getId(), HttpStatus.OK);
    }

    @PostMapping("events/{id}/verify")
    public ResponseEntity<MemberDTO> verify(@PathVariable String id, @RequestBody String code) {
        return ControllerUtil.handle((i, p) -> eventService.getMemberByCode(i, p),
                id, code, HttpStatus.OK);
    }

    //TODO: fix ParamHandlers
    @GetMapping(value = { "events/{eventId}/members/{id}/schools" })
    public ResponseEntity<PageDTO<SchoolDTO>> getSchools(@PathVariable String id, Authentication auth,
            @RequestParam("step") Integer step,
            @RequestParam("offset") String[] offset) {
        String[] tOffset = (offset != null && offset.length == 3)
                ? new String[] { CommonUtil.decode(offset[0]), CommonUtil.decode(offset[1]),
                        CommonUtil.decode(offset[2]) }
                : new String[] { "a", "1900-01-01", "1900-01-01" };

        List<SchoolDTO> schools = profileService.getSchools(UUID.fromString(id), step, tOffset);

        List<Object> lOffset = schools.isEmpty() ? Arrays.asList(tOffset) : schools.get(schools.size() - 1).getOffset();

        return ResponseEntity.ok(new PageDTO<>(schools, lOffset));
    }

}
