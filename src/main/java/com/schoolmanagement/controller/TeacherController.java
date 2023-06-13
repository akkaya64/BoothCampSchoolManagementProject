package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.abstracts.TeacherResponse;
import com.schoolmanagement.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("teachers")
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;

    // Not: Save() **********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @PostMapping("/save")  // http://localhost:8080/teachers/save
    // TeacherRequest adinda bir classimiz olmali ki kayit edilecek bilgileri requestin bodys inden
    // @RequestBody interface i ile cekebilelim ve requestten gelen verileri @Valid ile validationdan gecirelim
    public ResponseMessage<TeacherResponse> save(@RequestBody @Valid TeacherRequest teacher) {

        return teacherService.save(teacher);
    }

    // Not: getAll() **********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getAll") // http://localhost:8080/teachers/getAll
    public List<TeacherResponse> getAllTeacher(){ // sadece TeacherResponse den DTO olarak Json formatinda Teacher lari
        // List yapida lacak. harhangi bir save yada update gibi bir islem yapmayacak veya bir parametreye bagli olarak
        // verileri getirmeyecegi icin buraya baska herhangi bir condition vermiyoruz tek yapacagi Teacherslari Json
        // olarak List yapida dondurmek.
        return teacherService.getAllTeacher(); // teacherService katina git oradaki.getAllTeacher() methodu ile
        // tum Teacherlari getir.
    }

    // Not: updateTeacherById() ************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @PutMapping("/update/{userId}")  // http://localhost:8080/teachers/update/1
    // Bu method update edilmis bir TeacherResponse 'i ResponseMessage olarak dondurecek.  endPoint den gelen id yi
    // @PathVariable Long userId ile alip bu id ye ait Teacher i TeacherRequest in body @RequestBody sinden
    // gelen teacher i @Valid et ve  TeacherService katindaki updateTeacher methodunu kullanarak update et.
    public ResponseMessage<TeacherResponse> updateTeacher(@RequestBody @Valid TeacherRequest teacher,
                                                          @PathVariable Long userId){

        return teacherService.updateTeacher(teacher, userId);
        // Yukaridan gelen requestten gelen yeni teacher bilgilerini yukarida verilen DB deki userid ye sahip teacher
        // a setleyip update islemini yapmasi icin teacherService katmanina git updateTeacher methodunu kullanarak
        // gerekli kontrolleri ve updatetion islemini yap ve dondur
    }



}
