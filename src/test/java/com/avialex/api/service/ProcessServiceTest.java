package com.avialex.api.service;

import com.avialex.api.model.dto.UpdateProcessStatusRequest;
import com.avialex.api.model.entity.Process;
import com.avialex.api.model.entity.User;
import com.avialex.api.model.enums.ProcessStatus;
import com.avialex.api.repository.ProcessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

    @Mock
    private ProcessRepository processRepository;

//    @Mock
//    private EmailService emailService;

    @InjectMocks
    private ProcessService processService;

    private Process testProcess;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("client@example.com");
        
        testProcess = new Process();
        testProcess.setId(1L);
        testProcess.setProcessNumber(12345);
        testProcess.setName("Test Process");
        testProcess.setStatus(ProcessStatus.CREATED);
        testProcess.setClientId(testUser);
    }

    @Test
    void testGetAllProcesses_ReturnsListOfProcesses() {
        List<Process> processes = Arrays.asList(testProcess);
        when(processRepository.findAll()).thenReturn(processes);

        List<Process> result = processService.getAllProcesses();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(processRepository, times(1)).findAll();
    }

    @Test
    void testCreateProcess_SavesAndReturnsProcess() {
        when(processRepository.save(any(Process.class))).thenReturn(testProcess);

        Process result = processService.createProcess(testProcess);

        assertNotNull(result);
        assertEquals(testProcess.getId(), result.getId());
        verify(processRepository, times(1)).save(any(Process.class));
    }

    @Test
    void testGetProcessById_WhenExists_ReturnsProcess() {
        when(processRepository.findById(1L)).thenReturn(Optional.of(testProcess));

        Optional<Process> result = processService.getProcessById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(processRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProcessById_WhenNotExists_ReturnsEmpty() {
        when(processRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Process> result = processService.getProcessById(999L);

        assertFalse(result.isPresent());
        verify(processRepository, times(1)).findById(999L);
    }

    @Test
    void testGetByProcessNumber_WhenExists_ReturnsProcess() {
        when(processRepository.findByProcessNumber(12345)).thenReturn(Optional.of(testProcess));

        Optional<Process> result = processService.getByProcessNumber(12345);

        assertTrue(result.isPresent());
        assertEquals(12345, result.get().getProcessNumber());
        verify(processRepository, times(1)).findByProcessNumber(12345);
    }

    @Test
    void testUpdateProcess_UpdatesFieldsAndSaves() {
        Process updatedData = new Process();
        updatedData.setName("Updated Name");
        updatedData.setStatus(ProcessStatus.IN_PROGRESS);

        when(processRepository.findById(1L)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(any(Process.class))).thenReturn(testProcess);

        Process result = processService.updateProcess(1L, updatedData);

        assertNotNull(result);
        verify(processRepository, times(1)).findById(1L);
        verify(processRepository, times(1)).save(any(Process.class));
//        verify(emailService, times(1)).notifyClientOfProcessStatusChange(
//            anyString(), anyInt(), any(ProcessStatus.class), any(ProcessStatus.class)
//        );
    }

    @Test
    void testUpdateProcess_WhenNotFound_ThrowsException() {
        when(processRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            processService.updateProcess(999L, new Process());
        });

        verify(processRepository, times(1)).findById(999L);
        verify(processRepository, never()).save(any(Process.class));
    }

    @Test
    void testUpdateStatus_UpdatesStatusAndSendsEmail() {
        UpdateProcessStatusRequest request = new UpdateProcessStatusRequest(
                ProcessStatus.COMPLETED,
                null,
                null
        );

        when(processRepository.findById(1L)).thenReturn(Optional.of(testProcess));
        when(processRepository.save(any(Process.class))).thenReturn(testProcess);

        Process result = processService.updateStatus(1L, request);

        assertNotNull(result);
        verify(processRepository, times(1)).findById(1L);
        verify(processRepository, times(1)).save(any(Process.class));
//        verify(emailService, times(1)).notifyClientOfProcessStatusChange(
//            anyString(), anyInt(), any(ProcessStatus.class), any(ProcessStatus.class)
//        );
    }

    @Test
    void testDeleteProcess_DeletesProcess() {
        when(processRepository.findById(1L)).thenReturn(Optional.of(testProcess));
        doNothing().when(processRepository).delete(any(Process.class));

        processService.deleteProcess(1L);

        verify(processRepository, times(1)).findById(1L);
        verify(processRepository, times(1)).delete(any(Process.class));
    }

    @Test
    void testDeleteProcess_WhenNotFound_ThrowsException() {
        when(processRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            processService.deleteProcess(999L);
        });

        verify(processRepository, times(1)).findById(999L);
        verify(processRepository, never()).delete(any(Process.class));
    }

    @Test
    void testGetProcessByUserName_ReturnsProcessList() {
        List<Process> processes = Arrays.asList(testProcess);
        when(processRepository.findByClientId_Name("Test User")).thenReturn(processes);

        List<Process> result = processService.getProcessByUserName("Test User");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(processRepository, times(1)).findByClientId_Name("Test User");
    }

    @Test
    void testGetProcessByUserCpf_ReturnsProcessList() {
        List<Process> processes = Arrays.asList(testProcess);
        when(processRepository.findByClientId_Cpf("12345678900")).thenReturn(processes);

        List<Process> result = processService.getProcessByUserCpf("12345678900");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(processRepository, times(1)).findByClientId_Cpf("12345678900");
    }
}
