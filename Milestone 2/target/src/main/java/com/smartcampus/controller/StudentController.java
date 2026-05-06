package com.smartcampus.controller;

import com.smartcampus.dto.RegistrationDTO;
import com.smartcampus.entity.Event;
import com.smartcampus.entity.Registration;
import com.smartcampus.service.EventService;
import com.smartcampus.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentController {

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "index";
    }

    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id).orElse(null);
        if (event == null) {
            return "redirect:/";
        }
        model.addAttribute("event", event);
        return "event-details";
    }

    @GetMapping("/register/{eventId}")
    public String showRegistrationForm(@PathVariable Long eventId, Model model) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setEventId(eventId);
        model.addAttribute("registrationDTO", dto);
        model.addAttribute("event", eventService.getEventById(eventId).orElse(null));
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registrationDTO") RegistrationDTO dto, 
                                      BindingResult result, 
                                      Model model, 
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("event", eventService.getEventById(dto.getEventId()).orElse(null));
            return "register";
        }

        try {
            Registration reg = registrationService.initiateRegistration(dto);
            redirectAttributes.addFlashAttribute("message", "OTP sent to your email. Please verify.");
            return "redirect:/verify-otp/" + reg.getId();
        } catch (Exception e) {
            model.addAttribute("event", eventService.getEventById(dto.getEventId()).orElse(null));
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify-otp/{regId}")
    public String showOtpForm(@PathVariable Long regId, Model model) {
        model.addAttribute("regId", regId);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam Long regId, @RequestParam String otp, RedirectAttributes redirectAttributes) {
        boolean success = registrationService.verifyOtp(regId, otp);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Registration Successful!");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid OTP or registration expired.");
            return "redirect:/verify-otp/" + regId;
        }
    }
}
