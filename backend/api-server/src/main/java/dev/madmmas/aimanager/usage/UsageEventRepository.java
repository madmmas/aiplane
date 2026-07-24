package dev.madmmas.aimanager.usage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsageEventRepository extends JpaRepository<UsageEvent, String> {

  List<UsageEvent> findByProjectIdAndTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
      String projectId, Instant from, Instant to, Pageable pageable);

  @Query(
      """
      SELECT e.provider,
             COUNT(e),
             COALESCE(SUM(e.inputTokens), 0),
             COALESCE(SUM(e.outputTokens), 0),
             COALESCE(SUM(e.costUsd), 0)
      FROM UsageEvent e
      WHERE e.projectId = :projectId
        AND e.timestamp >= :from
        AND e.timestamp < :to
      GROUP BY e.provider
      ORDER BY e.provider
      """)
  List<Object[]> aggregateByProvider(
      @Param("projectId") String projectId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(
      """
      SELECT COALESCE(SUM(e.costUsd), 0)
      FROM UsageEvent e
      WHERE e.projectId = :projectId
        AND e.timestamp >= :from
        AND e.timestamp < :to
      """)
  BigDecimal sumCostUsd(
      @Param("projectId") String projectId, @Param("from") Instant from, @Param("to") Instant to);
}
