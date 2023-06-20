package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Student;
import com.schoolmanagement.payload.response.StudentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phone);

    Student findByUsernameEquals(String username);

    boolean existsByEmail(String email);

    @Query(value = "SELECT (count(s)>0) FROM Student s") // Bu Query DB deki Student tablosuna gidecek tablodaki ogrenci
        // sayisinin 0 dan buyuk olup olmadigini yani tabloda ogrenci var mi yok mu kontrol edecek.. tablonun durumuna
        // gore true yada false dondurecek
    boolean findStudent();

    @Query(value = "SELECT MAX(s.studentNumber) FROM Student s ")// Student tablosundaki studen numarasi en buyuk olani
        // alacagiz. JPQL de yada HQL Sql de kullandigimiz methodlarin ve degiskenlerin hepsini kullanabiliyoruz.
        // DB deki FROM Student s tablosundan, MAX(s.studentNumber) student tablosunun studentNumber fieldindaki en
        // buyuk studentNumber i getir SELECT diyoruz
    int getMaxStudentNumber();// Bu method olusturuld Hibernate yada Spring Data Jpa bunun nasil calisacagini bilmez
    // bunun nasil calisacagini olusturacagimiz query ile belirleyecegiz

    List<Student> getStudentByNameContaining(String studentName);


    Optional<Student> findByUsername(String username);

    // Su anda JPQL yaziyoruz
    @Query(value = "SELECT s FROM Student s WHERE s.advisorTeacher.teacher.username =:username")
    // SELECT s FROM Student s --> Student tablosuna git bana studentleri getir.
    // WHERE --> nereden getirecegiz?
    // s (Student tablosu icin s alias ini kullanmistik) Student tablosuna git
    // .advisorTeacher --> Student tablosundan ile iliskili olan bulunan advisorTeacher git
    // .teacher --> AdvisorTeacher tablosundan iliski icinde oldugu teacher tablosunu git
    // .username --> teacher tablosunun username fieldini getir.
    // =:username --> bu asagida parametrede bulunan username yi alir deger olarak verir.
    // yani yukaridaki JPQL sunu yapar asagidaki parametreden username ini aldigimiz teacheri getirir onun
    // AdvisorTeacher rolune bagli olan studentleri student tablosundan getirir.
    // JPQL 'e alternatif olarak asagidaki gibi SQL condition yazarak da ayni islemi yapmis oluruz. ama asagidaki gibi
    // JOIN yapmak yerine daha sade yapidaki JPQL ile yazmak daha bestpractice
    // @Query(value= "SELECT s FROM Student s JOIN s.advisorTeacher at JOIN at.teacher t WHERE t.username=:username")
    List<Student> getStudentByAdvisorTeacher_Username(String username);

    @Query("SELECT s FROM Student s WHERE s.id IN :id")
    List<Student> findByIdsEquals(Long[] studentIds);

    @Query("SELECT s FROM Student s WHERE s.username=:username")
    Optional<Student> findByUsernameEqualsForOptional(String username);
}