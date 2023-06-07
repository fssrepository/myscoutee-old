package com.raxim.myscoutee.profile.service

import com.raxim.myscoutee.profile.controller.months
import com.raxim.myscoutee.profile.data.document.mongo.*
import com.raxim.myscoutee.profile.repository.mongo.EventItemRepository
import com.raxim.myscoutee.profile.repository.mongo.EventRepository
import com.raxim.myscoutee.profile.repository.mongo.MemberRepository
import com.raxim.myscoutee.profile.repository.mongo.PromotionRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

fun Event.shiftBy(eventItem: EventItem): Event {

    //arrange date of eventItems
    val minutes: Long
    val evtRange: RangeLocal
    if (eventItem.id == this.info?.id) {
        minutes = ChronoUnit.MINUTES.between(this.info!!.range!!.start, eventItem.range!!.start)
        evtRange = RangeLocal(eventItem.range!!.start!!, eventItem.range!!.end!!)
        this.info = eventItem.copy(range = evtRange, members = this.info!!.members, num = this.info!!.num)
    } else {
        minutes = 0L
        // maxRange
        val maxRange = this.items.map { item ->
            if (item.id == eventItem.id) {
                eventItem.range
            } else {
                item.range
            }
        }.reduce { acc, range ->
            var start = acc!!.start
            if (range!!.start!!.isBefore(start)) {
                start = range.start!!
            }

            var end = acc.end
            if (range.end!!.isAfter(end)) {
                end = range.end
            }
            RangeLocal(start, end)
        }
        evtRange = maxRange!!
        this.info = this.info!!.copy(range = evtRange)
    }

    this.items.replaceAll { item ->
        val currItem = if (this.items.size == 1 || this.info!!.id == item.id) {
            this.info!!
        } else if (item.id == eventItem.id) {
            eventItem.copy(members = item.members, num = item.num)
        } else {
            item
        }

        if (minutes > 0) {
            val range = RangeLocal(
                item.range!!.start!!.plusMinutes(minutes),
                item.range!!.end!!.plusMinutes(minutes)
            )
            currItem.copy(range = range)
        } else {
            currItem
        }
    }

    return this
}

