package com.schoolmanagement.controller;

import com.schoolmanagement.entity.concretes.Student;
import com.schoolmanagement.payload.request.ChooseLessonProgramWithId;
import com.schoolmanagement.payload.request.StudentRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.StudentResponse;
import com.schoolmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    // Not: deleteStudent() ******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @DeleteMapping("/delete/{studentId}")
    public ResponseMessage<?> deleteStudent(@PathVariable Long studentId){// path den gelen studentId yi @PathVariable
        // ile Long data turundeki studentId ye map liyoruz .

        return studentService.deleteStudent(studentId);
    }

    // Not: getStudentByName() ***************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getStudentByName")
    // endpointten gelen datayi @PathVariable ilede alabilirdik
    public List<StudentResponse> getStudentByName(@RequestParam(name = "name") String studentName){//ByName e gore
        // student getirecegi icin birden fazla ayni isimde student olabilir bu nedenle icinde StudentResponse lar
        // barindiran List yapi olusturuyoruz

        // Requestin parametresinden name adinda (name = "name") entrypoint gelecek, gelen datayi @RequestParam ile
        // String data turundeki studentName variable sinin icine map liyoruz.

        return studentService.getStudentByName(studentName);
    }

    // Not: getStudentById() ******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getStudentById")
    // TODO donen deger POJO olmamali DTO olarak donmemiz gerekiyor ResponseMessage<StudentResponse>
    public Student getStudentById(@RequestParam(name = "id") Long id) {//Owner den Student Pojo sunu dondurmemizi
        // istedigi icin StudentResponse yazmiyoruz.
        // endPoint den id isminde bir deger gelecek @RequestParam ile bu degeri Long type indeki id ye set liyoruz.
        return studentService.getStudentByIdForResponse(id);
    }

    // Not: getAllStudentWithPage() ***********************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/search")
    public Page<StudentResponse> search(
        //Pageable yapinin ihtiyac duydugu datalari @RequestParam ile value parametre keywordunu kullanarak Service
        // katmaninda kullanacagimiz degiskenlere setliyoruz int page, int size, String sort gibi.
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ){
        return studentService.search(page,size,sort,type);
    }

    // Not: chooseLessonProgramById() *********************************************
    // Bir Student kendine LessonProgram belirleyecek.
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    @PostMapping("/chooseLesson")// Aslinada bu bir creation islemi Ogrencinin taplosuna yeni birseyler eklenecek
    public ResponseMessage<StudentResponse> chooseLesson(HttpServletRequest request,//HttpServletRequest ile
                                                         // kullanicinin olusturdugu request den gelen datalarindan
                                                         // ihtiyacimiz olani alabiliriz. birde Student in secip request
                                                         // olusturdugu LessonProgram in Id sini almaliyiz bu id yi yine
                                                         // requestin kendisinden de alabiliriz ama burada bunu requestin
                                                         // body sinden Json formatta lacagiz. bu en iyisi ama sonucta
                                                         // biz sadece bir tane data alacagiz bunu neden Json
                                                         // formatta alalim? in this case bize birden fazla id de
                                                         // gerekebilir.
                                                         // *Burada bize gerekli olan datalari farkli yollarla almamizin
                                                         // sebebi Service katinda islem yaparken datalarin karismasinin
                                                         // onune gecmek
                                                         @RequestBody @Valid
                                                             // Json yapida bir data almak istiyorsak Student in Lesson
                                                             // secimi yapabilecegi bir request classi olusturuyoruz
                                                             ChooseLessonProgramWithId chooseLessonProgramRequest){
        // bu kisimn servicede yazilirsa daha iyi olur
                                    // artik elimizde bir request var. request e git bunun bir t.getAttribute() methodu
                                    // vardi key degeri ("username") olan fieldin valuesini getir diyoruz. Bu method
                                    // requesti karsilayacagi icin gelen requestin Attribute larina bakacak key degeri
                                    // username olan fieldin value sini alacak. gelen datayida
        // String data turundeki username icine setleyecek.
        String username = (String) request.getAttribute("username");// bu normelde obje olarak donduruyor burada bir
                         // cast islemi yapiyoruz ve datat turunu (String) olarak belirliyoruz.

        return studentService.chooseLesson(username,chooseLessonProgramRequest);
    }

}
