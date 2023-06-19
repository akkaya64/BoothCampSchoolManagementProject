package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phone);

    Student findByUsernameEquals(String username);

    boolean existsByEmail(String email);

    @Query(value = "SELECT (count(s)>0 FROM Student s)") // Bu Query DB deki Student tablosuna gidecek tablodaki ogrenci
        // sayisinin 0 dan buyuk olup olmadigini yani tabloda ogrenci var mi yok mu kontrol edecek.. tablonun durumuna
        // gore true yada false dondurecek
    boolean findStudent();

    @Query(value = "SELECT MAX(s.studentNumber) FROM Student s ")// Student tablosundaki studen numarasi en buyuk olani
        // alacagiz. JPQL de yada HQL Sql de kullandigimiz methodlarin ve degiskenlerin hepsini kullanabiliyoruz.
        // DB deki FROM Student s tablosundan, MAX(s.studentNumber) student tablosunun studentNumber fieldindaki en
        // buyuk studentNumber i getir SELECT diyoruz
    int getMaxStudentNumber();// Bu method olusturuld Hibernate yada Spring Data Jpa bunun nasil calisacagini bilmez
    // bunun nasil calisacagini olusturacagimiz query ile belirleyecegiz
}