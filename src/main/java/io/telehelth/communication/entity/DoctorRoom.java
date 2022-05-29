package io.telehelth.communication.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class DoctorRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String username;

    String token;

    String type;

    String roomId;

}
