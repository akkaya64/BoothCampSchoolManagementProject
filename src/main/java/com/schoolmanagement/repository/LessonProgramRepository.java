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
    // Haydi simdi tam da bir Service katmani olusturmanin zamani

    // Collection<Object> findByTeachers_IdNull();//Service katmanindan otomatik olarak olusturulan bir yardimci method
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
        //LessonProgram a alliance olarak l diyoruz
    // SELECT l FROM LessonProgram l; LessonProgram dan butun lesson program lari getir. Ama gelecek olan lesson
    // programin bir ozelligi olmali
    // inner join l.students students; LessonProgram in iliskili oldugu bir students tablosu var buradaki student
    // tablosundaki username bilgisine gore LessonProgram tablosundaki sutudent'e ait bilgileri getir.
    // WHERE students.username =?1 ; Asagidaki paremetrede verilen ilk parametre neyse onu getir
    Set<LessonProgram> getLessonProgramByStudentUsername(String username);


    @Query("SELECT l FROM LessonProgram l WHERE l.id IN :lessonIdList")
    // LessonProgram icin l alliance sini kullaniyoruz
    // LessonProgram Classindan l alliance si ile LessonProgramlari getir
    // WHERE l.id IN : lessonsIdList ; lessonProgramin id fieldindaki birazdan asagidaki methotta verecegim id
    // li bilgileri getir.
    Set<LessonProgram> getLessonProgramByLessonProgramIdList(Set<Long> lessonIdList);
}