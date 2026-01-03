package com.botfutbol.repository;

import com.botfutbol.entity.Group;
import com.botfutbol.entity.GroupPoll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPollRepository extends JpaRepository<GroupPoll, String> {
    List<GroupPoll> findByGroupAndIsActiveTrueOrderByCreatedAtDesc(Group group);
    List<GroupPoll> findByGroupOrderByCreatedAtDesc(Group group);
}

