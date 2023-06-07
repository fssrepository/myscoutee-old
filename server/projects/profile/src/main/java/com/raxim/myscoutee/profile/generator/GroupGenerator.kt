package com.raxim.myscoutee.profile.generator

import com.raxim.myscoutee.algo.BGroupSet
import com.raxim.myscoutee.algo.NodeRepository
import com.raxim.myscoutee.algo.dto.Edge
import com.raxim.myscoutee.algo.dto.Group
import com.raxim.myscoutee.algo.dto.Node
import com.raxim.myscoutee.algo.dto.Range
import com.raxim.myscoutee.profile.data.document.mongo.Profile
import com.raxim.myscoutee.profile.data.dto.internal.Bound
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository
import com.raxim.myscoutee.profile.repository.mongo.ScheduleRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GroupGenerator(
    val scheduleRepository: ScheduleRepository,
    val likeRepository: LikeRepository,
    private val nodes: MutableMap<String, Profile> = mutableMapOf()
) : IGenerator<Bound, Profile> {
    companion object {
        const val RANDOM_GROUP = "RANDOM_GROUP"
    }

    override fun generate(flags: Bound): List<Set<Profile>> {
        val schedule = this.scheduleRepository.findByKey(RANDOM_GROUP)
        var lastRunningTime: Date? = Date();
        if (schedule.isPresent) {
            lastRunningTime = schedule.get().lastRunDate;
        }

        //rates should be harmonic mean
        val likesBoth = this.likeRepository.findBothAll(lastRunningTime.toString(), 1.5)

        val edges = likesBoth.map { likeBoth ->
            Edge(
                Node(
                    likeBoth.from!!.id.toString(),
                    likeBoth.from.gender
                ), Node(likeBoth.to!!.id.toString(), likeBoth.to.gender),
                ((1 / (2 / likeBoth.rate!!)) * likeBoth.distance!!).toLong()
            )
        }

        val nodeRepository = NodeRepository()
        nodeRepository.addAll(edges)

        val range = Range(flags.minGroupSize, flags.maxGroupSize)
        val groupSet = BGroupSet(nodeRepository, range, listOf("m", "w"))

        val edgeFromMap = likesBoth.associate { it.from?.id!!.toString() to it.from }
        nodes.putAll(edgeFromMap)

        val edgeToMap = likesBoth.associate { it.to?.id!!.toString() to it.to }
        nodes.putAll(edgeToMap)

        return groupSet.map { group: Group? -> group?.nodes }
            .map { node -> node!!.map { nodes[it.id]!! }.toSet() }
    }
}