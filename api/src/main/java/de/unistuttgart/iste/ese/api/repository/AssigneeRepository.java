package de.unistuttgart.iste.ese.api.repository;

import de.unistuttgart.iste.ese.api.model.Assignee;
import org.springframework.data.repository.CrudRepository;
public interface AssigneeRepository extends CrudRepository<Assignee, Long> {
    Assignee findById(long id);
}
