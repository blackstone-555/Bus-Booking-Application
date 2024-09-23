package com.example.entity;

import org.springframework.data.relational.core.mapping.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Table(name = "buses")
@Data
@NoArgsConstructor
public class Bus {

    @Id
    int busId;

    String busNumber;
    
    int scheduleId;

    public boolean validate()
    {
        if(busNumber==null || scheduleId==0)
        {
            return false;
        }
        return true;
    }
}