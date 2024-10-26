/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.unicauca.conferences.servicio;

import co.edu.unicauca.conferences.dao.ConferenceRepository;
import co.edu.unicauca.conferences.model.Conference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConferenceService {


    @Autowired
    private ConferenceRepository conferenceRepository;

    public Conference createConference(Conference conference) {
        return conferenceRepository.save(conference);
    }

    public List<Conference> getAllConferences() {
        return conferenceRepository.findAll();
    }

    public Optional<Conference> getConferenceById(Long id) {
        return conferenceRepository.findById(id);
    }

    public Conference updateConference(Long id, Conference conference) {
        // Verificar si existe la conferencia
        Conference existingConference = conferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conference not found"));
        
        // Actualizar propiedades
        existingConference.setName(conference.getName());
        existingConference.setStartDate(conference.getStartDate());
        existingConference.setEndDate(conference.getEndDate());
        existingConference.setLocation(conference.getLocation());
        existingConference.setTopics(conference.getTopics());
        
        return conferenceRepository.save(existingConference);
    }

    public void deleteConference(Long id) {
        // Verificar si existe la conferencia
        if (!conferenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Conference not found");
        }
        conferenceRepository.deleteById(id);
    }

    
}
