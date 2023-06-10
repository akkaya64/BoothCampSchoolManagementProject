package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.ContactMessage;
import com.schoolmanagement.payload.response.ContactMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.stream.DoubleStream;
//@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    // Classi @Repository ile annotation etmemize gerek yok. SpringBoot bu class JpaRepository den extend edildiyse
    // bu bir repository classi oldugunu biliyor parent child iliskisi

    boolean existsByEmailEqualsAndDateEquals(String email, LocalDate now);


    Page<ContactMessage> findByEmailEquals(String email, Pageable pageable);

    Page<ContactMessage> findBySubjectEquals(String subject, Pageable pageable);
}
