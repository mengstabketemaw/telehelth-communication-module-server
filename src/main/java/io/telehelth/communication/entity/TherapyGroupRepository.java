package io.telehelth.communication.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapyGroupRepository extends JpaRepository<TherapyGroup,Long> {
    List<TherapyGroup> findAllByTherapist(String therapist);

}
