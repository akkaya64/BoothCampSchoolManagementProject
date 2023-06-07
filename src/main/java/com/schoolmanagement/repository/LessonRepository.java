package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
                                                        // LessonRepository id ile getirilecek
                                                        // olan Lesson Entity
                                                        // trunde bir Inteface olacak

    boolean existsLessonByLessonNameEqualsIgnoreCase(String lessonName);
}
