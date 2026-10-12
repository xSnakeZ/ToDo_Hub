package de.unistuttgart.iste.ese.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "todo")
public class ToDo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private long id;
    @NotEmpty
    private String title;
    private String description;
    private boolean finished;
    @ManyToMany
    @JoinTable(name = "todo_assignee", joinColumns = {@JoinColumn(name="fk_todo")}, inverseJoinColumns = {@JoinColumn(name ="fk_assignee")})
    private List<Assignee> assigneeList;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Date createdDate;
    @DateTimeFormat
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Date dueDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Date finishedDate;
    private String category;

    public ToDo() {
    }

    public ToDo(String title, String description, List<Assignee> assignees, Date dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.createdDate = new Date(System.currentTimeMillis());
        finished = false;
        if(assignees == null){
            this.assigneeList = new ArrayList<>();
        } else {
            this.assigneeList = assignees;
        }
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean setFinished() {
        return finished;
    }

    public List<Assignee> getAssigneeList() {
        return assigneeList;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getFinishedDate() {
        return finishedDate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setAssigneeList(List<Assignee> assigneeList) {
        this.assigneeList = assigneeList;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setFinishedDate(Date finishedDate) {
        this.finishedDate = finishedDate;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public boolean isFinished() {
        return finished;
    }
}
