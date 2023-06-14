package com.schoolmanagement.utils;

import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

public class CheckSameLessonProgram {

    // Bu yapi bir Requestten gelen LessonProgrami iki DB den gelen LessonProgrami alarak calisacak iki datayi
    // kiyaslayacak burada sadece Id ye bakmasi yeterli degil Fieldlari da eslesiyormu diye kontrol etmeli bu nedenden
    // dolayi checkDuplacate methodunu kullanamiyoruz o bu seneryoya uygun degil onda sadece id ler eslesiyormu diye
    // bakiyorduk ve bunu yeterli oldugu cclass lardan cagiriyorduk. ayni fieldlara sahip bir LessonProgram Create
    // edildiginde buna farkli bir id vererek DB ye kayit eder bu nedenle LessonProgramlarin cakismasini onlemek icin
    // Fieldlarini da kontrol etmeliyiz. Biraz ince iscilik yapcaz gari burada.

    //  SENERYO 1) Teacher 'e ilk defa lessonProgram atamasi yapiliyor Bu techer e iki farkli Lesson atanabilir.
    //  Burada bu derslerin date ve time lari cakisiyormu kontrol edilmesi lazim. Cunku bir Teacher ayni anda iki
    //  farkli derse giremez

    // SENERYO 2) Teacher in mevcut dersleri var update edilecek yeni lessonlar eklenecek yeni lessonlarin date and
    // time lari mevcut Lessonlarin Date ve Time lari ile cakisiyormu kontrolunun yapilmasin lazim
    public static void  checkLessonPrograms(Set<LessonProgram> existLessonProgram, Set<LessonProgram> lessonProgramRequest){
        //

        // Once ikisinin de ici bosmu diye kontrol etmeliyiz.
        if(existLessonProgram.isEmpty() && lessonProgramRequest.size()>1) {
            checkDuplicateLessonPrograms(lessonProgramRequest);
        } else {
            checkDuplicateLessonPrograms(lessonProgramRequest);
            checkDuplicateLessonPrograms(existLessonProgram,lessonProgramRequest);
        }

    }

    private static void checkDuplicateLessonPrograms(Set<LessonProgram> lessonPrograms) {

        Set<String> uniqueLessonProgramKeys = new HashSet<>();

        for (LessonProgram lessonProgram : lessonPrograms ) {
            String lessonProgramKey = lessonProgram.getDay().name() + lessonProgram.getStartTime();
            if(uniqueLessonProgramKeys.contains(lessonProgramKey)){
                throw  new BadRequestException(Messages.LESSON_PROGRAM_EXIST_MESSAGE);
            }
            uniqueLessonProgramKeys.add(lessonProgramKey);
        }
    }

    public static void checkDuplicateLessonPrograms(Set<LessonProgram> existLessonProgram, Set<LessonProgram> lessonProgramRequest ){

        for (LessonProgram requestLessonProgram : lessonProgramRequest) {

            if(existLessonProgram.stream().anyMatch(lessonProgram ->
                    lessonProgram.getStartTime().equals(requestLessonProgram.getStartTime()) &&
                            lessonProgram.getDay().name().equals(requestLessonProgram.getDay().name()))) {
                throw  new BadRequestException(Messages.LESSON_PROGRAM_EXIST_MESSAGE);
            }

        }

    }

    // TODO : startTime baska bir lessonProgramin startTime ve endTime arasindami kontrolu eklebnecek
}