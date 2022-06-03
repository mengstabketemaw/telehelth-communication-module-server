package io.telehelth.communication.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface DoctorRoomRepository extends JpaRepository<DoctorRoom,Long> {
    Optional<DoctorRoom> findByUsername(String username);
    @Modifying
    void deleteByUsername(String username);
}
