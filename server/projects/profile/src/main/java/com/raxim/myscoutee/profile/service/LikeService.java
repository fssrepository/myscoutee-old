package com.raxim.myscoutee.profile.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.raxim.myscoutee.profile.data.document.mongo.LikeForGroup;
import com.raxim.myscoutee.profile.data.document.mongo.LikeGroup;
import com.raxim.myscoutee.profile.data.document.mongo.Profile;
import com.raxim.myscoutee.profile.data.document.mongo.Sequence;
import com.raxim.myscoutee.profile.data.dto.rest.LikeDTO;
import com.raxim.myscoutee.profile.repository.mongo.LikeRepository;
import com.raxim.myscoutee.profile.repository.mongo.ProfileRepository;
import com.raxim.myscoutee.profile.repository.mongo.SequenceRepository;
import com.raxim.myscoutee.profile.util.LikeUtil;

@Service
public class LikeService {

    private final ProfileRepository profileRepository;
    private final LikeRepository likeRepository;
    private final SequenceRepository sequenceRepository;

    public LikeService(ProfileRepository profileRepository,
            LikeRepository likeRepository,
            SequenceRepository sequenceRepository) {
        this.profileRepository = profileRepository;
        this.likeRepository = likeRepository;
        this.sequenceRepository = sequenceRepository;
    }

    public List<LikeDTO> saveLikes(Profile profile, List<LikeDTO> pfLikes) {
        // fill up missing from by the current authenticated user
        List<LikeDTO> likeDTOs = pfLikes.stream().map(like -> {
            UUID from = like.getFrom() != null ? like.getFrom() : profile.getId();
            like.setFrom(from);
            return like;
        }).toList();

        List<LikeForGroup> likes = handleLikes(profile, likeDTOs);

        List<LikeForGroup> likesWithCnt = generateNewCounters(likes);

        // save likes
        List<LikeForGroup> likesSaved = likeRepository.saveAll(likesWithCnt);

        List<LikeDTO> likesAll = toLikeDTOs(likesSaved);
        return likesAll;
    }

    private List<LikeForGroup> handleLikes(Profile profile, List<LikeDTO> likeDTOs) {
        // load likes, add + parameter based on like type (Person,Job,Idea)
        List<LikeGroup> dbLikeGroups = likeRepository.findByParty(profile.getId(),
                likeDTOs);

        // all the relevant profiles
        Set<UUID> profileUUIDs = likeDTOs.stream()
                .flatMap(likeDto -> Stream.of(likeDto.getFrom(), likeDto.getTo()))
                .collect(Collectors.toSet());
        profileUUIDs.add(profile.getId());

        // load profiles
        Map<UUID, Profile> profiles = profileRepository.findAllById(profileUUIDs)
                .stream()
                .collect(Collectors.toMap(
                        Profile::getId,
                        dbProfile -> dbProfile));

        Profile createdBy = profiles.get(profile.getId());

        // likeDTOs to likes
        List<LikeForGroup> likes = likeDTOs.stream().flatMap(likeDTO -> {
            // filter out LikeGroups being touched by the likes -> double rates are in
            // different like group
            LikeGroup likeGroup = dbLikeGroups.stream()
                    .filter(dbLikeGroup -> dbLikeGroup.getLikes()
                            .stream()
                            .anyMatch(dbLike -> LikeUtil.isEqual(likeDTO, dbLike)
                                    || LikeUtil.isReverseEqual(likeDTO, dbLike)))
                    .findFirst().orElse(null);

            LikeForGroup mLike = null;

            Long cnt = null;
            if (likeGroup != null) {
                List<LikeForGroup> mLikes = likeGroup.getLikes();
                if (mLikes != null) {
                    cnt = mLikes.get(0).getCnt();
                }

                mLike = mLikes.stream()
                        .filter(dbLike -> LikeUtil.isEqual(likeDTO, dbLike)
                                && profile.getId().equals(dbLike.getCreatedBy().getId()))
                        .findFirst()
                        .orElse(null);
            }

            if (mLike != null) {
                mLike.setRate(likeDTO.getRate());
                mLike.setDistance(LikeUtil.calcDistance(mLike.getFrom(), mLike.getTo()));
            } else {
                Profile profileFrom = profiles.get(likeDTO.getFrom());
                Profile profileTo = profiles.get(likeDTO.getTo());

                boolean isDouble = !likeDTO.getFrom().equals(profile.getId());

                mLike = new LikeForGroup();
                mLike.setId(UUID.randomUUID());
                mLike.setStatus(isDouble ? "D" : "A");
                mLike.setFrom(profileFrom);
                mLike.setTo(profileTo);
                mLike.setCreatedBy(createdBy);
                mLike.setCreatedDate(new Date());
                mLike.setRef(likeDTO.getRef());
                mLike.setType(likeDTO.getType());
                mLike.setCnt(cnt);

                mLike.setRate(likeDTO.getRate());
                mLike.setDistance(LikeUtil.calcDistance(profileFrom, profile));
            }
            return Stream.of(mLike);
        }).filter(likeForGroup -> likeForGroup != null).toList();
        return likes;
    }

    private List<LikeDTO> toLikeDTOs(List<LikeForGroup> likesSaved) {
        // return the updated likes
        List<LikeDTO> likesAll = likesSaved.stream()
                .map(like -> {
                    LikeDTO likeDTO = new LikeDTO();
                    if (like.getFrom() != null) {
                        likeDTO.setFrom(like.getFrom().getId());
                    }

                    if (like.getTo() != null) {
                        likeDTO.setTo(like.getTo().getId());
                    }

                    likeDTO.setRef(like.getRef());

                    likeDTO.setRate(like.getRate());
                    return likeDTO;
                })
                .toList();
        return likesAll;
    }

    private List<LikeForGroup> generateNewCounters(List<LikeForGroup> likes) {
        // update cnt values
        long newLikesNum = likes.stream().filter(like -> like.getCnt() == null).count();
        long newCnt = sequenceRepository.nextValue("likes", newLikesNum).getCnt();
        Sequence oldSequence = new Sequence("likes", newCnt - newLikesNum);

        List<LikeForGroup> likesWithCnt = likes.stream().map(like -> {
            if (like.getCnt() == null) {
                long cnt = oldSequence.getCnt();
                ++cnt;
                like.setCnt(cnt);
                oldSequence.setCnt(cnt);
            }
            return like;
        }).toList();
        return likesWithCnt;
    }
}
