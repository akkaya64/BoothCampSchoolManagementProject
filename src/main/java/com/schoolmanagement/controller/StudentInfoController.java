package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.StudentInfoRequestWithoutTeacherId;
import com.schoolmanagement.payload.request.UpdateStudentInfoRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.StudentInfoResponse;
import com.schoolmanagement.service.StudentInfoService;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/studentInfo")
@RequiredArgsConstructor
public class StudentInfoController {

    private final StudentInfoService studentInfoService;

    // Not: save()****************************************************************
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    @PostMapping("/save")
    public ResponseMessage<StudentInfoResponse> save(HttpServletRequest httpServletRequest,
                                                     @RequestBody
                                                     @Valid
                                                             //StudentInfo yu olusturma ve kayit etmeyi ogretmenin
                                                     // kendisi yapacagi icin bu seneryo icin olusturdugumuz
                                                     // StudentInfoRequestWithoutTeacherId request classinin icinde
                                                     // Teachere ait bir id ye ihtiyac yok. Teacher rolu zaten login
                                                     // oldugu icin teacher pathvariable den id si ni alip Db den
                                                     // teacher i getirmek icinde kullanbilirdik ama biz burada teacher
                                                     // in yine unique bir degeri olan username ini httpServletRequest
                                                     // den getirip bu methodun parametresine verecegiz. bu username ile
                                                     // service katmaninda teacher ile ilgili logical islemler
                                                     // yapilabilecek
                                                     StudentInfoRequestWithoutTeacherId studentInfoRequestWithoutTeacherId){
        // StudentInfoRequestWithoutTeacherId
        //TODO HttpServletRequest bunun ne getirdigini ne ise yaradigini hic anlamadim Day18 30.dk

        String username = (String) httpServletRequest.getAttribute("username");// httpServletRequest den
        // .getAttribute() methodu ile header i ("username") olan keyword un value sini getirip Sitring data turundeki
        // username variable in icine koyacak.
        return studentInfoService.save(username, studentInfoRequestWithoutTeacherId);

    }

    // Not: delete()****************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @DeleteMapping("/delete/{studentInfoId}")
    public ResponseMessage<?> delete(@PathVariable Long studentInfoId) {
        return studentInfoService.deleteStudentInfo(studentInfoId);
    }

    // Not: update()****************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PutMapping("/update/{studentInfoId}")//Hangi studentinfo update edilecekse path variable ile bu id yi aliyoruz. ve
    // bu unique degeri methodun parametresine veriyoruz ki logical islemlerin yapilacagi service katmanina
    // gonderebilelim Bu methodlarin parametresine pojo sinifi entity class objeleri degil requestten gelen Json
    // formatta objeler almamiz lazim. path dan aldigimiz studentInfoId yide birazdan olusturacagimiz request class
    // indan da alabilirdik bu tamamen tarimi nasil kuracagimiz ile lakali. Her seneryo icin kendine has dto yani bir
    // request class i olusturmak bestPractice. mesela bu seneryo icin birazdan bir UpdateStudentInfoRequest adinda
    // request classi olusturacagiz Service katmaninda gereksuiz fieldlari setleyip durmak zorunda kalmayacagiz.
    public ResponseMessage<StudentInfoResponse> update(@RequestBody @Valid UpdateStudentInfoRequest studentInfoRequest,
                                                       @PathVariable Long studentInfoId){
        return studentInfoService.update(studentInfoRequest,studentInfoId);

    }

    // Not: getAllForAdmin()*********************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/getAllForAdmin")
    public ResponseEntity<Page<StudentInfoResponse>> getAll(//bu zamana kadar custom olarak ResponseMessage dondurduk
     // ama donen degerler arasinda su anda en guncel yapi ResponseEntity.
     // <Page<StudentInfoResponse>> ResponseEntity, Page yapida olacak bu page yapi icine StudentInfoResponse turunde
     // degerler alacak.
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size
    ){
        // Pageable obje olusturma islemini Service katinda yazilmasi best-practice.. Hemen alttaki methodda kendimize
        // custom bir pagable yapi olusturduk.. Pageable yapinin ozelliklerini biz setledik
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());//Springframework un
        // PageRequest interface nin .of() methodu ile yapinin  page nin olacagini, size; page in  icine alacagi objelerin
        // sayisini Short.By methoduna verecegimiz athribute kelimesi ile siralamanin hangi fielde gore yapilacagini
        // .descending() methodu ilede siralamanin descending olacagini belitiyoruz ve donen deger bir pageable data
        // turunde olacagi icin bunu Pageable data turundeki pageable degiskenine atiyoruz
        Page<StudentInfoResponse> studentInfoResponse =  studentInfoService.getAllForAdmin(pageable);// studentInfoService
        // katmanina git orada bir getAllForAdmin isimli method olacak yukarda olusturdugumuz pagable yapisini o
        // methoda arguman olarak gonder donen degeride icine StudentInfoResponse alan Page data type
        // studentInfoResponse adindaki degiskenine koy

        return new ResponseEntity<>(studentInfoResponse, HttpStatus.OK);
    }

    // Not: getAllForTeacher()*********************************************************

    // --> Bir ogretmen kendi ogrencilerinin bilgilerini almak istedigi zaman bu method calisacak
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    @GetMapping("/getAllForTeacher")
    public ResponseEntity<Page<StudentInfoResponse>> getAllForTeacher(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size
    ){
        // Pageable obje olusturma islemini Service katinda yazilmasi best-practice
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String username = (String) httpServletRequest.getAttribute("username");
        // Auto Casting yapip String turune cevirdik (String) cunku getAttribute() methodu Object turde bir yapi
        // donduruyor
        // getAttribute("username"); e parametre olarak username verdik. Method  httpServletRequest e gidecek Attribute
        // olarak verdigimiz username ile eslesen Keyword larin value lerini bulup getirecek ve Auto Casting yaparak
        // String data turundeki username degiskeninin icine koyacak

        Page<StudentInfoResponse> studentInfoResponse =  studentInfoService.getAllTeacher(pageable,username);

        return new ResponseEntity<>(studentInfoResponse, HttpStatus.OK); // ResponseEntity.ok(studentInfoResponse);

    }

    // Not: getAllForStudent()*********************************************************
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    @GetMapping("/getAllByStudent")
    public ResponseEntity<Page<StudentInfoResponse>> getAllByStudent(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size

    ){
        // Pageable obje olusturma islemini Service katinda yazilmasi best-practice
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String username = (String) httpServletRequest.getAttribute("username");
        Page<StudentInfoResponse> studentInfoResponse = studentInfoService.getAllStudentInfoByStudent(username,pageable);
        return ResponseEntity.ok(studentInfoResponse);
    }

    // Not: getStudentInfoByStudentId()*************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER')")
    @GetMapping("/getByStudentId/{studentId}")
    public ResponseEntity<List<StudentInfoResponse>> getStudentId(@PathVariable Long studentId){

        List<StudentInfoResponse> studentInfoResponse = studentInfoService.getStudentInfoByStudentId(studentId);
        return ResponseEntity.ok(studentInfoResponse);

    }

    // Not: getStudentInfoById()*******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER')")
    @GetMapping("/get/{id}")
    public ResponseEntity<StudentInfoResponse> get(@PathVariable Long id){

        StudentInfoResponse studentInfoResponse = studentInfoService.findStudentInfoById(id);
        return ResponseEntity.ok(studentInfoResponse);
    }

    // Not: getAllWithPage()******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/search")
    public Page<StudentInfoResponse> search(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ) {
        return  studentInfoService.search(page,size,sort,type);
    }








}
