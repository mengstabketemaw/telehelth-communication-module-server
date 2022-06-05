package io.telehelth.communication.service;

import io.telehelth.communication.model.Role;
import io.telehelth.communication.model.VdtUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OnlineUsersService {
    private static final Logger logger = LoggerFactory.getLogger(OnlineUsersService.class);
    private static final Map<String,VdtUsers> onlineUsers = new HashMap<>();
    private static final Map<String,String> current = new HashMap<>();
    private static final List<String> patients = new ArrayList<>();
    private static final List<String> doctors = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;

    public OnlineUsersService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public Map<String,Integer> getStatus(){
        Map<String,Integer> status = new HashMap<>();
        status.put("current",current.size());
        status.put("patients",patients.size());
        status.put("doctors",doctors.size());
        status.put("users",onlineUsers.size());
        return status;
    }

    public void sendPersonalMessage(String username,VdtUsers message){
        logger.info("m sending personal message <{}> to <{}>",message,username);
        messagingTemplate.convertAndSendToUser(username,"/msg",message);
    }
    public void sendStatus(){
        Map<String, Integer> status = getStatus();
        logger.info("sending status <{}> to topic.status",status);
        messagingTemplate.convertAndSend("/topic/status",status);
    }

    public void patientEntered(VdtUsers patient){
        //assign to a doctor if there is one waiting for patient
        if(doctors.size()>0){
            String username = doctors.remove(0); //get the first doctor
            VdtUsers doctor = onlineUsers.get(username);
            current.put(doctor.getUsername(), patient.getUsername());
            sendPersonalMessage(doctor.getUsername(), patient);
            sendPersonalMessage(patient.getUsername(), doctor);
            return;
        }
        //else just add the patient to the queue of waiting
        patients.add(patient.getUsername());
    }

    public void doctorEntered(VdtUsers doctor){
        //if there is patient on queue of waiting assign it.
        if(patients.size()>0){
            String username = patients.remove(0);//get the first patient based on queue.
            VdtUsers patient = onlineUsers.get(username);
            current.put(doctor.getUsername(), patient.getUsername());
            sendPersonalMessage(doctor.getUsername(), patient);
            sendPersonalMessage(patient.getUsername(), doctor);
            return;
        }
        //else add the doctor to the queue of waiting
        doctors.add(doctor.getUsername());
    }

    public void patientLeft(VdtUsers patient){
        //if the patient was with a doctor, find that doctor and assign it to a new patient
        if(current.containsValue(patient.getUsername())){

            VdtUsers doctor = onlineUsers.get(
                    current.entrySet().stream()
                    .filter(e->e.getValue().equals(patient.getUsername()))
                    .findAny()
                    .get().getKey()
            );
            //remove the doctor
            current.remove(doctor.getUsername());
            //if there is patient in queue waiting for doctor
            if(patients.size()>0){
                String username = patients.remove(0);
                VdtUsers pat = onlineUsers.get(username);
                current.put(doctor.getUsername(), pat.getUsername());
                sendPersonalMessage(doctor.getUsername(), pat);
                sendPersonalMessage(pat.getUsername(), doctor);
            }else //if there is no patient add the doctor to the queue
                doctors.add(doctor.getUsername());
        }else
            patients.remove(patient.getUsername());
    }

    public void doctorLeft(VdtUsers doctor){
        //if doctor left the room just remove doctor from the list
        if(current.containsKey(doctor.getUsername())){
             String username = current.remove(doctor.getUsername());
             VdtUsers patient = onlineUsers.get(username);
             messagingTemplate.convertAndSendToUser(patient.getUsername(),"/msg","Your doctor has left leave the room");
             onlineUsers.remove(patient.getUsername());
        }else
            doctors.removeIf(d->d.equals(doctor.getUsername()));
    }



    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor stompAccessor = StompHeaderAccessor.wrap(event.getMessage());
        @SuppressWarnings("rawtypes")
        GenericMessage connectHeader = (GenericMessage) stompAccessor
                .getHeader(SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER); // FIXME find a way to pass the username
        // to the server
        @SuppressWarnings("unchecked")
        Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) connectHeader.getHeaders()
                .get(SimpMessageHeaderAccessor.NATIVE_HEADERS);

        String username = nativeHeaders.get("username").get(0);
        String type = nativeHeaders.get("type").get(0);
        String sessionId = stompAccessor.getSessionId();
        VdtUsers vdtUser = new VdtUsers(username,type,sessionId);
        onlineUsers.put(username,vdtUser);

        //creating a new thread to handle the user.
        Thread thread = new Thread(()->{
            try {
                Thread.sleep(1000); //sleep until the user finished registering and subscribing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(vdtUser.getRole().equals(Role.PATIENT))
                patientEntered(vdtUser);
            else
                doctorEntered(vdtUser);
            sendStatus();
        });

        thread.start();

        logger.info("Chat connection by user <{}> type <{}> with sessionId <{}>", username,type, sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor stompAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = stompAccessor.getSessionId();
        String username = onlineUsers.entrySet()
                                        .stream()
                                        .filter(e->e.getValue().getSessionId().equals(sessionId))
                                        .findAny()
                                        .get()
                                        .getValue()
                                        .getUsername();
        VdtUsers user = onlineUsers.remove(username);

        if(user.getRole().equals(Role.DOCTOR))
            doctorLeft(user);
        else
            patientLeft(user);
        sendStatus();
        logger.info("User has left the room <{}> with sessionId <{}>", user.getUsername(), sessionId);

    }



}
