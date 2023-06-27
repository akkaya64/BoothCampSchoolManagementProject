package com.schoolmanagement.controller;

import com.schoolmanagement.entity.concretes.Lesson;
import com.schoolmanagement.payload.request.LessonRequest;
import com.schoolmanagement.payload.response.LessonsResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController// Restfull APi yapmak icin kullanilir
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    // Not :  Save() *************************************************************************

//    {
//        "lessonName": "Chemical",
//            "creditScore": 5,
//            "isCompulsory": true
//    }
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @PostMapping("/save") // http://localhost:8080/lessons/save end poind save ile biterse asagidaki method tam size
    // gore buyrun girin cekinmeyin evinizde gibi hissedin lutfen ne arzu ederdiniz bir seve methodumu o zaman save
    // logical islemleri icin Service katina burun...
    // ama once return yapabilmeniz icin size bir LessonsResponse objesi verelim. hemen response packagesine gidip
    // sizin icin bir LessonsResponse objesi olusturayim daha sonrada save yapabileceginiz kullanicidan gelen
    // validasyunu yapilmis tertemiz bir LessonRequest ama sizden bir ricamiz var bu requesti save yapabilmesi icin
    // Service katmanina ulastirabilirmisiniz lutfen
    public ResponseMessage<LessonsResponse> save(@RequestBody @Valid LessonRequest lessonRequest) {
                                            // Hazir buradayken sizin ihtiyaciniz olacak save endpoint in den
                                            // gelecek olan yapi ile Json formatinda LessonRequest lesson objesi
                                            // olusturalim ne dersiniz :).. Lutfen bu datalar DB ye kayit ederken Pojo
                                            // ya cevirmeyi unutmayin simdilik hosca kalin
                                            // tekrar gorusmek dilegi ile
        return lessonService.save(lessonRequest);
        // Service katinda save methodu olusturmak icin ugrasmyin lutfen yukarida kirmizi yanan save in uzerine gelip
        // Service katinada otomatik olarak bir save methodu cretae edebilirsiniz
    }

    // Not :  Delete() *************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @DeleteMapping("/delete/{id}") // http://localhost:8080/lessons/delete/1
    public ResponseMessage deleteLesson(@PathVariable Long id) {
        return lessonService.deleteLesson(id);
    }

    // Not : getLessonByLessonName() **********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/getLessonByName") // http://localhost:8080/lessons/getLessonByName?lessonName=Math
    public ResponseMessage<LessonsResponse> getLessonByLessonName(@RequestParam String lessonName) {
        return lessonService.getLessonByLessonName(lessonName);// getLessonByLessonName uzerine gelip Service
        // katmaninda bu adda bir methot olusturmussundur umarim cunku orda logical islemler yapilacak

    }

    // Not :  getAllLesson() **********************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')") // TODO student veya teacher kelenebilir mi ??
    @GetMapping("/getAll")  // http://localhost:8080/lessons/getAll
    public List<LessonsResponse> getAllLesson() {//Respnse mesaj turunde de gonderebilirsin ama herhangi bir parametre
        // almadan hepsi gonderilecegi icin basit simple bir List seklinde de gonderebilirisin.
        return lessonService.getAllLesson(); // buradan otomatik olarak Service katmaninda bir method olutururuz.
        // bunun icin seni bir daha uyarmayacagim kendin bunu yapabilirsin artik alistin cunku
    }

    // Not :  getAllWithPage() **********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/search")   // http://localhost:8080/lessons/search?page=0&size=1&sort=id&type=desc
    public Page<LessonsResponse> search(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ) {
        return lessonService.search(page,size,sort,type);
    }

    // Not :  getAllLessonByLessonIds() *****************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    @GetMapping("/getAllLessonByLessonId")  // http://localhost:8080/lessons/getAllLessonByLessonId?lessonId=1,2
    public Set<Lesson> getAllLessonByLessonId(@RequestParam(name = "lessonId") Set<Long> idList){
        // Gelen objenin ki burada obje Lesson oluyor Lesson larin Unique olmasi icin Set methodunu kullnmalisin.
        // Burayi biraz acmaliyim cunku ilk defa goruyorun galiba burayi. Soyle;
        // method 'a  getAllLessonByLessonId ismini verelim
        // Burada id leri almamiz lazim birden fazla data gelecegi icin lessonId tarzi bir yapi olusturlmali
        // dolayisi ile bunu @RequestParam ile alabiliriz. birden fazla olacagi icin DB de bunu ismlendirmek icin
        // name attribute i (name = "lessonId") olsun diyoruz buradan gelen data \,
        // icinde bizim <Long> verilerini tutan idList ile Set lensin.
        // Bize <Long> tibinde id ler gelecek bunlar (name = "lessonId") adi altinda toplanacak ve bu gelen collection
        // list yapidaki idList e maplenecek
        return lessonService.getLessonByLessonIdList(idList);
    }

    // TODO : Update methodu yazilacak

}
