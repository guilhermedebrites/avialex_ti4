package com.avialex.api.controller;

import com.avialex.api.model.dto.MainDashboardResponseDTO;
import com.avialex.api.model.dto.UpdateProcessStatusRequest;
import com.avialex.api.model.entity.Process;
import com.avialex.api.service.ProcessService;
import com.avialex.api.service.DashboardExportService;
import com.avialex.api.service.ExportedCsv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/process")
public class ProcessController {
    private final ProcessService processService;
    private final DashboardExportService dashboardExportService;

    @Autowired
    public ProcessController(ProcessService processService, DashboardExportService dashboardExportService) {
        this.processService = processService;
        this.dashboardExportService = dashboardExportService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<Process>> getAll() {
        return ResponseEntity.ok(processService.getAllProcesses());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Process> create(@RequestBody Process process) {
        return ResponseEntity.ok(processService.createProcess(process));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Process> update(@PathVariable Long id, @RequestBody Process process) {
        return ResponseEntity.ok(processService.updateProcess(id, process));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user/name/{name}")
    public ResponseEntity<List<Process>> getByUserName(@PathVariable String name) {
        return ResponseEntity.ok(processService.getProcessByUserName(name));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user/cpf/{cpf}")
    public ResponseEntity<List<Process>> getByUserCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(processService.getProcessByUserCpf(cpf));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Process> getById(@PathVariable Long id) {
        return processService.getProcessById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{processNumber}")
    public ResponseEntity<Process> getByProcessNumber(@PathVariable Integer processNumber) {
        return processService.getByProcessNumber(processNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Process> search(@RequestParam(required = false) Long id,
                                          @RequestParam(required = false) Integer processNumber) {
        if (id != null) {
            return processService.getProcessById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else if (processNumber != null) {
            return processService.getByProcessNumber(processNumber)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Process> updateStatus(@PathVariable Long id, @RequestBody UpdateProcessStatusRequest req) {
        return ResponseEntity.ok(processService.updateStatus(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        processService.deleteProcess(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LAWYER')")
    @GetMapping("/dashboard")
    public ResponseEntity<MainDashboardResponseDTO> dashboard(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(processService.getDashboard(startDate, endDate));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LAWYER')")
    @GetMapping(value = "/dashboard/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportDashboardCsv(
        @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ExportedCsv exported = dashboardExportService.exportDashboard(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", exported.filename());
        headers.setContentLength(exported.bytes().length);

        return ResponseEntity.ok()
            .headers(headers)
            .body(exported.bytes());
    }
}