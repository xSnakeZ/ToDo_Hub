package de.unistuttgart.iste.ese.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.unistuttgart.iste.ese.api.ApiVersion1;

import de.unistuttgart.iste.ese.api.model.ToDo;
import de.unistuttgart.iste.ese.api.service.AssigneeService;
import de.unistuttgart.iste.ese.api.service.CSVService;
import de.unistuttgart.iste.ese.api.service.ToDoService;
import de.unistuttgart.iste.ese.api.utilites.Target;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Controller for the ToDo-Endpoints
 */
@ApiVersion1
@RestController
public class ToDoController {

    @Autowired
    ToDoService toDoService;
    @Autowired
    AssigneeService assigneeService;
    @Autowired
    CSVService fileService;

    @PostConstruct
    public void init() {

        if (toDoService.getAllToDos().isEmpty()) {
            ToDo cleanRoom = new ToDo("Clean Room", "Clean the room and throw away the trash", new ArrayList<>(),
                Date.from((Instant.now().plusSeconds(600))));
            toDoService.saveToDo(cleanRoom);
            ToDo emptyDishwasher = new ToDo("Empty Dishwasher", "Empty the dishwasher", new ArrayList<>(),
                Date.from((Instant.now().plusSeconds(600))));

            toDoService.saveToDo(emptyDishwasher);
            ToDo groceryShopping = new ToDo("Grocery Shopping", "Buy groceries for the week", new ArrayList<>(),
                Date.from((Instant.now().plusSeconds(600))));

            toDoService.saveToDo(groceryShopping);
            ToDo doLaundry = new ToDo("Do Laundry", "Do the laundry and fold the clothes", new ArrayList<>(),
                Date.from((Instant.now().plusSeconds(600))));

            toDoService.saveToDo(doLaundry);
            ToDo repairDoor = new ToDo("Repair Door", "Fix the broken door in the living room", new ArrayList<>(),
                Date.from((Instant.now().plusSeconds(30))));
            toDoService.saveToDo(repairDoor);
        }
    }

    @GetMapping("/todos")
    public List<ToDo> getAllToDos() {
        return toDoService.getAllToDos();
    }

    @GetMapping("/todos/{id}")
    public ToDo getToDo(@PathVariable("id") long id) {
        return toDoService.getToDoById(id);
    }

    @PostMapping("/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public ToDo postToDo(@RequestBody String requestBody) throws JsonProcessingException {
        ToDo toDo = toDoService.createToDoFromString(requestBody);
        return toDoService.saveToDo(toDo);
    }

    @PutMapping("/todos/{id}")
    public ToDo putToDo(@RequestBody String requestBody, @PathVariable("id") long id) {
        ToDo toDoToUpdate = toDoService.getToDoById(id);
        if (toDoToUpdate != null) {
            ToDo updatedToDo = toDoService.updateToDo(requestBody, id);
            return toDoService.saveToDo(updatedToDo);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ToDo with ID %s not found!", id));
    }

    @DeleteMapping("/todos/{id}")
    public ToDo deleteToDo(@PathVariable("id") long id) {
        ToDo toDoToDelete = toDoService.getToDoById(id);
        if (toDoToDelete != null) {
            toDoService.deleteAssignment(id, Target.TODO);
            toDoService.deleteToDoById(id);
            return toDoToDelete;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ToDo with ID %s not found!", id));
    }

    @GetMapping("/csv-downloads/todos")
    public ResponseEntity<Resource> getToDosCSV() throws IOException {
        String filename = "todos.csv";
        InputStreamResource file = new InputStreamResource(fileService.loadAll());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(file);
    }

    @GetMapping("/csv-downloads/todos/open")
    public ResponseEntity<Resource> getToDosCSVOpen() throws IOException {
        String filename = "open_todos.csv";
        InputStreamResource file = new InputStreamResource(fileService.loadOpen());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(file);
    }

    @GetMapping("/csv-downloads/todos/finished")
    public ResponseEntity<Resource> getToDosCSVFinished() throws IOException {
        String filename = "finished_todos.csv";
        InputStreamResource file = new InputStreamResource(fileService.loadFinished());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(file);
    }
}

