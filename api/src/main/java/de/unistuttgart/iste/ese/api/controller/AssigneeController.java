package de.unistuttgart.iste.ese.api.controller;

import de.unistuttgart.iste.ese.api.ApiVersion1;
import de.unistuttgart.iste.ese.api.model.Assignee;
import de.unistuttgart.iste.ese.api.service.AssigneeService;
import de.unistuttgart.iste.ese.api.service.ToDoService;
import de.unistuttgart.iste.ese.api.utilites.Target;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for the Assignee-Endpoints
 */
@ApiVersion1
@RestController
public class AssigneeController {

    @Autowired
    AssigneeService assigneeService;
    @Autowired
    ToDoService toDoService;

    @PostConstruct
    public void init() {
        
        if (assigneeService.getAllAssignees().isEmpty()) {
           Assignee robert = new Assignee("Robert", "Schwab", "robert.schwab@uni-stuttgart.de");
           assigneeService.saveAssignee(robert);
           Assignee mark = new Assignee("Mark", "Müller", "mark.muelller@uni-stuttgart.de");
           assigneeService.saveAssignee(mark);
           Assignee julia = new Assignee("Julia", "Mayer", "julia.mayer@uni-stuttgart.de");
           assigneeService.saveAssignee(julia);
        }
    }
    
    @GetMapping("/assignees")
    public List<Assignee> getAllAssignees() {
        return assigneeService.getAllAssignees();
    }

    @GetMapping("/assignees/{id}")
    public Assignee getAssignee(@PathVariable("id") long id) {
        return assigneeService.getAssigneeById(id);
    }

    @PostMapping("/assignees")
    @ResponseStatus(HttpStatus.CREATED)
    public Assignee postAssignee(@Valid @RequestBody Assignee requestBody) {
        Assignee newAssignee = new Assignee(requestBody.getPrename(), requestBody.getName(), requestBody.getEmail());
        return assigneeService.saveAssignee(newAssignee);

    }
    
    @PutMapping("/assignees/{id}")
    public Assignee putAssignee(@Valid @RequestBody Assignee requestBody, @PathVariable("id") long id) {
        requestBody.setId(id);
        Assignee assigneeToUpdate = assigneeService.getAssigneeById(id);
        if (assigneeToUpdate != null) {
            return assigneeService.saveAssignee(requestBody);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Assignee with ID %s not found!", id));
    }
    
    @DeleteMapping("/assignees/{id}")
    public Assignee deleteAssignee(@PathVariable("id") long id) {
        Assignee assigneeToDelete = assigneeService.getAssigneeById(id);
        if (assigneeToDelete != null) {
            toDoService.deleteAssignment(id, Target.ASSIGNEE);
            assigneeService.deleteAssigneeById(id);
            return assigneeToDelete;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Assignee with ID %s not found!", id));
    }
}
