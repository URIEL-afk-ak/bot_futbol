package com.botfutbol.repository;

import com.botfutbol.entity.GroupPoll;
import com.botfutbol.entity.PollVote;
import com.botfutbol.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, String> {
    Optional<PollVote> findByPollAndUser(GroupPoll poll, User user);
    List<PollVote> findByPoll(GroupPoll poll);
    long countByPollAndSelectedOptionIndex(GroupPoll poll, Integer optionIndex);
}

