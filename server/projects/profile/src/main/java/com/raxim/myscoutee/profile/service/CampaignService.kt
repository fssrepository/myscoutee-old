package com.raxim.myscoutee.profile.service

import com.raxim.myscoutee.profile.data.document.mongo.Event
import com.raxim.myscoutee.profile.data.document.mongo.Profile
import com.raxim.myscoutee.profile.data.document.mongo.Slot
import com.raxim.myscoutee.profile.data.dto.rest.Idea
import com.raxim.myscoutee.profile.data.dto.rest.Job
import com.raxim.myscoutee.profile.data.dto.rest.Promotion
import com.raxim.myscoutee.profile.repository.mongo.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class CampaignService(
    private val promotionRepository: PromotionRepository,
    private val ideaRepository: IdeaRepository,
    private val jobRepository: JobRepository,
    private val eventRepository: EventRepository,
    private val eventItemRepository: EventItemRepository,
    private val eventService: EventService,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository
) {
    fun getPromotions(profileId: UUID, step: Int?, tOffset: Array<Any>): List<Promotion> {
        return this.promotionRepository.findPromotionsByProfile(
            profileId, 20, step ?: 5,
            tOffset
        )
    }

    fun getAllActiveEvents(refIds: Array<UUID>): List<Event> {
        return if (refIds.isNotEmpty()) {
            this.eventService.getAllActiveEvents(refIds)
        } else {
            emptyList()
        }
    }

    fun getPromotion(
        promotion: com.raxim.myscoutee.profile.data.document.mongo.Promotion,
        profile: Profile,
        isUpdate: Boolean = false,
        isEvent: Boolean = false
    ): Optional<Pair<com.raxim.myscoutee.profile.data.document.mongo.Promotion, Boolean>> {
        val promotionRes: Optional<com.raxim.myscoutee.profile.data.document.mongo.Promotion> =
            if (promotion.id != null) {
                this.promotionRepository.findById(promotion.id)
            } else {
                Optional.empty()
            }

        val currPromotion = if (isEvent) {
            promotionRes.get()
        } else {
            promotion
        }

        val group = if (currPromotion.group != null && currPromotion.group.system == false) {
            this.groupRepository.findById(currPromotion.group.id!!).get()
        } else {
            this.groupRepository.findSystemGroupByType(currPromotion.groupType!!)
        }

        val template = this.eventRepository.findById(currPromotion.item!!.id!!)

        return if (promotionRes.isPresent) {
            val oldPromotion = promotionRes.get()
            val upPromotion = oldPromotion.copy(
                item = template.get(),
                group = group,
                slots = currPromotion.slots,
                name = currPromotion.name,
                range = currPromotion.range
            )
            Optional.of(Pair(upPromotion, promotionRes.get().item!!.id != template.get().id))
        } else {
            if (!isUpdate) {
                val newPromotion = currPromotion.copy(
                    createdBy = profile.id,
                    createdDate = Date(),
                    item = template.get(),
                    group = group
                )
                Optional.of(Pair(newPromotion, false))
            } else {
                Optional.empty()
            }
        }
    }

    fun savePromotion(
        promotion: com.raxim.myscoutee.profile.data.document.mongo.Promotion,
        profile: Profile,
        isEvent: Boolean = false,
        isNewTemplate: Boolean = false
    ): Optional<com.raxim.myscoutee.profile.data.document.mongo.Promotion> {
        val promotionToSave: com.raxim.myscoutee.profile.data.document.mongo.Promotion
        if (isEvent) {
            val deletedSlots = promotion.slots.filter { slot ->
                promotion.events.none { event -> event.status != "D" && slot.range!!.start!!.toLocalDate() == event.info!!.range!!.start!!.toLocalDate() }
            }
            promotion.slots.removeAll(deletedSlots)

            val newSlots = promotion.events.filter { event ->
                event.status != "D" && promotion.slots.none { slot -> slot.range!!.start!!.toLocalDate() == event.info!!.range!!.start!!.toLocalDate() }
            }.map { event ->
                Slot(range = event.info!!.range)
            }

            promotion.slots.addAll(newSlots)

            promotionToSave = promotion
        } else {
            val deletedEvents = if (!isNewTemplate) {
                if (promotion.id != null) {
                    promotion.events.filter { event ->
                        event.status != "D" &&
                                promotion.slots.none { slot -> slot.range!!.start!!.toLocalDate() == event.info!!.range!!.start!!.toLocalDate() }
                    }.map { event ->
                        event.status = "D"
                        event
                    }
                } else {
                    emptyList()
                }
            } else {
                promotion.events.map { event ->
                    event.status = "D"
                    event
                }
            }

            promotion.events.removeAll(deletedEvents)

            val newEvents = promotion.slots.filter { slot ->
                promotion.events.none { event -> event.status != "D" && slot.range!!.start!!.toLocalDate() == event.info!!.range!!.start!!.toLocalDate() }
            }.map { slot ->
                val evt = promotion.item!!.cloneBy(profile, slot, true)
                evt.copy(type = promotion.type)
            }.toList()

            val mEventItems = newEvents.flatMap { event -> event.items }

            val mMembers = newEvents.flatMap { event -> event.info!!.members!!.toList() }
            this.memberRepository.saveAll(mMembers)

            promotion.cnt = promotion.slots.sumBy { slot ->
                slot.numOfItems
            }

            //don't need to save into event as the id is the same
            this.eventItemRepository.saveAll(mEventItems)

            val modEvents = newEvents.union(deletedEvents)
            val events = this.eventRepository.saveAll(modEvents)

            val allEvents = events.union(promotion.events).toMutableList()

            promotionToSave = promotion.copy(
                events = allEvents
            )
        }

        val promotion = this.promotionRepository.save(promotionToSave)
        return Optional.of(promotion)
    }

    fun getIdeas(profileId: UUID, step: Int?, tOffset: Array<Any>): List<Idea> {
        return this.ideaRepository.findIdeasByProfile(
            profileId, 20, step ?: 5,
            tOffset
        )
    }

    fun saveIdea(
        profileId: UUID,
        ideaId: UUID?,
        idea: com.raxim.myscoutee.profile.data.document.mongo.Idea
    ): Idea {

        val ideaToSave = if (ideaId != null) {
            val ideaResult = this.ideaRepository.findById(ideaId)
            val ideaEntity = ideaResult.get()
            idea.copy(
                id = ideaEntity.id,
                name = idea.name,
                desc = idea.desc,
                createdDate = ideaEntity.createdDate,
                createdBy = ideaEntity.createdBy
            )
        } else {
            idea.copy(
                id = UUID.randomUUID(),
                createdBy = profileId,
                createdDate = Date()
            )
        }

        val ideaSaved = this.ideaRepository.save(ideaToSave)

        return Idea(ideaSaved)
    }

    fun getJobs(profileId: UUID, step: Int?, tOffset: Array<Any>): List<Job> {
        return this.jobRepository.findJobsByProfile(
            profileId, 20, step ?: 5,
            tOffset
        )
    }

    fun saveJob(
        profileId: UUID,
        jobId: UUID?,
        job: com.raxim.myscoutee.profile.data.document.mongo.Job
    ): Job {

        val jobToSave = if (jobId != null) {
            val jobResult = this.jobRepository.findById(jobId)
            val jobEntity = jobResult.get()
            job.copy(
                id = jobEntity.id,
                name = jobEntity.name,
                desc = job.desc,
                createdDate = jobEntity.createdDate,
                createdBy = jobEntity.createdBy
            )
        } else {
            job.copy(
                id = UUID.randomUUID(),
                createdBy = profileId,
                createdDate = Date()
            )
        }

        val jobSaved = this.jobRepository.save(jobToSave)

        return Job(jobSaved)
    }
}