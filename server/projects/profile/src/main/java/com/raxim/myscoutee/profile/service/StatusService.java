package com.raxim.myscoutee.profile.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.raxim.myscoutee.profile.repository.mongo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.dto.rest.*;

@Service
public class StatusService {
    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final TokenRepository tokenRepository;
    private final MemberRepository memberRepository;
    private final PromotionRepository promotionRepository;

    public StatusService(
            ProfileRepository profileRepository,
            EventRepository eventRepository,
            EventItemRepository eventItemRepository,
            TokenRepository tokenRepository,
            MemberRepository memberRepository,
            PromotionRepository promotionRepository
    ) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.tokenRepository = tokenRepository;
        this.memberRepository = memberRepository;
        this.promotionRepository = promotionRepository;
    }

    public ResponseEntity<?> itemStatus(String itemId, String status, UUID profileUid) {
        Optional<Profile> profileOp = profileRepository.findById(profileUid);
        if (profileOp.isPresent()) {
            Profile profile = profileOp.get();
            return eventItemRepository.findById(UUID.fromString(itemId)).map(eventItem -> {
                {
                    String action = null;

                    if (status.equals("A") && eventItem.getNum() == eventItem.getCapacity().getMax()) {
                        return ResponseEntity.badRequest().body(new Error(450, "err.event_full"));
                    } else {
                        if (eventItem.getOptional() != null && eventItem.getOptional()) {
                            if (status.equals("A")) {
                                boolean isMember = eventItem.getMembers().stream()
                                        .anyMatch(member -> member.getId().equals(profile.getId()));
                                if (!isMember) {
                                    eventItem.getMembers().add(new Member(profileUid, profile, status, "U"));
                                }
                                action = "joined";
                                eventItem.setNum(eventItem.getNum() + 1);
                            } else if (status.equals("L")) {
                                action = "left";
                                eventItem.setNum(eventItem.getNum() - 1);
                            }

                            Set<Member> eMembers = eventItem.getMembers().stream().map(member -> {
                                if (member.getId().equals(profile.getId())) {
                                    member.setStatus(status);
                                }
                                return member;
                            }).collect(Collectors.toSet());

                            eventItem.setMembers(eMembers);
                            EventItem savedEventItem = eventItemRepository.save(eventItem);

                            if (action != null) {
                                List<UUID> promoterIds = savedEventItem.getMembers().stream()
                                        .filter(member -> member.getRole().equals("P"))
                                        .map(member -> member.getProfile().getId()).collect(Collectors.toList());

                                if (!promoterIds.isEmpty()) {
                                    List<String> deviceKeys = tokenRepository.findByUserIds(promoterIds).stream()
                                            .map(Token::getDeviceKey).collect(Collectors.toList());

                                    if (!deviceKeys.isEmpty()) {
                                        MulticastMessage message = MulticastMessage.builder()
                                                .setNotification(Notification.builder()
                                                        .setTitle("Member + " + action)
                                                        .setBody("Member '" + profile.getFirstName() + "' " + action +
                                                                " the " + eventItem.getName() + " event!")
                                                        .build())
                                                .addAllTokens(deviceKeys)
                                                .build();

                                        FirebaseMessaging.getInstance().sendMulticast(message);
                                        System.out.println("Successfully sent message");
                                    }
                                }
                            }

                            return ResponseEntity.noContent().build();
                        } else {
                            return ResponseEntity.badRequest().build();
                        }
                    }
                }
            }).orElse(ResponseEntity.notFound().build()).invoke();
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> status(String id, String status, UUID profileUid) {
        Optional<Profile> profileOp = profileRepository.findById(profileUid);
        if (profileOp.isPresent()) {
            Profile profile = profileOp.get();
            return eventRepository.findById(UUID.fromString(id)).map(event -> {
                {
                    String action = null;

                    if (status.equals("A") && event.getInfo().getNum() == event.getInfo().getCapacity().getMax()) {
                        return ResponseEntity.badRequest().body(new Error(450, "err.event_full"));
                    } else {
                        boolean isPromotion = event.getInfo().getMembers().stream()
                                .anyMatch(member -> member.getRole().equals("P"));

                        Set<Member> eMembers = event.getInfo().getMembers().stream().map(member -> {
                            if (member.getId().equals(profile.getId())) {
                                if (member.getRole().equals("P")) {
                                    if (status.equals("A")) {
                                        event.setStatus("A");
                                        action = "accepted";
                                        if (event.getRef() != null) {
                                            event.getRef().setCnt(event.getRef().getCnt() - 1);
                                        }
                                    } else {
                                        event.setStatus("C");
                                        action = "cancelled";
                                    }
                                }

                                if (isPromotion) {
                                    if (status.equals("L") && !member.getRole().equals("P") &&
                                            event.getStatus().equals("A")) {
                                        member.setStatus("LL");
                                    } else {
                                        if (status.equals("A")) {
                                            event.getInfo().setNum(event.getInfo().getNum() + 1);
                                        } else if (status.equals("L")) {
                                            event.getInfo().setNum(event.getInfo().getNum() - 1);
                                        }
                                        member.setStatus(status);
                                    }
                                } else {
                                    if (status.equals("A")) {
                                        event.getInfo().setNum(event.getInfo().getNum() + 1);
                                    } else if (status.equals("L")) {
                                        event.getInfo().setNum(event.getInfo().getNum() - 1);
                                    }
                                    member.setStatus(status);
                                }
                            }
                            return member;
                        }).collect(Collectors.toSet());

                        if (status.equals("L") && (event.getStatus().equals("A") || event.getStatus().equals("P"))) {
                            Member hasMember = event.getInfo().getMembers().stream()
                                    .filter(member -> member.getStatus().equals("A") || member.getStatus().equals("I"))
                                    .findFirst().orElse(null);
                            if (hasMember == null) {
                                event.setStatus("C");
                                action = "cancelled";
                            }
                        }

                        event.getInfo().setMembers(eMembers);
                        memberRepository.saveAll(event.getInfo().getMembers());
                        eventRepository.save(event);

                        if (event.getRef() != null) {
                            promotionRepository.findPromotionByEvent(event.getRef().getId()).ifPresent(promotion -> {
                                promotion.setCnt(promotion.getEvents().stream().mapToInt(Event::getCnt).sum());
                                promotionRepository.save(promotion);
                            });

                            if (action != null) {
                                List<String> deviceKeys = eventRepository.findTokensByEvent(
                                        new String[]{event.getRef().getId()}).stream()
                                        .map(Token::getDeviceKey).collect(Collectors.toList());

                                if (!deviceKeys.isEmpty()) {
                                    MulticastMessage message = MulticastMessage.builder()
                                            .setNotification(Notification.builder()
                                                    .setTitle("Event + " + action)
                                                    .setBody("Promoter " + action +
                                                            " the " + event.getInfo().getName() + " event!")
                                                    .build())
                                            .addAllTokens(deviceKeys)
                                            .build();

                                    FirebaseMessaging.getInstance().sendMulticast(message);
                                    System.out.println("Successfully sent message");
                                }
                            }
                        }

                        return ResponseEntity.noContent().build();
                    }
                }
            }).orElse(ResponseEntity.notFound().build()).invoke();
        }
        return ResponseEntity.badRequest().build();
    }
}
