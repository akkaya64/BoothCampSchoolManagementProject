package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.StudentInfo;
import net.bytebuddy.jar.asm.commons.Remapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Collection;
import java.util.List;


public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
    List<StudentInfo> getAllByStudentId_Id(Long studentId);


    boolean existsByIdEquals(Long studentInfoId);


    StudentInfo findByIdEquals(Long studentInfoId);

    @Query("SELECT s FROM StudentInfo s WHERE s.teacher.username= ?1")// ?1 --> asagidaki parametrede verilen ilk
            // s alias StudentInfo
            // SELECT s --> StudentInfo lari getir
            // FROM StudentInfo --> StudentInfo tablosundan
            // WHERE s.teacher.username --> StudentInfo tablosunun teacher objesinin username field indaki
            // ?1 --> asagidaki methodun parametresinde olan 1 inci data olan username ile eslesen

    // Yani httpServletRequest den gelen username li teacher in studentInfo tablosunu getir.


    Page<StudentInfo> findByTeacherId_UsernameEquals(String username, Pageable pageable);
    //Page yapida bir StudentInfo entity objesi gelecek

    @Query("SELECT s FROM StudentInfo s WHERE s.student.username = username")
    Page<StudentInfo> findByStudentId_UsernameEquals(String username, Pageable pageable);

    @Query("SELECT (count(s) > 0) FROM StudentInfo s WHERE s.student.id = studentId")
    boolean existsByStudent_IdEquals(Long studentId);


    @Query("SELECT s FROM StudentInfo s WHERE s.student.id = ?1")
    List<StudentInfo> findByStudent_IdEquals(Long studentId);
}
