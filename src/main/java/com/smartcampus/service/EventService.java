package com.smartcampus.service;

import com.smartcampus.entity.Event;
import com.smartcampus.repository.EventRepository;
import com.smartcampus.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> searchEvents(String query) {
        if (query == null || query.isEmpty()) {
            return getAllEvents();
        }
        return eventRepository.findByDepartmentContainingIgnoreCaseOrTypeContainingIgnoreCase(query, query);
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public Map<Long, Long> getEventRegistrationStats() {
        Map<Long, Long> stats = new HashMap<>();
        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            long count = registrationRepository.countByEventIdAndStatus(event.getId(), "CONFIRMED");
            stats.put(event.getId(), count);
        }
        return stats;
    }
}
