package com.avialex.api.service;

import com.avialex.api.model.dto.MainDashboardResponseDTO;
import com.avialex.api.model.dto.MonthlyProcessStats;
import com.avialex.api.model.dto.UpdateProcessStatusRequest;
import com.avialex.api.model.entity.Process;
import com.avialex.api.model.enums.ProcessStatus;
import com.avialex.api.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessService {
    private final ProcessRepository processRepository;
//    private final EmailService emailService;

    @Autowired
    public ProcessService(ProcessRepository processRepository) {
        this.processRepository = processRepository;
//        this.emailService = emailService;
    }

    public java.util.Optional<Process> getByProcessNumber(Integer processNumber) {
        return processRepository.findByProcessNumber(processNumber);
    }

    public List<Process> getAllProcesses() {
        return processRepository.findAll();
    }

    @Transactional
    public Process createProcess(Process process) {
        return processRepository.save(process);
    }

    @Transactional
    public Process updateProcess(Long id, Process updated) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found"));
        if (updated.getName() != null) {
            process.setName(updated.getName());
        }
        if (updated.getInvolvedParties() != null) {
            process.setInvolvedParties(updated.getInvolvedParties());
        }
        if (updated.getProcessNumber() != null) {
            process.setProcessNumber(updated.getProcessNumber());
        }
        boolean statusChanged = false;
        ProcessStatus oldStatus = process.getStatus();
        if (updated.getStatus() != null && !updated.getStatus().equals(process.getStatus())) {
            process.setStatus(updated.getStatus());
            statusChanged = true;
        }
        if (updated.getClientId() != null) {
            process.setClientId(updated.getClientId());
        }
        if (updated.getRecoveredValue() != null) {
            process.setRecoveredValue(updated.getRecoveredValue());
        }
        if (updated.getWon() != null) {
            process.setWon(updated.getWon());
        }
        Process returnProcess = processRepository.save(process);
//        if (statusChanged && returnProcess.getClientId() != null && returnProcess.getClientId().getEmail() != null) {
//            emailService.notifyClientOfProcessStatusChange(returnProcess.getClientId().getEmail(), returnProcess.getProcessNumber(), oldStatus, returnProcess.getStatus());
//        }
        return returnProcess;
    }

    @Transactional
    public Process updateStatus(Long id, UpdateProcessStatusRequest req) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found"));
        boolean statusChanged = false;
        ProcessStatus oldStatus = process.getStatus();

        if (req.status() != null && !req.status().equals(process.getStatus())) {
            process.setStatus(req.status());
            statusChanged = true;
        }

        if (req.recoveredValue() != null) {
            process.setRecoveredValue(req.recoveredValue());
        }

        if (req.won() != null) {
            process.setWon(req.won());
        } else {
            if (req.status() == ProcessStatus.COMPLETED) {
                process.setWon(Boolean.TRUE);
            }
        }

        Process saved = processRepository.save(process);

//        if (statusChanged && saved.getClientId() != null && saved.getClientId().getEmail() != null) {
//            emailService.notifyClientOfProcessStatusChange(saved.getClientId().getEmail(), saved.getProcessNumber(), oldStatus, saved.getStatus());
//        }

        return saved;
    }

    @Transactional
    public Process updateStatus(Long id, ProcessStatus status) {
        UpdateProcessStatusRequest req = new UpdateProcessStatusRequest(status, null, null);
        return updateStatus(id, req);
    }

    public List<Process> getProcessByUserName(String nome) {
        return processRepository.findByClientId_Name(nome);
    }

    public List<Process> getProcessByUserCpf(String cpf) {
        return processRepository.findByClientId_Cpf(cpf);
    }

    public Optional<Process> getProcessById(Long id) {
        return processRepository.findById(id);
    }

    @Transactional
    public void deleteProcess(Long id) {
        Process process = processRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process not found"));
        processRepository.delete(process);
    }

    public MainDashboardResponseDTO getDashboard(LocalDate startDate, LocalDate endDate) {
        YearMonth defaultMonth = YearMonth.now().minusMonths(1);
        LocalDate start = (startDate != null) ? startDate : defaultMonth.atDay(1);
        LocalDate end = (endDate != null) ? endDate : defaultMonth.atEndOfMonth();
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<ProcessStatus> activeStatuses = List.of(ProcessStatus.CREATED, ProcessStatus.IN_PROGRESS);
        long activeProcesses = processRepository.countByStatusInAndCreationDateBetween(activeStatuses, startDateTime, endDateTime);

        long activeClients = processRepository.countDistinctClientsBetween(startDateTime, endDateTime);

        List<Object[]> grouped = processRepository.countGroupedByMonthAndWon(startDateTime, endDateTime);

        Map<YearMonth, long[]> monthMap = new LinkedHashMap<>();
        long totalWon = 0L;
        long totalLost = 0L;
        BigDecimal totalRecovered = BigDecimal.ZERO;

        for (Object[] row : grouped) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long wonCount = (row[2] != null) ? ((Number) row[2]).longValue() : 0L;
            long lostCount = (row[3] != null) ? ((Number) row[3]).longValue() : 0L;
            Object sumObj = row[4];
            BigDecimal sumRecovered = BigDecimal.ZERO;
            if (sumObj != null) {
                if (sumObj instanceof BigDecimal) {
                    sumRecovered = (BigDecimal) sumObj;
                } else {
                    sumRecovered = new BigDecimal(sumObj.toString());
                }
            }

            YearMonth ym = YearMonth.of(year, month);
            monthMap.putIfAbsent(ym, new long[]{0L, 0L});
            monthMap.get(ym)[0] += wonCount;
            monthMap.get(ym)[1] += lostCount;

            totalWon += wonCount;
            totalLost += lostCount;
            totalRecovered = totalRecovered.add(sumRecovered);
        }

        Locale pt = new Locale("pt", "BR");
        List<MonthlyProcessStats> monthlyStats = monthMap.entrySet().stream()
                .map(e -> {
                    YearMonth ym = e.getKey();
                    String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, pt);
                    monthName = monthName.substring(0,1).toUpperCase(pt) + monthName.substring(1);
                    String label = monthName + "/" + ym.getYear();
                    long wonCount = e.getValue()[0];
                    long lostCount = e.getValue()[1];
                    return new MonthlyProcessStats(label, wonCount, lostCount);
                })
                .collect(Collectors.toList());

        long successFeePercent = 0L;
        long denom = totalWon + totalLost;
        if (denom > 0) {
            successFeePercent = Math.round((totalWon * 100.0) / denom);
        }

        long totalProcesses = processRepository.countByCreationDateBetween(startDateTime, endDateTime);

        return new MainDashboardResponseDTO(activeProcesses, activeClients, totalRecovered, successFeePercent, monthlyStats, totalProcesses);
    }
}
