package com.smartcampus.controller;

import com.smartcampus.entity.Event;
import com.smartcampus.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EventService eventService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("events", eventService.searchEvents(search));
        Map<Long, Long> stats = eventService.getEventRegistrationStats();
        model.addAttribute("stats", stats);
        model.addAttribute("searchQuery", search);
        return "admin/dashboard";
    }

    @GetMapping("/event/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "admin/event-form";
    }

    @PostMapping("/event/save")
    public String saveEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/event-form";
        }
        eventService.saveEvent(event);
        redirectAttributes.addFlashAttribute("success", "Event saved successfully");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/event/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id).orElse(null);
        if (event == null) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("event", event);
        return "admin/event-form";
    }

    @GetMapping("/event/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("success", "Event deleted successfully");
        return "redirect:/admin/dashboard";
    }
}
