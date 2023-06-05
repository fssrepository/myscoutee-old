package com.raxim.myscoutee.profile.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.common.Pair;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Group;
import com.raxim.myscoutee.profile.data.document.mongo.Idea;
import com.raxim.myscoutee.profile.data.document.mongo.Job;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Slot;
import com.raxim.myscoutee.profile.data.dto.rest.Promotion;
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.GroupRepository;
import com.raxim.myscoutee.profile.repository.mongo.IdeaRepository;
import com.raxim.myscoutee.profile.repository.mongo.JobRepository;
import com.raxim.myscoutee.profile.repository.mongo.MemberRepository;
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository;

@Service
public class CampaignService {

    private final PromotionRepository promotionRepository;
    private final IdeaRepository ideaRepository;
    private final JobRepository jobRepository;
    private final EventRepository eventRepository;
    private final EventItemRepository eventItemRepository;
    private final EventService eventService;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public CampaignService(PromotionRepository promotionRepository, IdeaRepository ideaRepository,
            JobRepository jobRepository, EventRepository eventRepository, EventItemRepository eventItemRepository,
            EventService eventService, GroupRepository groupRepository, MemberRepository memberRepository) {
        this.promotionRepository = promotionRepository;
        this.ideaRepository = ideaRepository;
        this.jobRepository = jobRepository;
        this.eventRepository = eventRepository;
        this.eventItemRepository = eventItemRepository;
        this.eventService = eventService;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    public List<Promotion> getPromotions(UUID profileId, Integer step, Object[] tOffset) {
        return this.promotionRepository.findPromotionsByProfile(profileId, 20, step != null ? step : 5, tOffset);
    }

    public List<Event> getAllActiveEvents(UUID[] refIds) {
        return refIds != null && refIds.length > 0 ? this.eventService.getAllActiveEvents(refIds) : List.of();
    }

    public Optional<Pair<com.raxim.myscoutee.profile.data.document.mongo.Promotion, Boolean>> getPromotion(
            com.raxim.myscoutee.profile.data.document.mongo.Promotion promotion, Profile profile, boolean isUpdate,
            boolean isEvent) {
        Optional<com.raxim.myscoutee.profile.data.document.mongo.Promotion> promotionRes = promotion.getId() != null
                ? this.promotionRepository.findById(promotion.getId())
                : Optional.empty();

        com.raxim.myscoutee.profile.data.document.mongo.Promotion currPromotion = isEvent ? promotionRes.get()
                : promotion;

        Group group = currPromotion.getGroup() != null && !currPromotion.getGroup().isSystem()
                ? this.groupRepository.findById(currPromotion.getGroup().getId()).get()
                : this.groupRepository.findSystemGroupByType(currPromotion.getGroupType());

        Optional<Event> template = this.eventRepository.findById(currPromotion.getItem().getId());

        if (promotionRes.isPresent()) {
            com.raxim.myscoutee.profile.data.document.mongo.Promotion oldPromotion = promotionRes.get();
            Promotion upPromotion = oldPromotion.copy(template.get(), group, currPromotion.getSlots(),
                    currPromotion.getName(), currPromotion.getRange());
            return Optional.of(new Pair<>(upPromotion, promotionRes.get().getItem().getId() != template.get().getId()));
        } else {
            if (!isUpdate) {
                Promotion newPromotion = currPromotion.copy(profile.getId(), new Date(), template.get(), group);
                return Optional.of(new Pair<>(newPromotion, false));
            } else {
                return Optional.empty();
            }
        }
    }

    public Optional<Promotion> savePromotion(Promotion promotion, Profile profile, boolean isEvent,
            boolean isNewTemplate) {
        if (isEvent) {
            List<Slot> deletedSlots = promotion.getSlots().stream()
                    .filter(slot -> promotion.getEvents().stream()
                            .noneMatch(event -> event.getStatus() != "D"
                                    && slot.getRange().getStart().toLocalDate()
                                            .equals(event.getInfo().getRange().getStart().toLocalDate())))
                    .toList();
            promotion.getSlots().removeAll(deletedSlots);

            List<Event> newEvents = promotion.getEvents().stream()
                    .filter(event -> event.getStatus() != "D"
                            && promotion.getSlots().stream()
                                    .noneMatch(slot -> slot.getRange().getStart().toLocalDate()
                                            .equals(event.getInfo().getRange().getStart().toLocalDate())))
                    .map(event -> new Slot(event.getInfo().getRange()))
                    .toList();
            promotion.getSlots().addAll(newEvents);

            return Optional.of(promotion);
        } else {
            List<Event> deletedEvents;
            if (!isNewTemplate) {
                if (promotion.getId() != null) {
                    deletedEvents = promotion.getEvents().stream()
                            .filter(event -> event.getStatus() != "D" && promotion.getSlots().stream()
                                    .noneMatch(slot -> slot.getRange().getStart().toLocalDate()
                                            .equals(event.getInfo().getRange().getStart().toLocalDate())))
                            .peek(event -> event.setStatus("D"))
                            .toList();
                } else {
                    deletedEvents = List.of();
                }
            } else {
                deletedEvents = promotion.getEvents().stream().peek(event -> event.setStatus("D")).toList();
            }

            promotion.getEvents().removeAll(deletedEvents);

            List<Event> newEvents = promotion.getSlots().stream()
                    .filter(slot -> promotion.getEvents().stream()
                            .noneMatch(event -> event.getStatus() != "D"
                                    && slot.getRange().getStart().toLocalDate()
                                            .equals(event.getInfo().getRange().getStart().toLocalDate())))
                    .map(slot -> {
                        Event evt = promotion.getItem().cloneBy(profile, slot, true);
                        return evt.copy(promotion.getType());
                    }).toList();

            List<EventItem> mEventItems = newEvents.stream().flatMap(event -> event.getItems().stream()).toList();
            this.memberRepository.saveAll(newEvents.stream().flatMap(event -> event.getInfo().getMembers().stream())
                    .toList());

            promotion.setCnt(promotion.getSlots().stream().mapToInt(Slot::getNumOfItems).sum());

            this.eventItemRepository.saveAll(mEventItems);

            List<Event> modEvents = Stream.concat(newEvents.stream(), deletedEvents.stream()).toList();
            List<Event> events = this.eventRepository.saveAll(modEvents);

            List<Event> allEvents = Stream.concat(events.stream(), promotion.getEvents().stream())
                    .collect(Collectors.toList());

            promotion.setEvents(allEvents);

            return Optional.of(this.promotionRepository.save(promotion));
        }
    }

    public List<Idea> getIdeas(UUID profileId, Integer step, Object[] tOffset) {
        return this.ideaRepository.findIdeasByProfile(profileId, 20, step != null ? step : 5, tOffset);
    }

    public Idea saveIdea(UUID profileId, UUID ideaId, Idea idea) {
        Idea ideaToSave;
        if (ideaId != null) {
            Idea ideaEntity = this.ideaRepository.findById(ideaId).get();
            ideaToSave = idea.copy(ideaEntity.getId(), idea.getName(), idea.getDesc(), ideaEntity.getCreatedDate(),
                    ideaEntity.getCreatedBy());
        } else {
            ideaToSave = idea.copy(UUID.randomUUID(), profileId, new Date());
        }

        Idea ideaSaved = this.ideaRepository.save(ideaToSave);

        return new Idea(ideaSaved);
    }

    public List<Job> getJobs(UUID profileId, Integer step, Object[] tOffset) {
        return this.jobRepository.findJobsByProfile(profileId, 20, step != null ? step : 5, tOffset);
    }

    public Job saveJob(UUID profileId, UUID jobId, Job job) {
        Job jobToSave;
        if (jobId != null) {
            Job jobEntity = this.jobRepository.findById(jobId).get();
            jobToSave = job.copy(jobEntity.getId(), jobEntity.getName(), job.getDesc(), jobEntity.getCreatedDate(),
                    jobEntity.getCreatedBy());
        } else {
            jobToSave = job.copy(UUID.randomUUID(), profileId, new Date());
        }

        Job jobSaved = this.jobRepository.save(jobToSave);

        return new Job(jobSaved);
    }
}
