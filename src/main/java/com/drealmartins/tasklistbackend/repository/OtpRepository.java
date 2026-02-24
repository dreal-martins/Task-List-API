package com.drealmartins.tasklistbackend.repository;

import com.drealmartins.tasklistbackend.entity.OtpCode;
import com.drealmartins.tasklistbackend.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByEmailAndCodeAndTypeAndVerifiedFalse(
            String email,
            String code,
            OtpType type
    );

    Optional<OtpCode> findTopByEmailAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            String email,
            OtpType type
    );

    void deleteByExpiryDateBefore(LocalDateTime currentTime);

    void deleteByEmail(String email);

    void deleteByEmailAndType(String email, OtpType type);
}