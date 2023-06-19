package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.StudentRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.StudentResponse;
import com.schoolmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    // Not: Save() **********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @PostMapping("/save")
    public ResponseMessage<StudentResponse> save(@RequestBody @Valid StudentRequest studentRequest) {
        // endpoint den @RequestBody ile Json formata cevrilmis bir obje gelecek @Valid validation yap ve
        // StudentRequest data type indeki studentRequest e setle-maple-ata diyoruz
        return studentService.save(studentRequest);
    }

    // Not: changeActiveStatus() *********************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/changeStatus")// DB deki datanin tamamini degistirmeyecegimiz icin PutMapping yapmadik. PutMapping
    // yaptigimiz zaman butun field lari setlememiz gerekecekti setlemedigimiz ger fieeld da null olarak gelecekti.
    public ResponseMessage<?> changeStatus(@RequestParam Long id, @RequestParam boolean status){// request in
        // parametrelerinden id bilgisini alarak student objesine ulasilabir, ikinci olarak yine requestin
        // parametrelerinden ogrenci aktif mi degil mi booleanin yeni degereini almaliyiz ki ogrencinin status durumunu
        // guncelleyebilelim... birden fazla data alacaksak path variable den almak pek uygun degil
        return studentService.changeStatus(id,status);// studentService in changeStatus() metgodunda yapilacak olan
        // logical islemler icin changeStatus() methoduna id ve yeni status bilgilerini gonderiyoruz.
    }

    // Not: getAllStudent() *******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getAll")
    public List<StudentResponse> getAllStudent(){
        return studentService.getAllStudent();
    }

    // Not: updateStudent() ******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @PutMapping("/update/{userId}")//update edilecek ogrencinin id bilgis gelmesi lazim. tek bir data oldugu icin kolay
    // bir sekilde @PathVariable ile bunu alabiliriz.
    //Kullaniciya bir message dondurecegiz parametre olarak da artik degistirilmis bir StudentResponse objesi veriyoruz.
    public ResponseMessage<StudentResponse> updateStudent(@PathVariable Long userId, // endpoint path inden gelen userId
                                                          // adini verdigimiz id yi buradaki parametre olarak verdigimiz
                                                          // Long turundeki userId ye mapliyoruz-setliyoruz-atiyoruz.
                                                          @RequestBody @Valid StudentRequest studentRequest){ //
        // Kullanicidan bir obje gelecek  @RequestBody ile bunu al @Valid ile validation isleminden gecirip
        // StudentRequest turunde bir studentRequest bir container e mapliyoruz-setliyoruz-atiyoruz.

        return studentService.updateStudent(userId, studentRequest); // Methodun parametresinden gelen id bilgisini ve
        // yeni olusan StudentRequest in kendisini service katmanindaki updateStudent methoduna gonderiyoruz.

    }


}
