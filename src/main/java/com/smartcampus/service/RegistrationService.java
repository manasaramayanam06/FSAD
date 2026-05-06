package com.smartcampus.service;

import com.smartcampus.dto.RegistrationDTO;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.entity.Student;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JavaMailSender mailSender;

    public Registration initiateRegistration(RegistrationDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Student student = studentRepository.findByEmail(dto.getEmail()).orElse(null);
        if (student == null) {
            student = new Student();
            student.setEmail(dto.getEmail());
            student.setName(dto.getName());
            student.setDepartment(dto.getDepartment());
            student = studentRepository.save(student);
        }

        // Check if already registered
        List<Registration> existing = registrationRepository.findByStudentId(student.getId());
        for (Registration r : existing) {
            if (r.getEvent().getId().equals(event.getId()) && "CONFIRMED".equals(r.getStatus())) {
                throw new RuntimeException("Student is already registered for this event.");
            }
        }

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setStudent(student);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus("PENDING");
        
        // Generate OTP
        String otp = String.format("%04d", new Random().nextInt(10000));
        registration.setOtp(otp);
        
        // SEND ACTUAL EMAIL
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(student.getEmail());
            message.setSubject("OTP for Event Registration: " + event.getTitle());
            message.setText("Dear " + student.getName() + ",\n\nYour OTP for the event '" + event.getTitle() + "' is: " + otp + "\n\nPlease enter this to complete your registration.\n\nRegards,\nSmart Campus Team");
            mailSender.send(message);
            System.out.println("Email sent successfully to " + student.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email. Check your SMTP configuration in application.properties.");
            System.err.println("Generated OTP (Fallback): " + otp);
        }

        return registrationRepository.save(registration);
    }

    public boolean verifyOtp(Long registrationId, String otp) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
                
        if ("PENDING".equals(registration.getStatus()) && otp.equals(registration.getOtp())) {
            registration.setStatus("CONFIRMED");
            registration.setOtp(null); // Clear OTP after use
            registrationRepository.save(registration);
            return true;
        }
        return false;
    }
}
