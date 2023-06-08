package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.LessonProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface LessonProgramRepository extends JpaRepository<LessonProgram, Long> {
    // Merhaba Projeye yeni bir Ozellik eklmede yine birlikteyiz :) bu cok
    // heyecen verici. Bu interface yi olusturdugun icin eminim ki mutlu hissediyorsundur. Hadi bakalim ise once
    // LessonProgramRepository interfacesini i JpaRepository den extends etmektenn baslayalim, biliyorsun
    // JpaRepository ler generic yapilardir ve diamond <> icine entitty classi ve PK yaptigimiz unique bir deger
    // olan id nin data type ni yaziyoruz. aynen su sekilde: <LessonProgram, Long> . Artik LessonProgramRepository
    // Springframework un JpaRepository interfacesini kullanarak LessonProgram, Long ozelliginde bir repository
    // katmani olusturduk...
    // Simdi tam da bir Service katmani olusturmanin zamani

    //Collection<Object> findByTeachers_IdNull();//Service katmanindan otomatik olarak olusturulan bir yardimci method
    // findByTeachers_IdNull() bize Collection<Object>
    // Collection yapida ki oda service katinda List yapi da olusturuldu
    // <Object> generic olarak da LessonProgram donecek
    // yani burada otomatik olak olusturulan code blogunu asagidaki gibi duzenliyoruz
    List<LessonProgram> findByTeachers_IdNull();
    // findByTeachers_IdNull(); findBy: turetilebilen bir calssin keywordudur,
                               // Teachers da bir keyword dur teachers lari bulmasini istiyoruz
                               // _IdNull hangilerini? id si null olanlari
    //LessonProgram tablosuna git teacher header lari null olanlari getir diyoruz

    List<LessonProgram> findByTeachers_IdNotNull();
    //LessonProgram tablosuna git teacher header lari null olmayanlari getir diyoruz

    @Query("SELECT l FROM LessonProgram l inner join l.teachers teachers where teachers.username = ?1")
    Set<LessonProgram> getLessonProgramByTeacherUsername(String username);

    @Query("SELECT l FROM LessonProgram l inner join l.students students WHERE students.username =?1")
    Set<LessonProgram> getLessonProgramByStudentUsername(String username);
}