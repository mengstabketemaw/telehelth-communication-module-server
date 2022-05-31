package io.telehelth.communication.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class TherapyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String therapist; //username of the doctor

    String description;

    LocalDateTime startingDate;

    int maxPatientNumber;

    int currentPatientNumber=0; // initializing it to 0

    String[] patients = new String[]{};
}
