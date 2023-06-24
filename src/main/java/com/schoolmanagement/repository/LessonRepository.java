package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
                                                        // LessonRepository id ile getirilecek
                                                        // olan Lesson Entity
                                                        // trunde bir Inteface olacak

    boolean existsLessonByLessonNameEqualsIgnoreCase(String lessonName);

    Optional<Lesson> getLessonByLessonName(String lessonName);


    @Query(value = "SELECT l FROM Lesson l WHERE l.lessonId IN :lessons")
        // Bu Query in acilimi;
        // SELECT l lessonlari getir. nereden?
        // FROM Lesson, lesson tablomdan. hangilerini?
        // WHERE l.LessonId; assagidaki lesson nin icindeki id ler ile eslesen
        // IN : lesson; List halindeki id lerden (Set<Long> lessons)

    Set<Lesson> getLessonByLessonIdList(Set<Long> lessons);// Burada lessons in icinde id ler var

    boolean existsByLessonIdEquals(Long lessonId);
   
    Lesson findByLessonIdEquals(Long lessonId);



}
