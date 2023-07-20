package com.raxim.myscoutee.algo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raxim.myscoutee.common.util.CommonUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Match;
import com.raxim.myscoutee.profile.data.document.mongo.Member;
import com.raxim.myscoutee.profile.data.document.mongo.Result;
import com.raxim.myscoutee.profile.data.document.mongo.Score;
import com.raxim.myscoutee.profile.data.document.mongo.ScoreMatrix;

enum ScoreAction {
    win("W"),
    draw("D"),
    loose("L"),
    goalFor("GF"),
    goalAgainst("GA"),
    yellow("Y"),
    ired("IR"),
    dred("DR"),
    ydred("YDR");
    // prfer removed, because the members are assigned baed on order

    private final String type;

    ScoreAction(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

public class Fifa {
    private List<Match> matches;
    private List<UUID> members;
    private List<ScoreMatrix> scoreMatrices;

    public Fifa(List<Match> matches, List<ScoreMatrix> scoreMatrices) {
        this.matches = matches;
        this.scoreMatrices = scoreMatrices;
    }

    public Map<UUID, Result> calc() {
        Map<UUID, Result> results = new HashMap<>();

        Map<String, ScoreMatrix> mtxByType = scoreMatrices.stream()
                .collect(Collectors.toMap(ScoreMatrix::getType, sm -> sm));

        List<Score> scores = matches.stream()
                .flatMap(match -> match.getScores().stream()).toList();

        for (Score score : scores) {

            if (!results.containsKey(score.getMemberId())) {
                results.put(score.getMemberId(), new Result(score.getMemberId()));
            }

            Result result = results.get(score.getMemberId());

            ScoreAction scoreAction = ScoreAction.valueOf(score.getType());
            Double lScore = mtxByType.get(score.getType()).getScore();

            switch (scoreAction) {
                case win:
                    result.setW(result.getW() + 1);
                    break;
                case draw:
                    result.setD(result.getD() + 1);
                    break;
                case loose:
                    result.setL(result.getL() + 1);
                    break;
                case yellow:
                case ydred:
                case ired:
                case dred:
                    result.setFpts(result.getFpts() + lScore.intValue());
                case goalAgainst:
                    result.setGa(lScore.intValue());
                case goalFor:
                    result.setGf(lScore.intValue());
                default:
            }

            int wScore = mtxByType.get(ScoreAction.win.getType()).getScore().intValue();
            int dScore = mtxByType.get(ScoreAction.win.getType()).getScore().intValue();

            result.setPts(wScore * result.getW() + dScore * result.getD());
            result.setGd(result.getGa() - result.getGd());

            result.setMp(result.getW() + result.getD() + result.getL());
        }
        return results;
    }

    public List<Member> getFirstXMembers(int firstXWinner, Stream<Member> sMembers) {
        List<Member> lMembers = sMembers.sorted()
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> CommonUtil.getFirstXWithEqual(list, firstXWinner)));

        if (lMembers.size() > firstXWinner) {
            Set<UUID> lMemberIds = lMembers.stream()
                    .map(lMember -> lMember.getProfile().getId())
                    .collect(Collectors.toSet());

            List<Match> matches = getMatches().stream()
                    .filter(match -> match.getMembers().stream()
                            .anyMatch(memberId -> lMemberIds.contains(memberId)))
                    .toList();

            Fifa fifa = new Fifa(matches, scoreMatrices);
            List<Result> lResults = fifa.calc().values().stream().sorted()
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            list -> CommonUtil.getFirstXWithEqual(list, firstXWinner)));

            if (lResults.size() > firstXWinner) {
                lResults = CommonUtil.getLastSameElements(lResults);
                Collections.shuffle(lResults);
            }

            Map<UUID, Member> lMembersMap = lMembers.stream().collect(
                    Collectors.toMap(member -> member.getProfile().getId(), member -> member));
            lMembers = lResults.stream().limit(firstXWinner)
                    .map(lResult -> lMembersMap.get(lResult.getProfileId())).toList();
        }
        return lMembers;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }
}
