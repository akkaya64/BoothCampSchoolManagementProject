package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Teacher;
import com.schoolmanagement.payload.response.abstracts.TeacherResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phone);

    Teacher findByUsernameEquals(String username);

    boolean existsByEmail(String email);

    List<Teacher> getTeacherByNameContaining(String teacherName);
    //  Collection<Object> getTeacherByNameContaining(String teacherName); create edilen methodun orjinal hali ama bize
    //  List yapida Teacher objesi donecegi icin yukardaki degisikligi yapiyoruz
}
