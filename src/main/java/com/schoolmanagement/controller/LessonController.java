package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.LessonRequest;
import com.schoolmanagement.payload.response.LessonsResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController// Restfull APi yapmak icin kullanilir
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    // Not :  Save() *************************************************************************

    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
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

}
