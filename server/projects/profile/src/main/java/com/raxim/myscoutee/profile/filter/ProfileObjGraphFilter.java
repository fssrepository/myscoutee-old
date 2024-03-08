package com.raxim.myscoutee.profile.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.ObjGraph;
import com.raxim.myscoutee.profile.data.document.mongo.EventWithCandidates;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Rule;
import com.raxim.myscoutee.profile.util.EventUtil;

@Component
public class ProfileObjGraphFilter implements IFilter<ObjGraph<Profile>, EventWithCandidates> {

        @Override
        public ObjGraph<Profile> filter(ObjGraph<Profile> t, EventWithCandidates evt) {
                FGraph tfg = t.getfGraph();

                Set<Member> members = evt.getEvent().getMembers();
                Set<Member> candidates = evt.getCandidates();
                Rule rule = evt.getEvent().getRule();

                Set<Edge> sIgnoredEdges = EventUtil.permutate(members);

                Set<Edge> sIgnoredEdgesByRate = tfg.getEdges().stream()
                                .filter(edge -> rule.getRate() != null &&
                                                edge.getWeight() >= rule.getRate())
                                .collect(Collectors.toSet());

                List<Set<Edge>> ignoredEdges = new ArrayList<>();
                ignoredEdges.add(sIgnoredEdges);
                ignoredEdges.add(sIgnoredEdgesByRate);
                ignoredEdges.addAll(tfg.getIgnoredEdges());

                Set<Edge> possibleEdges = EventUtil.permutate(candidates);

                List<Edge> validEdges = tfg.getEdges().stream()
                                .filter(edge -> possibleEdges.contains(edge))
                                .toList();

                Set<Node> ignoredNodes = members.stream()
                                .filter(member -> "A".equals(member.getStatus())
                                                || "I".equals(member.getStatus()))
                                .map(member -> {
                                        Profile profile = t.getNodes()
                                                        .get(member.getProfile().getId().toString());
                                        return new Node(profile.getId().toString(), profile.getGender());
                                })
                                .collect(Collectors.toSet());

                FGraph fGraph = new FGraph(tfg.getNodes(), validEdges, ignoredNodes, ignoredEdges);

                return new ObjGraph<Profile>(t.getNodes(), fGraph);
        }

}
