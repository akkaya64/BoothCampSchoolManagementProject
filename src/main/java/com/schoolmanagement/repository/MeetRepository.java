package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Meet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetRepository extends JpaRepository<Meet,Long> {

    List<Meet> findByStudentList_IdEquals(Long studentId);
}
