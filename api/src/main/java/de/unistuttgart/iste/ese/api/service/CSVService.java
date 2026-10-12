package de.unistuttgart.iste.ese.api.service;

import de.unistuttgart.iste.ese.api.model.ToDo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.unistuttgart.iste.ese.api.utilites.CSVHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Service class providing methods regarding CSV-Export 
 */
@Service
public class CSVService {

    @Autowired
    ToDoService toDoService;

    public ByteArrayInputStream loadAll() throws IOException {
        List<ToDo> toDos = toDoService.getAllToDos();

        ByteArrayInputStream in = CSVHelper.toDosToCSV(toDos);
        return in;
    }

    public ByteArrayInputStream loadOpen() throws IOException {
        List<ToDo> toDos = toDoService.getAllToDos().stream().filter(t -> !t.isFinished()).toList();

        ByteArrayInputStream in = CSVHelper.toDosToCSV(toDos);
        return in;
    }
    public ByteArrayInputStream loadFinished() throws IOException {
        List<ToDo> toDos = toDoService.getAllToDos().stream().filter(t -> t.isFinished()).toList();

        ByteArrayInputStream in = CSVHelper.toDosToCSV(toDos);
        return in;
    }
}
