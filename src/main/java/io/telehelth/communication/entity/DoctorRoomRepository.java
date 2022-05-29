package io.telehelth.communication.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRoomRepository extends JpaRepository<DoctorRoom,Long> {
    Optional<DoctorRoom> findByUsername(String username);
    void deleteByUsername(String username);
}
