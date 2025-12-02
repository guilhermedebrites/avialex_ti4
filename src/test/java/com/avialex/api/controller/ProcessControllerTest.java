package com.avialex.api.controller;

import com.avialex.api.model.dto.MainDashboardResponseDTO;
import com.avialex.api.model.dto.UpdateProcessStatusRequest;
import com.avialex.api.model.entity.Process;
import com.avialex.api.model.enums.ProcessStatus;
import com.avialex.api.service.ProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessControllerTest {

    @Mock
    private ProcessService processService;

    @InjectMocks
    private ProcessController processController;

    private Process testProcess;

    @BeforeEach
    void setUp() {
        testProcess = new Process();
        testProcess.setId(1L);
        testProcess.setProcessNumber(12345);
        testProcess.setName("Test Process");
        testProcess.setStatus(ProcessStatus.CREATED);
    }

    @Test
    void testGetAll_ReturnsListOfProcesses() {
        List<Process> processes = Arrays.asList(testProcess);
        when(processService.getAllProcesses()).thenReturn(processes);

        ResponseEntity<List<Process>> response = processController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(processService, times(1)).getAllProcesses();
    }

    @Test
    void testCreate_ReturnsCreatedProcess() {
        when(processService.createProcess(any(Process.class))).thenReturn(testProcess);

        ResponseEntity<Process> response = processController.create(testProcess);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testProcess.getId(), response.getBody().getId());
        verify(processService, times(1)).createProcess(any(Process.class));
    }

    @Test
    void testGetById_WhenProcessExists_ReturnsProcess() {
        when(processService.getProcessById(1L)).thenReturn(Optional.of(testProcess));

        ResponseEntity<Process> response = processController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(processService, times(1)).getProcessById(1L);
    }

    @Test
    void testGetById_WhenProcessNotExists_ReturnsNotFound() {
        when(processService.getProcessById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Process> response = processController.getById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(processService, times(1)).getProcessById(999L);
    }

    @Test
    void testGetByProcessNumber_WhenExists_ReturnsProcess() {
        when(processService.getByProcessNumber(12345)).thenReturn(Optional.of(testProcess));

        ResponseEntity<Process> response = processController.getByProcessNumber(12345);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(12345, response.getBody().getProcessNumber());
        verify(processService, times(1)).getByProcessNumber(12345);
    }

    @Test
    void testUpdateStatus_ReturnsUpdatedProcess() {
        UpdateProcessStatusRequest request = new UpdateProcessStatusRequest(
                ProcessStatus.IN_PROGRESS,
                null,
                null
        );
        
        when(processService.updateStatus(eq(1L), any(UpdateProcessStatusRequest.class)))
            .thenReturn(testProcess);

        ResponseEntity<Process> response = processController.updateStatus(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(processService, times(1)).updateStatus(eq(1L), any(UpdateProcessStatusRequest.class));
    }

    @Test
    void testDelete_ReturnsNoContent() {
        doNothing().when(processService).deleteProcess(1L);

        ResponseEntity<Void> response = processController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(processService, times(1)).deleteProcess(1L);
    }

    @Test
    void testSearch_WithId_ReturnsProcess() {
        when(processService.getProcessById(1L)).thenReturn(Optional.of(testProcess));

        ResponseEntity<Process> response = processController.search(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(processService, times(1)).getProcessById(1L);
    }

    @Test
    void testSearch_WithProcessNumber_ReturnsProcess() {
        when(processService.getByProcessNumber(12345)).thenReturn(Optional.of(testProcess));

        ResponseEntity<Process> response = processController.search(null, 12345);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(processService, times(1)).getByProcessNumber(12345);
    }

    @Test
    void testSearch_WithoutParameters_ReturnsBadRequest() {
        ResponseEntity<Process> response = processController.search(null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(processService, never()).getProcessById(anyLong());
        verify(processService, never()).getByProcessNumber(anyInt());
    }

    @Test
    void testDashboard_ReturnsMainDashboardResponse() {
        MainDashboardResponseDTO mockDto = new MainDashboardResponseDTO(10L, 5L, null, 0L, null, 15L);
        when(processService.getDashboard(any(), any())).thenReturn(mockDto);

        ResponseEntity<MainDashboardResponseDTO> response = processController.dashboard(
            LocalDate.now().minusDays(30), 
            LocalDate.now()
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().activeProcess());
        verify(processService, times(1)).getDashboard(any(), any());
    }
}
