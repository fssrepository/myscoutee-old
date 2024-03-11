package com.raxim.myscoutee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;

import com.raxim.myscoutee.algo.AbstractAlgoTest;
import com.raxim.myscoutee.common.AppTestConstants;
import com.raxim.myscoutee.common.config.JsonConfig;
import com.raxim.myscoutee.common.config.RepositoryConfig;
import com.raxim.myscoutee.common.repository.MongoDataLoaderTestExecutionListener;
import com.raxim.myscoutee.common.repository.TestData;
import com.raxim.myscoutee.profile.data.document.mongo.Event;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.RangeLocal;
import com.raxim.myscoutee.profile.repository.mongo.EventRepository;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ScoreMatrixRepository;
import com.raxim.myscoutee.profile.service.EventGeneratorByScoreService;

@DataMongoTest
@DirtiesContext
@Import({ RepositoryConfig.class, JsonConfig.class })
@TestPropertySource(properties = { "de.flapdoodle.mongodb.embedded.version=6.0.6",
                "logging.level.org.springframework.data.mongodb=DEBUG" })
@TestData({ "mongo/profiles.json", "mongo/multistage/events.json", "mongo/likes.json", "mongo/score_matrices.json" })
@TestExecutionListeners(value = MongoDataLoaderTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class EventGeneratorScoreServiceTestInt extends AbstractAlgoTest {

        @Autowired
        private LikeRepository likeRepository;

        @Autowired
        private ScoreMatrixRepository scoreMatrixRepository;

        @Autowired
        private EventRepository eventRepository;

        @Test
        public void shouldGenerateScoreEventsWithTwoStages() {
                Event event = this.eventRepository.findById(AppTestConstants.UUID_EVENT_32).get();
                // manipulate startdate enddate of event
                RangeLocal rangeLocal = new RangeLocal(LocalDateTime.now().minusMinutes(20),
                                LocalDateTime.now().plusHours(2));
                event.setRange(rangeLocal);
                this.eventRepository.save(event);

                int graceCnt = 60;
                event.getItems().stream().forEach(item -> {
                        RangeLocal lRangeLocal = new RangeLocal(
                                        LocalDateTime.now().plusMinutes((item.getStageIdx() + 1) * graceCnt),
                                        LocalDateTime.now().plusHours(2));
                        item.setRange(lRangeLocal);
                        this.eventRepository.save(item);
                });

                EventGeneratorByScoreService eventGeneratorByScoreService = new EventGeneratorByScoreService(
                                eventRepository, likeRepository, scoreMatrixRepository);

                List<Event> events = eventGeneratorByScoreService.generate(null);

                Event parentEvent = events.get(0);
                assertEquals(0, parentEvent.getStageIdx());

                List<Event> currEvents = parentEvent.getItemsByStage(parentEvent.getStageIdx());
                assertEquals(2, currEvents.size());

                Event event1 = currEvents.get(0);
                assertEquals("P", event1.getStatus());
                assertEquals(6, event1.getMembers().size());

                Event event2 = currEvents.get(1);
                assertEquals("P", event2.getStatus());
                assertEquals(5, event2.getMembers().size());

                // timed out
                List<Event> eventsToSave = currEvents.stream().map(item -> {
                        RangeLocal lRangeLocal = new RangeLocal(
                                        LocalDateTime.now().plusMinutes(item.getStageIdx() * graceCnt),
                                        LocalDateTime.now().plusHours(2));
                        item.setRange(lRangeLocal);

                        return item;
                }).toList();

                this.eventRepository.saveAll(eventsToSave);

                events = eventGeneratorByScoreService.generate(null);

                parentEvent = events.get(0);
                assertEquals(0, parentEvent.getStageIdx());

                currEvents = parentEvent.getItemsByStage(parentEvent.getStageIdx());
                assertEquals(2, currEvents.size());

                event1 = currEvents.get(0);
                assertEquals("T", event1.getStatus());
                assertEquals(6, event1.getMembers().size());

                event2 = currEvents.get(1);
                assertEquals("T", event2.getStatus());
                assertEquals(5, event2.getMembers().size());

                // accepted
                eventsToSave = currEvents.stream().map(item -> {
                        Set<Member> members = item.getMembers().stream().map(member -> {
                                member.setStatus("A");
                                return member;
                        }).collect(Collectors.toSet());
                        item.setMembers(members);

                        RangeLocal lRangeLocal = new RangeLocal(
                                        LocalDateTime.now().plusMinutes(item.getStageIdx() * graceCnt),
                                        LocalDateTime.now().plusMinutes(item.getStageIdx() * graceCnt));
                        item.setRange(lRangeLocal);

                        return item;
                }).toList();

                this.eventRepository.saveAll(eventsToSave);

                events = eventGeneratorByScoreService.generate(null);

                parentEvent = events.get(0);
                assertEquals(1, parentEvent.getStageIdx());

                List<Event> prevEvents = parentEvent.getItemsByStage(parentEvent.getStageIdx() - 1);

                assertEquals(2, prevEvents.size());

                event1 = prevEvents.get(0);
                assertEquals("F", event1.getStatus());
                assertEquals(6, event1.getMembers().size());

                event2 = prevEvents.get(1);
                assertEquals("F", event2.getStatus());
                assertEquals(5, event2.getMembers().size());

                List<Event> nextEvents = parentEvent.getItemsByStage(parentEvent.getStageIdx());
                assertEquals(1, nextEvents.size());

                event1 = nextEvents.get(0);
                assertEquals("P", event1.getStatus());
                assertEquals(4, event1.getMembers().size());
        }

}
