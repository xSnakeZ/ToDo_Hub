package de.unistuttgart.iste.ese.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.unistuttgart.iste.ese.api.model.Assignee;
import de.unistuttgart.iste.ese.api.model.ToDo;
import de.unistuttgart.iste.ese.api.repository.ToDoRepository;
import de.unistuttgart.iste.ese.api.utilites.Target;
import de.unistuttgart.iste.ese.api.utilites.TodoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service for the ToDo-Controller
 */

@Service
public class ToDoService {

    @Autowired
    ToDoRepository toDoRepository;
    @Autowired
    AssigneeService assigneeService;

    public List<ToDo> getAllToDos() {
        return (List<ToDo>) toDoRepository.findAll();
    }

    public ToDo getToDoById(long id) {
        return toDoRepository.findById(id);
    }

    public ToDo saveToDo(ToDo todo) {
        return toDoRepository.save(todo);
    }

    public void deleteToDoById(long id) {
        toDoRepository.deleteById(id);
    }
    
    public void deleteAssignment(long id, Target target) {
        switch (target) {
            case TODO: {
                ToDo toDo = toDoRepository.findById(id);
                List<Assignee> assigneeList = toDo.getAssigneeList();
                toDo.getAssigneeList().removeAll(assigneeList);
            }
            case ASSIGNEE: {
                for (ToDo t : toDoRepository.findAll()) {
                    Assignee assignee = assigneeService.getAssigneeById(id);
                    t.getAssigneeList().remove(assignee);
                }
            }
        }
    }

    /**
     * Creates a ToDo from a String. The String must be a valid JSON-String.
     *
     * @param s JSON-String
     * @return ToDo
     * @throws JsonProcessingException if the String is not a valid JSON-String
     */

    public ToDo createToDoFromString(String s) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String title = null;
        String description = null;
        List<Assignee> assigneeList = new ArrayList<>();
        Date dueDate = null;

        try {
            JsonNode toDoJsonNode = objectMapper.readTree(s);
            if (toDoJsonNode.has("title")) {
                title = toDoJsonNode.get("title").asText();
                if (title.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title needs at least one char");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is missing");
            }
            if (toDoJsonNode.has("description")) {
                description = toDoJsonNode.get("description").asText();
            } else {
                description = "";
            }
            if (toDoJsonNode.has("assigneeIdList")) {
                JsonNode assigneeIdList = toDoJsonNode.get("assigneeIdList");
                if (assigneeIdList.isArray()) {
                    assigneeCheck((ArrayNode) assigneeIdList, assigneeList);
                }
            }
            if (toDoJsonNode.has("dueDate")) {
                if(toDoJsonNode.get("dueDate").asText().equals("null")){
                    dueDate = null;
                } else {
                    String dueDateString = toDoJsonNode.get("dueDate").asText();
                    if (dueDateString.matches("^\\d{13}$")) {
                        dueDate = new Date(toDoJsonNode.get("dueDate").asLong());
                        if (dueDate.before(new Date(System.currentTimeMillis()))) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DueDate is in the past");
                        }
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timestamp");
                    } 
                }
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        }
        ToDo createdToDo = new ToDo(title, description, assigneeList, dueDate);
        createdToDo.setCategory(predictCategoryFromTitle(title));
        return createdToDo;
    }

    /**
     * Updates an existing ToDo from a String. The String must be a valid JSON-String.
     *
     * @param s JSON-String
     * @param id id of the ToDo to be updated
     * @return ToDo
     */
    public ToDo updateToDo(String s, long id) {
        ObjectMapper objectMapper = new ObjectMapper();
        ToDo toDo = toDoRepository.findById(id);
        try {
            JsonNode toDoJsonNode = objectMapper.readTree(s);
            if (toDoJsonNode.has("title")) {
                String title = toDoJsonNode.get("title").asText();
                if (title.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title needs at least one char");
                }
                toDo.setTitle(title);
                toDo.setCategory(predictCategoryFromTitle(title));
            }
            if (toDoJsonNode.has("description")) {
                toDo.setDescription(toDoJsonNode.get("description").asText());
            }
            if (toDoJsonNode.has("assigneeIdList")) {
                JsonNode assigneeIdList = toDoJsonNode.get("assigneeIdList");
                List<Assignee> newAssigneeList = new ArrayList<>();
                if (assigneeIdList.isArray()) {
                    assigneeCheck((ArrayNode) assigneeIdList, newAssigneeList);
                    toDo.setAssigneeList(newAssigneeList);
                }
            }
            if (toDoJsonNode.has("dueDate")) {
                if (toDoJsonNode.get("dueDate").asText().equals("null")) {
                    toDo.setDueDate(null);
                } else {
                    String dueDateString = toDoJsonNode.get("dueDate").asText();
                    if (dueDateString.matches("^\\d{13}$")) {
                        toDo.setDueDate(new Date(toDoJsonNode.get("dueDate").asLong()));
                        if (toDo.getDueDate().before(toDo.getCreatedDate())) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date is before creation date");
                        }
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timestamp");
                    }
                }
            }
                    //No Validation since this is just needed for PUT-Methods on existing ToDos
                    if (toDoJsonNode.has("finished")) {
                        boolean finished = toDoJsonNode.get("finished").asBoolean();
                        if (finished) {
                            Date finishedDate = new Date(System.currentTimeMillis());
                            toDo.setFinishedDate(finishedDate);
                        } else {
                            toDo.setFinishedDate(null);
                        }
                        toDo.setFinished(finished);
                    }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return toDo;
    }

    /**
     * Checks if the assignees in the assigneeIdList exist and are not duplicated
     * @param assigneeIdList ArrayNode containing the assigneeIds
     * @param newAssigneeList List of Assignees
     */ 
    private void assigneeCheck(ArrayNode assigneeIdList, List<Assignee> newAssigneeList) {
        for (int i = 0; i < assigneeIdList.size(); i++) {
            long assigneeId = assigneeIdList.get(i).asLong();
            newAssigneeList.add(assigneeService.getAssigneeById(assigneeId));
        }
        if (!(assigneeService.allAssigneesExistAndNoDuplicates(newAssigneeList))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignees do not exist or are duplicated");
        }
    }

    /**
     * Predicts the category of a ToDo based on its title
     * @param title title of the ToDo
     * @return predicted category
     */
    private String predictCategoryFromTitle(String title) {
        String modelPath = "model.pmml";
        TodoModel todoModel = new TodoModel(modelPath);
        String categoryPrediction = todoModel.predictClass(title);
        todoModel.unloadModel();
        return categoryPrediction;
    }
    
    
}
