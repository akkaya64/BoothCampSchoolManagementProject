package com.schoolmanagement.payload.dto;

import com.schoolmanagement.entity.concretes.Lesson;
import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.payload.request.LessonProgramRequest;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
//@Component
public class LessonProgramDto {
    // Bu class i service katymaninda bu sekliyle classi injection yapip cagirdigimizda exception aliriz bunun cozumu
    // iki yol ile yapilir bunyardan ilki bu classi @Component ile annote ederiz ama bu sekilde takibi yapmak takibi
    // zorlastiriyor. Bu sekilde @Companent olusturulacak her classin icin ikinci yol ise olusturdugumuz copnfic package
    // sininin icinde creation islemlerini yapmak

    // DTO --> POJO DONUSUMU
    public LessonProgram dtoLessonProgram(LessonProgramRequest lessonProgramRequest, Set<Lesson> lessons){
        return LessonProgram.builder()
                .startTime(lessonProgramRequest.getStartTime())
                .stopTime(lessonProgramRequest.getStopTime())
                .day(lessonProgramRequest.getDay())
                .lesson(lessons)
                .build();
    }
}
