package co.edu.unicauca.conferences.presentacion;

import co.edu.unicauca.conferences.dto.UserDTO;
import co.edu.unicauca.conferences.model.Conference;
import co.edu.unicauca.conferences.servicio.*;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/conferences")
public class ConferenceController {

    @Autowired
    private ConferenceService conferenceService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${session.service.url}")
    private String sessionServiceUrl;
@PostMapping
public ResponseEntity<String> createConference(@RequestBody Conference conference, @RequestParam Long userId) {
    // Llamar al microservicio de sesión para obtener la información del usuario
    ResponseEntity<UserDTO> response = restTemplate.getForEntity(sessionServiceUrl + "/api/users/" + userId, UserDTO.class);

    if (response.getStatusCode().is2xxSuccessful()) {
        UserDTO loggedUser = response.getBody();

        // Verificar que el usuario tenga el rol de organizador
        if (!"Organizador".equals(loggedUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Solo los organizadores pueden crear conferencias.");
        }

        // Asignar el userId como organizerId a la conferencia
        conference.setOrganizerId(userId);  // Asigna el organizador

        // Crear conferencia
        Conference createdConference = conferenceService.createConference(conference);
        return ResponseEntity.status(HttpStatus.CREATED).body("Conferencia creada con éxito.");
    } else {
        return ResponseEntity.status(response.getStatusCode()).body("Error al obtener información del usuario.");
    }
}

    @GetMapping
    public ResponseEntity<List<Conference>> getAllConferences() {
        List<Conference> conferences = conferenceService.getAllConferences();
        return ResponseEntity.ok(conferences);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conference> getConferenceById(@PathVariable Long id) {
        Optional<Conference> conference = conferenceService.getConferenceById(id);
        return conference.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

@PutMapping("/{id}")
public ResponseEntity<String> updateConference(@PathVariable Long id, @RequestBody Conference conference, @RequestParam Long userId) {
    ResponseEntity<UserDTO> response = restTemplate.getForEntity(sessionServiceUrl + "/api/users/" + userId, UserDTO.class);

    if (response.getStatusCode().is2xxSuccessful()) {
        UserDTO loggedUser = response.getBody();

        if (!"Organizador".equals(loggedUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Solo los organizadores pueden actualizar conferencias.");
        }

        // Verificar si la conferencia existe
        Optional<Conference> existingConference = conferenceService.getConferenceById(id);
        if (existingConference.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conferencia no encontrada.");
        }

        // Verificar que el usuario sea el organizador que creó la conferencia
        if (!existingConference.get().getOrganizerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Solo el organizador que creó la conferencia puede actualizarla.");
        }

        // Actualizar conferencia
        conferenceService.updateConference(id, conference);
        return ResponseEntity.ok("Conferencia actualizada exitosamente.");
    } else {
        return ResponseEntity.status(response.getStatusCode()).body("Error al obtener información del usuario.");
    }
}

@DeleteMapping("/{id}")
public ResponseEntity<String> deleteConference(@PathVariable Long id, @RequestParam Long userId) {
    ResponseEntity<UserDTO> response = restTemplate.getForEntity(sessionServiceUrl + "/api/users/" + userId, UserDTO.class);

    if (response.getStatusCode().is2xxSuccessful()) {
        UserDTO loggedUser = response.getBody();

        if (!"Organizador".equals(loggedUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Solo los organizadores pueden eliminar conferencias.");
        }

        // Verificar si la conferencia existe
        Optional<Conference> existingConference = conferenceService.getConferenceById(id);
        if (existingConference.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conferencia no encontrada.");
        }

        // Verificar que el usuario sea el organizador que creó la conferencia
        if (!existingConference.get().getOrganizerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. Solo el organizador que creó la conferencia puede eliminarla.");
        }

        // Eliminar conferencia
        conferenceService.deleteConference(id);
        return ResponseEntity.ok("Conferencia eliminada exitosamente.");
    } else {
        return ResponseEntity.status(response.getStatusCode()).body("Error al obtener información del usuario.");
    }
}


}