package com.schoolmanagement.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// Bu class Student in LessonProgram secebilmesi icin sectigi lessonlarin id sini RequestBody den alabilmesi icin
// olusturuldu
public class ChooseLessonProgramWithId implements Serializable {
    // Student birden fazla LessonProgram i secebilir dolayisi ile List yada Set yapida bir yapi kurmamiz gerekecek
    // ancak ogrencinin ayni lessonProgrami birden fazla secme ihtimaline karsi icine benzer ogeleri kabul etmeyen,
    // icinde sadece unique degerler barindiran Set yapisi ile calismamiz gerekir

    // Id alacagimiz icin Set yapinin icerisine id lerin gelecegini belirtmek icin id nin data type (Long) ni veriyoruz
    // Bu id yi @PathVariable dan da kolayca alabilirdik ama tecrube olmasi icin requestBody den almayi zorluyoruz.

    //Bu bir DTO bu data DB ye gidecek bu nedenle DB ye kayit etmeden once validasyonlardan gecmesi gerekiyor.
    @NotNull(message = "Please Select Lesson Program")
    @Size(min = 1, message = "Lessons must not be empty")
    private Set<Long> lessonProgramId;

}