fun Event.cloneBy(
    profile: Profile,
    slot: Slot? = null,
    isRef: Boolean = false
): Event {
    var newEvent = this.copy(id = UUID.randomUUID())
    if (slot != null) {
        newEvent.info!!.capacity = slot.capacity
        newEvent.cnt = slot.numOfItems

        val slotDate = slot.range!!.start!!.toLocalDate()
        val infoStart = this.info!!.range!!.start!!
        val infoEnd = this.info!!.range!!.end!!

        val days = ChronoUnit.DAYS.between(infoStart.toLocalDate(), slotDate)
        val evtRange = RangeLocal(infoStart.plusDays(days), infoEnd.plusDays(days))
        val eventItem = newEvent.info!!.copy(range = evtRange)

        newEvent = newEvent.shiftBy(eventItem)
    }

    newEvent.items = newEvent.items.filter { item -> item.type != "pr" && item.status != "D" }
        .map { item -> item.copy(id = UUID.randomUUID()) }.toMutableList()

    newEvent.items[0] = newEvent.info!!

    val members = newEvent.info!!.members!!.filter { member -> member.status == "P" }.toMutableSet()
    val status = if (newEvent.status == "T" || members.isNotEmpty()) {
        "P"
    } else {
        "A"
    }

    val memberStatus = if (newEvent.status == "T") {
        "P"
    } else {
        "A"
    }

    val role = if (newEvent.status == "T") {
        "P"
    } else {
        "M"
    }

    val newEventId = UUID.randomUUID()
    members.add(Member(profile.id, profile, status = memberStatus, role = role, eventRef = newEventId))

    val ref = if (!isRef) this else null

    newEvent.info!!.members = members
    newEvent.info!!.num++

    newEvent = newEvent.copy(
        id = newEventId,
        status = status,
        info = newEvent.info,
        group = profile.group,
        ref = ref
    )

    return newEvent
}

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val eventItemRepository: EventItemRepository,
    private val promotionRepository: PromotionRepository,
    private val memberRepository: MemberRepository
) {
    fun getEventsByStatus(
        tOffset: Array<String>,
        group: UUID,
        status: String
    ): List<com.raxim.myscoutee.profile.data.dto.rest.Event> {
        return this.eventRepository.findEventsByStatus(group, 20, tOffset, status)
    }

    fun getAllActiveEvents(refIds: Array<UUID>): List<Event> {
        return this.eventRepository.findActiveEvents(arrayOf("A", "P"), refIds)
    }

    fun getAllActiveTokens(refIds: Array<UUID>): List<Token> {
        return this.eventRepository.findTokensByEvent(refIds)
    }

    fun getEvents(
        step: String?,
        direction: Int?,
        tOffset: Array<String>,
        profileId: UUID,
        status: Array<String>
    ): List<com.raxim.myscoutee.profile.data.dto.rest.Event> {
        var events = emptyList<com.raxim.myscoutee.profile.data.dto.rest.Event>()

        if (step == null || step == "d") {
            events = if (direction == 1) {
                this.eventRepository.findEventDown(
                    profileId, 20, 5, "%Y-%m-%d", status,
                    tOffset, "A"
                )
            } else {
                this.eventRepository.findEventUp(
                    profileId, 20, 5, "%Y-%m-%d", status,
                    tOffset, "A"
                )
            }
        } else {
            if (step == "w") {
                events = this.eventRepository.findEventDown(
                    profileId, 20, 5, "%Y %U", status,
                    tOffset, "A"
                )
            } else if (step == "m") {
                val from =
                    LocalDate.parse(tOffset[0], DateTimeFormatter.ISO_DATE_TIME).withDayOfMonth(1).atStartOfDay()

                val until =
                    LocalDate.parse(tOffset[0], DateTimeFormatter.ISO_DATE_TIME).withDayOfMonth(1).atStartOfDay()
                        .plusMonths(1)

                events = this.eventRepository.findEventByMonth(
                    profileId, 20, 5, "%Y-%m", status, until.format(DateTimeFormatter.ISO_DATE_TIME),
                    tOffset, "A"
                ).map { event ->
                    val dateParts = event.groupKey.toString().split("-")
                    event.groupKey = dateParts[0] + " " + months[dateParts[1].toInt() - 1]
                    val range = event.event?.info?.range
                    if (range != null) {
                        var start = range.start!!
                        if (start < from) {
                            start = from
                        }
                        var end = range.end!!
                        if (end > until) {
                            end = until.minusSeconds(1);
                        }
                        event.event.info?.range = RangeLocal(start, end)
                    }
                    event
                }
            }
        }
        return events
    }

    fun cloneEvent(
        eventId: UUID,
        profile: Profile
    ): Optional<Event> {
        val eventRes = this.eventRepository.findById(eventId)

        return if (eventRes.isPresent) {
            val event = eventRes.get().cloneBy(profile)
            this.memberRepository.saveAll(event.info!!.members!!.toList())
            this.eventItemRepository.saveAll(event.items)
            val savedEvent = this.eventRepository.save(event)
            Optional.of(savedEvent)
        } else {
            Optional.empty()
        }
    }

    fun recommendEvent(
        eventId: UUID
    ): Optional<com.raxim.myscoutee.profile.data.dto.rest.Event> {
        val eventRes = this.eventRepository.findById(eventId)

        return if (eventRes.isPresent) {
            val eventDto: com.raxim.myscoutee.profile.data.dto.rest.Event

            val event = eventRes.get()
            val items =
                event.items.filter { item -> item.type != "pr" && item.status != "D" }
                    .map { item -> item.copy(id = UUID.randomUUID()) }
                    .toMutableList()
            event.items = this.eventItemRepository.saveAll(items)

            event.info!!.members = mutableSetOf()

            val newEvent = event.copy(
                id = UUID.randomUUID(),
                status = "U",
                ref = event,
                createdDate = Date()
            )

            val savedEvent = this.eventRepository.save(newEvent)

            val groupKey = savedEvent.info?.range?.start?.toLocalDate()
            val sortKey = savedEvent.info?.range?.start?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

            eventDto = com.raxim.myscoutee.profile.data.dto.rest.Event(
                event = savedEvent,
                groupKey = groupKey,
                sortKey = sortKey
            )

            Optional.of(eventDto)
        } else {
            Optional.empty()
        }
    }

    fun getEvent(
        eventItem: EventItem,
        profile: Profile,
        status: String,
        eventId: UUID? = null,
        isUpdate: Boolean = false
    ): Optional<Event> {
        var eventRes: Optional<Event> = if (eventId != null) {
            this.eventRepository.findById(eventId)
        } else {
            Optional.empty()
        }

        eventRes = if (eventRes.isPresent) {
            val event = eventRes.get()

            if (isUpdate) {
                if (eventItem.id != null) {
                    val item = event.items.firstOrNull { item -> item.id == eventItem.id }
                    if (item != null) {
                        Optional.of(event)
                    } else {
                        Optional.empty()
                    }
                } else {
                    Optional.empty()
                }
            } else {
                Optional.of(event)
            }
        } else {
            if (!isUpdate) {
                val event = Event(
                    info = eventItem,
                    position = eventItem.position,
                    group = profile.group,
                    status = status,
                    createdBy = profile.id,
                    createdDate = Date()
                )
                Optional.of(event)
            } else {
                Optional.empty()
            }
        }

        return if (eventRes.isPresent) {
            val event = eventRes.get()
            var member = event.info!!.members!!.firstOrNull { member -> member.id == profile.id }
            if (member == null) {
                var code: String? = null
                if (eventItem.ticket == true) {
                    code = UUID.randomUUID().toString()
                }

                val role = if (status == "T") "P" else "M"

                member =
                    Member(profile.id, profile = profile, status = "A", code = code, role = role, eventRef = event.id)
                event.info!!.members!!.add(member)

                if (role == "M") {
                    event.info!!.num++
                }
            }
            Optional.of(event)
        } else {
            Optional.empty()
        }

    }

    fun saveEvent(
        pEvent: Event,
        pEventItem: EventItem
    ): Optional<Pair<Event, EventItem>> {

        var event = pEvent
        var eventItem = event.items.firstOrNull { item -> pEventItem.id == item.id }
        if (eventItem == null) {
            event.items.add(pEventItem)
        }

        event.items.sortBy { item -> item.range!!.start }
        event.positions = event.items.map { item -> item.position!! }

        event = event.shiftBy(pEventItem)

        val eventItems = this.eventItemRepository.saveAll(event.items)
        event = event.copy(items = eventItems)
        event = this.eventRepository.save(event)
        eventItem = eventItems.first { item ->
            item.id == pEventItem.id
        }

        return Optional.of(Pair(event, eventItem))
    }

    fun saveEvents(groups: List<Set<Profile>>) {
        val membersByGroup = groups.map { profiles -> profiles.map { Member(profile = it, status = "A", role = "U") } }

        val eventItems = membersByGroup.map { members ->
            val fromDT = LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(21)
            val toDT = fromDT.plusHours(3)
            val range = RangeLocal(fromDT, toDT)

            EventItem(
                type = "g",
                category = "l",
                name = "Generated Event!",
                desc = "Generated Event for strangers!",
                members = members?.toMutableSet(),
                range = range
            )
        }

        val eventItemsSaved = this.eventItemRepository.saveAll(eventItems)

        val events = eventItemsSaved.map { eventItemSaved ->
            Event(
                info = eventItemSaved,
                items = mutableListOf(eventItemSaved),
                status = "A"
            )
        }

        this.eventRepository.saveAll(events)
    }

    fun getEventItems(
        eventId: UUID,
        step: Int?,
        tOffset: Array<Any>,
        profileId: UUID
    ): List<com.raxim.myscoutee.profile.data.dto.rest.EventItem> {
        return this.eventRepository.findItemsByEvent(
            eventId, 20, step ?: 5, "%Y-%m-%d", profileId,
            tOffset
        )
    }

    // group needs to be handled on client side
    fun getEventsByPromotion(
        profileId: UUID,
        eventId: UUID,
        step: Int?,
        tOffset: Array<Any>
    ): List<com.raxim.myscoutee.profile.data.dto.rest.Event> {
        return this.promotionRepository.findEventsByPromotion(
            eventId, 20, step ?: 5, "%Y-%m-%d", profileId,
            tOffset
        )
    }
}