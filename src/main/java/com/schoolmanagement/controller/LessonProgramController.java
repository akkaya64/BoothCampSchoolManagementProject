package com.schoolmanagement.controller;
// Bu controller katmanini olusturduguna gore buyuk ihtimal Rest Full API yapacaksin. Kesinlikle bu cok iyi bir fikir
// ama once bunu Springframewoorke classin basina gerekli annotationu koyarak bildirmelisin

import com.schoolmanagement.payload.request.LessonProgramRequest;
import com.schoolmanagement.payload.response.LessonProgramResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.LessonProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    // Not :  getAll() *************************************************************************
    @GetMapping("/getAll")  // http://localhost:8080/lessonPrograms/getAll
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER','STUDENT')")
    public List<LessonProgramResponse> getAll() {
        return lessonProgramService.getAllLessonProgram();
    }

    // Not :  getById() ************************************************************************

    @GetMapping("/getById/{id}") //http://localhost:8080/lessonPrograms/getById/1
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    public LessonProgramResponse getById(@PathVariable Long id) {
        return lessonProgramService.getByLessonProgramId(id);
    }

    // Not :  getAllLessonProgramUnassigned() **************************************************
    @GetMapping("/getAllUnassigned") //http://localhost:8080/lessonPrograms/getAllUnassigned
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER','STUDENT')")
    public List<LessonProgramResponse> getAllUnassigned() {
        return lessonProgramService.getAllLessonProgramUnassigned();
    }
}
