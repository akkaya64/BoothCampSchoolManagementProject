package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.EducationTermRequest;
import com.schoolmanagement.payload.response.EducationTermResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.EducationTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("educationTerms")
@RequiredArgsConstructor
public class EducationTermController {

    private final EducationTermService educationTermService;

    // Not :  Save() *************************************************************************

//    {
//        "id": 1,
//            "term":"FALL_SEMESTER",
//            "startDate":"2023=01-01",
//            "endDate":"2023-02-28",
//            "lastRegistrationDate":"2022-12-20"
//    }
    @PostMapping("/save")   // http://localhost:8080/educationTerms/save
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    public ResponseMessage<EducationTermResponse> save(@RequestBody @Valid EducationTermRequest educationTermRequest){

        return educationTermService.save(educationTermRequest);

    }
    // Not :  getById() ************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER','TEACHER')") // student ?
    @GetMapping("/{id}")  // http://localhost:8080/educationTerms/1
    public EducationTermResponse get(@PathVariable Long id){
        return educationTermService.get(id);
    }

    // Not :  getAll() *************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER','TEACHER')")
    @GetMapping("/getAll")  // http://localhost:8080/educationTerms/getAll
    public List<EducationTermResponse> getAll() {
        return educationTermService.getAll();
    }

    // Not :  getAllWithPage() ******************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER','TEACHER')")
    @GetMapping("/search") // http://localhost:8080/educationTerms/search?page=0&size=10&sort=startDate&type=desc
    public Page<EducationTermResponse> getAllWithPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "startDate") String sort,
            @RequestParam(value = "type", defaultValue = "desc") String type
    ){
        return educationTermService.getAllWithPage(page,size,sort,type);
    }

    // Not :  Delete() *************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @DeleteMapping("/delete/{id}") // http://localhost:8080/educationTerms/delete/1
    public ResponseMessage<?> delete(@PathVariable Long id){//Best Practice olani id nin basina bir tanimlaa kelimesi
        // kullanilmasi gerekir. mesela bu id education da kullanilan id  gibi educationId

        return educationTermService.delete(id);

    }

//    // Not :  UpdateById() ********************************************************************
//    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
//    @DeleteMapping("/update/{id}") // http://localhost:8080/educationTerms/update/1
//    public ResponseMessage<EducationTermResponse> update(@PathVariable Long id,
//                                                         @RequestBody @Valid EducationTermRequest educationTermRequest){
//
//        return educationTermService.update(id, educationTermRequest);
//
//    }

    // Not :  UpdateById() ********************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PutMapping("/update/{id}")  // http://localhost:8080/educationTerms/update/1
    public ResponseMessage<EducationTermResponse> update(@PathVariable Long id,
                                                         @RequestBody @Valid EducationTermRequest educationTermRequest){
        return educationTermService.update(id, educationTermRequest);

    }



}