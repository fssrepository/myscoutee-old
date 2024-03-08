package com.raxim.myscoutee.profile.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;

public class EventUtil {
    public static final String EVENT_TYPE_PRIVATE = "pr";
    public static final String DELETED = "D";

    public static Set<Edge> permutate(Set<Member> members) {
        List<List<List<Member>>> nodes = CommonUtil.permutation(new HashSet<>(members));

        Set<Edge> edges = nodes.stream()
                .flatMap(group -> group.stream()
                        .map(pair -> {
                            Profile profile1 = pair.get(0).getProfile();
                            Profile profile2 = pair.get(1).getProfile();

                            Node node1 = new Node(profile1.getId().toString(), profile1.getGender());
                            Node node2 = new Node(profile2.getId().toString(), profile2.getGender());
                            Edge edge = new Edge(node1, node2);
                            return edge;
                        }))
                .collect(Collectors.toSet());
        return edges;
    }

    public static Set<String> getIds(Set<Member> members) {
        return members.stream()
                .map(candidate -> candidate.getProfile().getId().toString())
                .collect(Collectors.toSet());
    }
}
