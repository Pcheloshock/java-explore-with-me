package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query(value = "SELECT h.app, h.uri, COUNT(h.ip) as hits " +
            "FROM hits h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC", nativeQuery = true)
    List<Object[]> getAllStats(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query(value = "SELECT h.app, h.uri, COUNT(DISTINCT h.ip) as hits " +
            "FROM hits h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC", nativeQuery = true)
    List<Object[]> getUniqueStats(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query(value = "SELECT h.app, h.uri, COUNT(h.ip) as hits " +
            "FROM hits h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC", nativeQuery = true)
    List<Object[]> getAllStatsByUris(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);

    @Query(value = "SELECT h.app, h.uri, COUNT(DISTINCT h.ip) as hits " +
            "FROM hits h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC", nativeQuery = true)
    List<Object[]> getUniqueStatsByUris(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("uris") List<String> uris);
}
