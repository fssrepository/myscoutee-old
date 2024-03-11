package com.raxim.myscoutee.profile.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.ScoreMatrix;
import com.raxim.myscoutee.profile.generator.EventGeneratorByScore;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScoreMatrixRepository;
import com.raxim.myscoutee.profile.service.iface.IEventGeneratorService;
import com.raxim.myscoutee.profile.util.LikesWrapper;

/*
 * who did met before, selecton is by score 
 * (the members are scored -> multistage competition also using it to assign the firstX member)
 */
@Service
public class EventGeneratorByScoreService implements IEventGeneratorService {
        private final EventRepository eventRepository;
        private final LikeRepository likeRepository;
        private final ScoreMatrixRepository scoreMatrixRepository;

        public EventGeneratorByScoreService(EventRepository eventRepository, LikeRepository likeRepository,
                        ScoreMatrixRepository scoreMatrixRepository) {
                this.eventRepository = eventRepository;
                this.likeRepository = likeRepository;
                this.scoreMatrixRepository = scoreMatrixRepository;
        }

        @Override
        public List<Event> generate(String flags) {
                List<Event> events = this.eventRepository.findEvents();

                List<String> rankTypes = events.stream()
                                .flatMap(event -> event.getItems().stream()
                                                .map(sEvent -> sEvent.getRule().getRankType()))
                                .toList();
                List<ScoreMatrix> dbScoreMatrices = this.scoreMatrixRepository.findByNames(rankTypes);

                Map<String, List<ScoreMatrix>> scoreMatricesByType = dbScoreMatrices.stream()
                                .collect(Collectors.groupingBy(ScoreMatrix::getName));

                LikesWrapper lw = this.likeRepository.findAllByGroup();
                FGraph fGraph = lw.fGraph(Set.of("A", "F"), events);
                Map<String, Profile> profiles = lw.profiles();

                EventGeneratorByScore eventGeneratorByScore = new EventGeneratorByScore(events, scoreMatricesByType,
                                fGraph, profiles);
                List<Event> eventsToSave = eventGeneratorByScore.generate(flags);

                List<Event> savedEvents = this.eventRepository.saveAll(eventsToSave);
                return savedEvents;
        }

}
