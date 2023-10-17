package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.ScoreMatrix;
import com.raxim.myscoutee.profile.data.dto.FilteredEdges;
import com.raxim.myscoutee.profile.generator.EventGeneratorByScore;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScoreMatrixRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;

/*
 * who did met before, selecton is by score 
 * (the members are scored -> multistage competition also using it to assign the firstX member)
 */
@Service
public class EventGeneratorByScoreService implements IEventGeneratorService {
    private final EventRepository eventRepository;
    private final ScoreMatrixRepository scoreMatrixRepository;

    public EventGeneratorByScoreService(EventRepository eventRepository, ScoreMatrixRepository scoreMatrixRepository) {
        this.eventRepository = eventRepository;
        this.scoreMatrixRepository = scoreMatrixRepository;
    }

    @Override
    public List<Event> generate(FilteredEdges filteredEdges, String flags) {
        List<Event> events = this.eventRepository.findEvents();

        List<String> rankTypes = events.stream().map(event -> event.getRule().getRankType()).toList();
        List<ScoreMatrix> dbScoreMatrices = this.scoreMatrixRepository.findByNames(rankTypes);

        Map<String, List<ScoreMatrix>> scoreMatricesByType = dbScoreMatrices.stream()
                .collect(Collectors.groupingBy(ScoreMatrix::getName));

        EventGeneratorByScore eventGeneratorByScore = new EventGeneratorByScore(events, filteredEdges, flags,
                scoreMatricesByType);
        List<Event> eventsToSave = eventGeneratorByScore.generate();

        List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
        return savedEvents;
    }

}
