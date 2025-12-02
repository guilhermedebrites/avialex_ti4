package com.avialex.api.repository;

import com.avialex.api.model.entity.Process;
import com.avialex.api.model.enums.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long>, JpaSpecificationExecutor<Process> {
    List<Process> findByClientId_Name(String name);
    List<Process> findByClientId_Cpf(String cpf);
    List<Process> findByClientId_Id(Long userId);
    boolean existsByClientId_Id(Long userId);
    java.util.Optional<Process> findByProcessNumber(Integer processNumber);

    long countByStatusInAndCreationDateBetween(List<ProcessStatus> statuses, LocalDateTime start, LocalDateTime end);

    long countByCreationDateBetween(LocalDateTime start, LocalDateTime end);

    List<Process> findByCreationDateBetweenOrderByCreationDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.clientId.id) FROM Process p WHERE p.creationDate BETWEEN :start AND :end")
    long countDistinctClientsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT extract(year from p.creation_date)::int, extract(month from p.creation_date)::int, " +
            "COALESCE(SUM(CASE WHEN p.won = TRUE THEN 1 ELSE 0 END),0) AS won_count, " +
            "COALESCE(SUM(CASE WHEN p.status = 'COMPLETED' AND (p.won IS NULL OR p.won = FALSE) THEN 1 ELSE 0 END),0) AS lost_count, " +
            "COALESCE(SUM(p.recovered_value),0) " +
            "FROM process p WHERE p.creation_date BETWEEN :start AND :end " +
            "GROUP BY extract(year from p.creation_date), extract(month from p.creation_date) " +
            "ORDER BY extract(year from p.creation_date), extract(month from p.creation_date)",
            nativeQuery = true)
    List<Object[]> countGroupedByMonthAndWon(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}