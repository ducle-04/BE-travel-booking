package com.travel.travelbooking.repository;

import com.travel.travelbooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<User, Long> {

    @Query("SELECT COUNT(u) FROM User u WHERE u.status <> 'DELETED'")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'DELETED'")
    long countDeletedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status IN ('INACTIVE', 'BANNED')")
    long countInactiveOrBanned();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfDay AND u.status <> 'DELETED'")
    long countNewUsersToday(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.status <> 'DELETED'")
    long countUsersByRole(@Param("roleName") String roleName);

    @Query("""
        SELECT FUNCTION('DATE', u.createdAt), COUNT(u)
        FROM User u
        WHERE u.createdAt >= :sevenDaysAgo AND u.status <> 'DELETED'
        GROUP BY FUNCTION('DATE', u.createdAt)
        ORDER BY FUNCTION('DATE', u.createdAt)
        """)
    List<Object[]> countNewUsersLast7Days(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
    @Query("SELECT COUNT(r) FROM Review r")
    long countTotalReviews();
}
