package de.unistuttgart.iste.ese.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;


@Entity
@Table(name="assignee")
public class Assignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignee_id")
    private long id;
    @NotEmpty
    private String prename;
    @NotEmpty
    private String name;
    @Email
    @Pattern(regexp = ".*uni-stuttgart\\.de$")
    private String email;
    
    public Assignee(){}
    public Assignee(String prename, String name, String email){
        this.prename = prename;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getPrename() {
        return prename;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPrename(String prename) {
        this.prename = prename;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
