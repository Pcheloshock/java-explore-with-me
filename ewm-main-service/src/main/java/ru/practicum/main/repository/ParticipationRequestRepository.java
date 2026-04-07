package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long userId);

    List<ParticipationRequest> findByEventId(Long eventId);

    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT r.event.id, COUNT(r) FROM ParticipationRequest r " +
           "WHERE r.event.id IN :eventIds AND r.status = :status " +
           "GROUP BY r.event.id")
    List<Object[]> countConfirmedRequestsByEventIdsGrouped(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);

    default Map<Long, Long> countConfirmedRequestsByEventIds(List<Long> eventIds) {
        List<Object[]> results = countConfirmedRequestsByEventIdsGrouped(eventIds, RequestStatus.CONFIRMED);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
