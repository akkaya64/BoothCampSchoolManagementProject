package com.schoolmanagement.controller;

import com.schoolmanagement.payload.response.AdvisorTeacherResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.AdvisorTeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advisorTeacher")
@RequiredArgsConstructor
public class AdvisorTeacherController {
    private final AdvisorTeacherService advisorTeacherService;

    // Bu class da save methodu yok cunku bu class var olan Teacher Objesinin bir field i olarak olusacak.
    // Yani Teacher e role atayarak save islemini orada yapmis oluyoruz

    // AdvisorTeacher  class ina advisorTeacher endpoint i ile sadece 3 tane request gelecek. diger requestler
    // Teacher gibi Student gibi classlarin Controller katmanlarindan gelcegi icin Business Logic leri burada degil
    // TODO yukaridaki cumleyi gerekirse guncelleyecegim
    // AdvisorTeacherService katmaninda yapilacak

    // Not: deleteAdvisorTeacher() ******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @DeleteMapping("/delete/{id}")
                                                                //Buradaki id AdvisorTeacher id si degil. Burada yeni
    // bir Id create ediliyor new leniyor ama Service katinda bu id yi AdvisorTeacher id si olarak al diyecegiz
    // AdvisorTeacher in id sinin Long data type inda oldugunu biliyoruz, o zaman burada id nin data type ini yazmamiz
    // gerekeyiordu. Buradayeni bir id olusacagi icin olusacak olan id nin data type ni belirtiyoruz. Yani burada id
    // adinda bir variable olusturuyoruz end pointten gelen id yide buraya atiyoruz. Service katindada bu id nin
    // AdvisorTeacher in id si olacagini setliyoruz.
    public ResponseMessage<?> deleteAdvisorTeacher(@PathVariable Long id){
        // <?> yazmadanda birakabiliriz cunku donecek birsey yok ama intelji nin urari vememesi icin icinde ? olan bir
        // diamond koyuyoruz

        return advisorTeacherService.deleteAdvisorTeacher(id);
    }

    // Not: getAllAdvisorTeacher() ******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getAll")
    public List<AdvisorTeacherResponse> getAllAdvisorTeacher(){//DTO yapida bir AdvisorTeacherResponse List i dondurecek

        return advisorTeacherService.getAllAdvisorTeacher();
    }

    // Not: getAllAdvisorTeacherWithPage() **********************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/search")
    public Page<AdvisorTeacherResponse> search(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ){
        return advisorTeacherService.search(page,size,sort,type);
    }



}
