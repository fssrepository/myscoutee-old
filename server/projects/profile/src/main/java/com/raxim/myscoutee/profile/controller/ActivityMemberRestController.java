package com.raxim.myscoutee.profile.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raxim.myscoutee.common.config.firebase.dto.FirebasePrincipal;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.common.util.ControllerUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Match;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.CodeDTO;
import com.raxim.myscoutee.profile.data.dto.rest.EventDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MatchDTO;
import com.raxim.myscoutee.profile.data.dto.rest.MemberDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageDTO;
import com.raxim.myscoutee.profile.data.dto.rest.PageParam;
import com.raxim.myscoutee.profile.data.dto.rest.SchoolDTO;
import com.raxim.myscoutee.profile.handler.MemberParamHandler;
import com.raxim.myscoutee.profile.handler.ParamHandlers;
import com.raxim.myscoutee.profile.handler.SchoolParamHandler;
import com.raxim.myscoutee.profile.service.EventService;
import com.raxim.myscoutee.profile.service.SchoolService;
import com.raxim.myscoutee.profile.service.StatusService;

enum MemberAction {
        join("J"),
        wait("W"),
        leave("L"),
        accept("A"),
        kick("K"),
        reject("R");
        // prfer removed, because the members are assigned baed on order

        private final String type;

        MemberAction(final String type) {
                this.type = type;
        }

        public String getType() {
                return type;
        }
}

@RestController
@RequestMapping("activity")
public class ActivityMemberRestController {
        private final StatusService statusService;
        private final ParamHandlers paramHandlers;
        private final EventService eventService;
        private final SchoolService schoolService;

        public ActivityMemberRestController(
                        StatusService statusService,
                        ParamHandlers paramHandlers,
                        EventService eventService,
                        SchoolService schoolService) {
                this.statusService = statusService;
                this.paramHandlers = paramHandlers;
                this.eventService = eventService;
                this.schoolService = schoolService;
        }

        @PostMapping({ "events/{id}/{type}", "invitations/{id}/{type}",
                        "events/{eventId}/items/{id}/{type}" })
        public ResponseEntity<EventDTO> changeStatusForEvent(@PathVariable String id,
                        @PathVariable String type,
                        Authentication auth) {
                FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
                Profile profile = firebasePrincipal.getUser().getProfile();

                String actionType = MemberAction.valueOf(type).getType();

                return ControllerUtil.handle((i, s, p) -> statusService.change(i, s, p),
                                id, profile.getId().toString(), actionType,
                                HttpStatus.OK);
        }

        // LeaderBoard object
        @GetMapping({ "events/{eventId}/leaderboard" })
        public ResponseEntity<List<MemberDTO>> getLeaderBoard(@PathVariable String eventId) {
                return ControllerUtil.handleList((ei) -> eventService.getLeaderboard(ei),
                                eventId,
                                HttpStatus.OK);
        }

        @PostMapping({ "events/{eventId}/matches/{matchId}" })
        public ResponseEntity<MatchDTO> saveMatch(@PathVariable String eventId,
                        @PathVariable String matchId, @RequestBody Match match) {
                return ControllerUtil.handle((i, s) -> eventService.saveMatch(i, s),
                                eventId, match,
                                HttpStatus.OK);
        }

        @PostMapping({ "events/{eventId}/members/{memberId}/{type}",
                        "events/{eventId}/items/{id}/members/{memberId}/{type}" })
        public ResponseEntity<?> manageStatusForEvent(@PathVariable String eventId, @PathVariable String itemId,
                        @PathVariable String memberId, @PathVariable String type, Authentication auth,
                        @RequestBody Member member) {
                FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
                Profile profile = firebasePrincipal.getUser().getProfile();

                if (member != null) {
                        return ControllerUtil.handle((i, m, s, p) -> eventService.score(i, m, s, p),
                                        eventId, memberId, member.getScore(), profile.getId(),
                                        HttpStatus.OK);
                } else {
                        String actionType = MemberAction.valueOf(type).getType();

                        return ControllerUtil.handle((i, m, s, p) -> statusService.change(i, m, s, p),
                                        eventId, memberId, actionType, profile.getId(),
                                        HttpStatus.OK);
                }
        }

        @GetMapping(value = { "events/{id}/members", "invitations/{id}/members",
                        "events/{eventId}/items/{id}/members",
                        "events/{eventId}/items/{itemId}/items/{id}/members" })
        public ResponseEntity<PageDTO<MemberDTO>> getMembersForEvent(@PathVariable String eventId,
                        @PathVariable String itemId, @PathVariable String id, PageParam pageParam,
                        Authentication auth) {

                FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
                Profile profile = firebasePrincipal.getUser().getProfile();

                pageParam = paramHandlers.handle(profile, pageParam, MemberParamHandler.TYPE);
                List<MemberDTO> members = eventService.getMembersByEvent(pageParam, id);
                List<Object> lOffset = CommonUtil.offset(members, pageParam.getOffset());

                Integer scroll = (eventId != null && itemId == null) ? 1 : null;
                Object lRole = !members.isEmpty() ? members.get(members.size() - 1).getRole() : null;

                return ResponseEntity.ok(new PageDTO<>(members, lOffset, scroll, pageParam.getStep(), lRole));
        }

        @PostMapping("events/{eventId}/members")
        @Transactional
        public ResponseEntity<EventDTO> addMembers(
                        @PathVariable String eventId, @RequestBody List<String> profileids, Authentication auth) {
                FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
                Profile profile = firebasePrincipal.getUser().getProfile();

                return ControllerUtil.handle((i, s, p) -> eventService.invite(i, s, p),
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

        // TODO: school fix - discreet event - isBusiness/isSchool event - discreet
        // level
        @GetMapping(value = { "events/{eventId}/members/{id}/schools" })
        public ResponseEntity<PageDTO<SchoolDTO>> getSchools(@PathVariable String id, PageParam pageParam,
                        Authentication auth) {
                FirebasePrincipal firebasePrincipal = (FirebasePrincipal) auth.getPrincipal();
                Profile profile = firebasePrincipal.getUser().getProfile();

                pageParam = paramHandlers.handle(profile, pageParam, SchoolParamHandler.TYPE);

                List<SchoolDTO> schoolDTOs = schoolService.getSchools(UUID.fromString(id), pageParam);
                List<Object> lOffset = CommonUtil.offset(schoolDTOs, pageParam.getOffset());

                return ResponseEntity.ok(new PageDTO<>(schoolDTOs, lOffset));
        }

}
