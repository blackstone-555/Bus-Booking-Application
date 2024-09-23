package com.BusBooking.User.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.http.ResponseEntity;

@Table("users") // This maps the class to the "users" table in the database
@Data
@NoArgsConstructor
public class User {

    @Id
    private Integer id; 

    private String name;

    private String emailid;

    private String phoneno;

    private String passhash;

    private String role;

    public boolean validate()
    {
        if (this.getEmailid() == null || this.getName() == null || this.getPasshash() == null || this.getRole()==null) {
            return false;
        }
        return true;
    }
}
