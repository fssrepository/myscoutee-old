package com.raxim.myscoutee.profile.service;

import org.springframework.stereotype.Service;

@Service
public class EventGeneratorPromotionService {
    // for non-creator the event will have a rateBar, inside it can be discreet Event or not
    // for creator the event does not have rateBar, but the promotion.members has and it shows the rate what the creator adds to them
    // if promotion is within eventGrace from start, if stage is within eventGrace start next stage
    // stage can be closed manually before eventGrace (hence immediate calculation needs to be triggered and invite accordingly)
    // a) if any timeslot has got minimum capacity, promotion status is A
    // b) if any timeslot hasn't got minimum capacity then merge slots,
    // invite members (considering prefered timeslots), memberGrace overdue, invite
    // other ones based on rate
    // join first is auto approval - but members can be kicked
    // when promotion.event (subEvent) has min capacity after merge status is
    // accepted

    // the algorithm based on stage (score or rate - breaker), rate needs to be
    // approved memberGrace in time
    // or invite an other one from all the slots of the prev Stage
}
