package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.abstracts.TeacherResponse;
import com.schoolmanagement.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
}
