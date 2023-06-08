package com.schoolmanagement.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.schoolmanagement.entity.concretes.EducationTerm;
import com.schoolmanagement.entity.concretes.Lesson;
import com.schoolmanagement.entity.enums.Day;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)// null olan degerler gorunmesin diyorsak
public class LessonProgramResponse {

    private Long lessonProgramId;
    private Day day;
    private LocalTime startTime;
    private LocalTime stopTime;
    private Set<Lesson> lessonName;// lessonName asinda icinde Set yapida <Lesson> lari tutan bir field Set yapilar
                                   // tek bir objeyi tutar mesela iki tane Math varsa birisini alir.
    private EducationTerm educationTerm;
    // TODO student ve teacher yazilinca eklemeler yapilacak
}
