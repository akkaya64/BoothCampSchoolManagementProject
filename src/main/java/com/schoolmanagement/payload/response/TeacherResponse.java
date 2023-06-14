package com.schoolmanagement.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.payload.response.abstracts.BaseUserResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherResponse extends BaseUserResponse {
     private Set<LessonProgram> lessonPrograms;

     // Email bilgisini almamiz lazim ve email bilgisi unique bir deger almasi lazim. peki Admin service class icinde
     // bizim checkDuplicate adinda unique ligi kontrol eden bir method yazmistik Service katinda buradan alacagimiz
     // email bilgisininin uniqueligini kontrol etmek icin cagiramayiz cunku o method ssn number username ve phone u
     // kontrol ediyordu biz burada email bilgisinin unique olup olmadigini kontrol edecegiz. bunu yapmak icin o
     // methodu overload yapabiliriz yada yeni bir varark li  method yazabilriz. 3 tanede 4 tanede unique kontrolu
     // yapabilecek bir method olacak.

     private String email;

}
