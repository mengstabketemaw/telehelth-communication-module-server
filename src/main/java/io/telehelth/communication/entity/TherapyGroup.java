package io.telehelth.communication.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class TherapyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String therapist; //username of the doctor

    String description;

    LocalDateTime startingDate;

    double duration;

    int maxPatientNumber;

    String[] patients = new String[]{};
}
