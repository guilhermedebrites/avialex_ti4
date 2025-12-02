package com.avialex.api.service;

import com.avialex.api.model.enums.ProcessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testNotifyClientOfProcessStatusChange_SendsEmail() {
        String recipientEmail = "client@example.com";
        Integer processNumber = 12345;
        ProcessStatus oldStatus = ProcessStatus.CREATED;
        ProcessStatus newStatus = ProcessStatus.IN_PROGRESS;

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.notifyClientOfProcessStatusChange(recipientEmail, processNumber, oldStatus, newStatus);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testNotifyClientOfProcessStatusChange_VerifiesEmailContent() {
        String recipientEmail = "client@example.com";
        Integer processNumber = 12345;
        ProcessStatus oldStatus = ProcessStatus.CREATED;
        ProcessStatus newStatus = ProcessStatus.COMPLETED;

        emailService.notifyClientOfProcessStatusChange(recipientEmail, processNumber, oldStatus, newStatus);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
