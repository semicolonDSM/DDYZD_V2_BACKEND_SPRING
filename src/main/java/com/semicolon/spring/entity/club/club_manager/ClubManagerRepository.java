package com.semicolon.spring.entity.club.club_manager;

import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.user.User;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ClubManagerRepository extends CrudRepository<ClubManager, Integer> {
    Optional<ClubManager> findByClubAndUser(Club club, User user);
    @Transactional
    void deleteByClubAndUser(Club club, User user);
}
