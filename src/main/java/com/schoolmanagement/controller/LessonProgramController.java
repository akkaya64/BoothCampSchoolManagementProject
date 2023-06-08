package com.schoolmanagement.controller;
// Bu controller katmanini olusturduguna gore buyuk ihtimal Rest Full API yapacaksin. Kesinlikle bu cok iyi bir fikir
// ama once bunu Springframewoorke classin basina gerekli annotationu koyarak bildirmelisin

import com.schoolmanagement.payload.request.LessonProgramRequest;
import com.schoolmanagement.payload.response.LessonProgramResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.LessonProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController// tam da isleri kolaylastirmak icin ihtiyacim olan takim cantasi
@RequestMapping("/lessonPrograms")// kesinlikle requsetleri burada karilayacaksin dostum harikasin... Gelenleri
// kacirma hemen iceri al
@RequiredArgsConstructor// olmazsa olmaz ne yapiyordu lan bu bir google yapayim. kesin bir ise yariyordur ama neye!!!
public class LessonProgramController {

    private final LessonProgramService lessonProgramService; //ilk etapta Service katmani ile iliski kurulacak

    // Not :  Save() *************************************************************************
    @PostMapping("/save")  // http://localhost:8080/lessonPrograms/save
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    // Hey daha fazla ilerlemeden ResponseMessage generic olarak verecegin classi once bir olustur lutfen yoksa
    // kullniciya hangi objeyi dondureceksin... Tabiki Kullanicidan alacagin bilgiler icinde bir request calas
    // olusturman  gerektigini biliyorsundur...
    public ResponseMessage<LessonProgramResponse> save(@RequestBody @Valid LessonProgramRequest lessonProgramRequest) {
        return lessonProgramService.save(lessonProgramRequest);
    }
}
