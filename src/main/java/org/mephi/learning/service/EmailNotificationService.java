package org.mephi.learning.service;

import java.util.Collection;
import java.util.List;
import org.mephi.learning.entity.Assignment;
import org.mephi.learning.entity.Submission;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final String from;

    public EmailNotificationService(
            ObjectProvider<JavaMailSender> mailSenders, Environment environment) {
        this.mailSenders = mailSenders;
        this.from = environment.getProperty("app.mail.from", "no-reply@learning.local");
    }

    public void assignmentAdded(Assignment assignment, Collection<String> recipients) {
        send(
                recipients,
                "Новое задание: " + assignment.getTitle(),
                "В курсе «"
                        + assignment.getLesson().getModule().getCourse().getTitle()
                        + "» добавлено новое задание «"
                        + assignment.getTitle()
                        + "». Срок выполнения: "
                        + (assignment.getDueDate() == null ? "не указан" : assignment.getDueDate())
                        + ".");
    }

    public void submissionGraded(Submission submission) {
        send(
                List.of(submission.getStudent().getEmail()),
                "Оценена работа: " + submission.getAssignment().getTitle(),
                "Ваша работа по заданию «"
                        + submission.getAssignment().getTitle()
                        + "» оценена на "
                        + submission.getScore()
                        + " из "
                        + submission.getAssignment().getMaxScore()
                        + ".\nКомментарий: "
                        + (submission.getFeedback() == null ? "нет" : submission.getFeedback()));
    }

    private void send(Collection<String> recipients, String subject, String text) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) return;
        for (String recipient : recipients) {
            if (recipient == null || recipient.isBlank()) continue;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(text);
            try {
                sender.send(message);
            } catch (MailException ignored) {
                // Delivery failures must not roll back saved academic data.
            }
        }
    }
}
