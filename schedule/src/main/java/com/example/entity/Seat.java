package com.example.entity;



import lombok.NoArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

//import com.fasterxml.jackson.annotation.JsonIgnore;


@Table(name = "seats")
@Data
@NoArgsConstructor
public class Seat {

    @Id
    int seatId;

    String seatNumber;

    int busId;
    
    String status;

    int fare;

    @Version
    Long version;

    public boolean validate()
    {
        if(seatNumber==null || busId==0)
        {
            return false;
        }
        return true;
    }
}