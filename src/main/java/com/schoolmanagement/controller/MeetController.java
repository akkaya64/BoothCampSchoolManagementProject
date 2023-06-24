package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.MeetRequestWithoutId;
import com.schoolmanagement.payload.response.MeetResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.MeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("meet")
@RequiredArgsConstructor
public class MeetController {
    private final MeetService meetService;

    // Not :  Save() *************************************************************************
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    @PostMapping("/save")
    public ResponseMessage<MeetResponse> save(HttpServletRequest httpServletRequest ,
                                              @RequestBody @Valid MeetRequestWithoutId meetRequest){
        String username = httpServletRequest.getHeader("username");
        return meetService.save(username, meetRequest);
    }
}
