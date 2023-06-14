package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.ChooseLessonTeacherRequest;
import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.TeacherResponse;
import com.schoolmanagement.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    // Not: getTeacherByName() **************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getTeacherByName")
    //teacherName 'i @PathVariable, @RequestParam, @RequestBody, @RequestAttribute yada Json dosyadan(Request Objesi) da
    // alabiliriz Burada  @RequestParam ile getirelim name keywordunu kullanarak endPoint den (name = "name")"name"
    // adinda bir yapi gelecek bunu String teacherName den gelecek veri ile set le diyoruz. Bu tarz birden fazla deger
    // donmeyecekse simple olarak @PathVariable ilede alabiliriz
    public List<TeacherResponse> getTeacherByName(@RequestParam(name = "name") String teacherName){
        return teacherService.getTeacherByName(teacherName);

    }

    // Not: deleteTeacher() *****************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @DeleteMapping("/delete/{id}")
    // gelen deger generic bir yapida gelecek ama birsey dondurulmeyecek bu nedenle diamond icine birsey yazmiyoruz
    // <?> yaziyoruz.
    public ResponseMessage<?> deleteTeacher(@PathVariable Long id) {
        return  teacherService.deleteTeacher(id);
    }

    // Not: getTeacherById() ****************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getSavedTeacherById/{id}")
    public ResponseMessage<TeacherResponse> getSavedTeacherById(@PathVariable Long id){
        return teacherService.getSavedTeacherById(id);
    }



    // Not: getAllWithPage() ****************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/search")
    public Page<TeacherResponse> search(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ){
        return teacherService.search(page, size,sort,type);
    }


    // Not: addLessonProgramToTeachersLessonsProgram() **********************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @PostMapping("/chooseLesson")
    // Teachere a LessonProgram atayabilmemiz icin requestin Body sinde requestten gelen LessonProgram 'a ve
    // LessonProgramin setlenecegi bir Teacher 'a ait id lerin bulunmasi gerekiyor. Bunlari requestin Bodysinden
    // alabilmeiz icin bu datalari iclerinde barindiran Request class create ediyoruz. Bu Class 'i nerede olusturuyoruz?
    // Tabiki!! payload packagesinin icinde bulunan request packagesinin icinde olusturuyoruz
    public ResponseMessage<TeacherResponse> chooseLesson(@RequestBody @Valid ChooseLessonTeacherRequest chooseLessonRequest){
        return teacherService.chooseLesson(chooseLessonRequest);
    }






}
