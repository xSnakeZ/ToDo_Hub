package de.unistuttgart.iste.ese.api.service;

import de.unistuttgart.iste.ese.api.model.Assignee;
import de.unistuttgart.iste.ese.api.repository.AssigneeRepository;
import de.unistuttgart.iste.ese.api.repository.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for the Assignee-Controller
 */
@Service
public class AssigneeService {

    @Autowired
    AssigneeRepository assigneeRepository;
    @Autowired
    ToDoRepository toDoRepository;

    public List<Assignee> getAllAssignees() {
        return (List<Assignee>) assigneeRepository.findAll();
    }

    public Assignee getAssigneeById(long id) {
        return assigneeRepository.findById(id);
    }

    public Assignee saveAssignee(Assignee assignee) {
        return assigneeRepository.save(assignee);
    }

    public void deleteAssigneeById(long id) {
        assigneeRepository.deleteById(id);
    }
    public boolean allAssigneesExistAndNoDuplicates(List<Assignee> assigneeList) {
        List<Assignee> uniqueAssignees = new ArrayList<>();
        if (!(assigneeList.isEmpty())) {
            for (Assignee assignee : assigneeList) {
                if (assignee == null || uniqueAssignees.contains(assignee)) {
                    return false;
                }
                uniqueAssignees.add(assignee);
            }
        }
        return true;
    }

}
