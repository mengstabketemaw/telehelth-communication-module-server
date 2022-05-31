package io.telehelth.communication.controller;

import io.telehelth.communication.entity.DoctorRoom;
import io.telehelth.communication.entity.DoctorRoomRepository;
import io.telehelth.communication.entity.TherapyGroup;
import io.telehelth.communication.entity.TherapyGroupRepository;
import io.telehelth.communication.service.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video-api")
@CrossOrigin
public class Controller {
    private final Logger logger = Logger.getLogger(Controller.class.getName());
    private final Service service;
    private final WebClient videoApi;
    private final DoctorRoomRepository doctorRoomRepository;
    private final TherapyGroupRepository therapyGroupRepository;

    public Controller(Service service, WebClient videoApi, DoctorRoomRepository doctorRoomRepository, TherapyGroupRepository therapyGroupRepository) {
        this.service = service;
        this.videoApi = videoApi;
        this.doctorRoomRepository = doctorRoomRepository;
        this.therapyGroupRepository = therapyGroupRepository;
    }

    @GetMapping("/check-server")
    public String test(){
       return "yea it is Working";
    }


    @PostMapping("/create-room")
    public ResponseEntity<DoctorRoom> createMeeting(@RequestBody Map<String,String> body){
        String username = body.get("username");
        doctorRoomRepository.deleteByUsername(username);//make sure there only one room per user
        String type = body.get("type"); //consultation,vdt,therapyGroup
        logger.info("creating a a room for doctor username: "+username+", with purpose of: "+type);
        String token = service.getToken();
        Map<String,Object> response = videoApi.post()
                .uri("/meetings")
                   .header("Authorization",token)
                   .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                   .retrieve()
                   .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() { })
                   .block();
        DoctorRoom doctorRoom = new DoctorRoom();
        doctorRoom.setUsername(username);
        doctorRoom.setRoomId(response.get("meetingId").toString());
        doctorRoom.setToken(token);
        doctorRoomRepository.save(doctorRoom);
        logger.info("room has been create with id : "+response.get("meetingId"));
        return ResponseEntity.ok(doctorRoom);
    }

    @GetMapping("/validate-room/{roomId}")
    public ResponseEntity<Void> validate(@PathVariable String roomId){
        String token = service.getToken();
       int status =  videoApi.post()
                .uri("/meetings/"+roomId)
                .header("Authorization",token)
                .retrieve()
                .toBodilessEntity()
                .block()
               .getStatusCodeValue();
       return ResponseEntity.status(status).build();
    }

    @GetMapping("/get-room/{username}")
    public ResponseEntity<DoctorRoom> getRoom(@PathVariable String username){
        return ResponseEntity.ok(doctorRoomRepository.findByUsername(username).get());
    }

    @GetMapping("/delete-room/{username}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String username){
        doctorRoomRepository.deleteByUsername(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-therapy-group")
    public ResponseEntity<Void> createTherapyGroup(@RequestBody @Valid TherapyGroup therapyGroup){
        therapyGroupRepository.save(therapyGroup);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/therapy-groups")
    public ResponseEntity<List<TherapyGroup>> getAllTherapyGroup(){
        return ResponseEntity.ok(therapyGroupRepository.findAll());
    }

    @GetMapping("/therapy-groups/doctor/{therapist}")
    public ResponseEntity<List<TherapyGroup>> getMyTherapyGroup(@PathVariable String therapist){
        return ResponseEntity.ok(therapyGroupRepository.findAllByTherapist(therapist));
    }

    @GetMapping("/join-therapy-group/{id}/{username}")
    public ResponseEntity<Void> joinTherapyGroup(@PathVariable long id,@PathVariable String username){
        TherapyGroup therapyGroup = therapyGroupRepository.findById(id).get();
        String[] currPatients = therapyGroup.getPatients();

        List<String> patientList = Arrays.stream(currPatients)
                .collect(Collectors.toList())
                ;
        patientList.add(username);
        therapyGroup.setPatients(patientList.toArray(new String[0]));
        therapyGroup.setCurrentPatientNumber(therapyGroup.getCurrentPatientNumber()+1);
        therapyGroupRepository.save(therapyGroup);
        return ResponseEntity.ok().build();
    }



}
