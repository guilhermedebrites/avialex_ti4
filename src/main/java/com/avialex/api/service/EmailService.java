package com.avialex.api.service;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.avialex.api.model.enums.ProcessStatus;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyClientOfProcessStatusChange(String to, Integer processNumber, ProcessStatus oldStatus, ProcessStatus newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Atualização do status do seu processo - Avialex");
        message.setText(
            "Olá! Gostaríamos de informar que o status do seu processo número " + processNumber +
            " foi atualizado de: " + oldStatus.toPtBr() + " para: " + newStatus.toPtBr() + ".\nEstamos à disposição para quaisquer dúvidas pelo contato projetoavialex@gmail.com.\n\n Atenciosamente,\n Equipe Avialex"
        );
        mailSender.send(message);
    }
}

