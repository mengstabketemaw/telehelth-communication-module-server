package io.telehelth.communication.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class DoctorRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String username;

    @Lob
    String token;

    String type;

    String roomId;

}
