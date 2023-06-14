package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phone);

    Teacher findByUsernameEquals(String username);

    boolean existsByEmail(String email);

    //@Query("select t from Teacher t where t.name like concat('%', ?1, '%')")
    List<Teacher> getTeacherByNameContaining(String teacherName);
    //  Collection<Object> getTeacherByNameContaining(String teacherName); create edilen methodun orjinal hali ama bize
    //  List yapida Teacher objesi donecegi icin yukardaki degisikligi yapiyoruz
}
