package io.telehelth.communication.model;

import lombok.Data;

@Data
public class VdtUsers {
    String username;
    String sessionId;
    Role role;

    public VdtUsers(String username){
        this.username = username;
    }

    public VdtUsers(String username, String type, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
        if(type.equalsIgnoreCase("patient"))
            this.role = Role.PATIENT;
        else
            this.role = Role.DOCTOR;
    }

}
