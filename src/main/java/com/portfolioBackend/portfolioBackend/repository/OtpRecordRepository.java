package com.portfolioBackend.portfolioBackend.repository;

import com.portfolioBackend.portfolioBackend.model.OtpRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpRecordRepository extends MongoRepository<OtpRecord, String> {

    Optional<OtpRecord> findByEmailAndOtpAndUsedFalse(String email, String otp);
}
