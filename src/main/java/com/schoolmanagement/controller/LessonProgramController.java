package com.schoolmanagement.controller;
// Bu controller katmanini olusturduguna gore buyuk ihtimal Rest Full API yapacaksin. Kesinlikle bu cok iyi bir fikir
// ama once bunu Springframewoorke classin basina gerekli annotationu koyarak bildirmelisin

import com.schoolmanagement.payload.request.LessonProgramRequest;
import com.schoolmanagement.payload.response.LessonProgramResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.service.LessonProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController// tam da isleri kolaylastirmak icin ihtiyacim olan takim cantasi
@RequestMapping("/lessonPrograms")// kesinlikle requsetleri burada karilayacaksin dostum harikasin... Gelenleri
// kacirma hemen iceri al
@RequiredArgsConstructor// olmazsa olmaz ne yapiyordu lan bu bir google yapayim. kesin bir ise yariyordur ama neye!!!
public class LessonProgramController {

    private final LessonProgramService lessonProgramService; //ilk etapta Service katmani ile iliski kurulacak

    // Not :  Save() *************************************************************************
    @PostMapping("/save")  // http://localhost:8080/lessonPrograms/save
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    // Hey daha fazla ilerlemeden ResponseMessage generic olarak verecegin classi once bir olustur lutfen yoksa
    // kullniciya hangi objeyi dondureceksin... Tabiki Kullanicidan alacagin bilgiler icinde bir request calas
    // olusturman  gerektigini biliyorsundur...
    public ResponseMessage<LessonProgramResponse> save(@RequestBody @Valid LessonProgramRequest lessonProgramRequest) {
        return lessonProgramService.save(lessonProgramRequest);
    }

    // Not :  getAll() *************************************************************************
    @GetMapping("/getAll")  // http://localhost:8080/lessonPrograms/getAll
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER','STUDENT')")
    public List<LessonProgramResponse> getAll() {
        return lessonProgramService.getAllLessonProgram();
    }

    // Not :  getById() ************************************************************************

    @GetMapping("/getById/{id}") //http://localhost:8080/lessonPrograms/getById/1
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    public LessonProgramResponse getById(@PathVariable Long id) {
        return lessonProgramService.getByLessonProgramId(id);
    }

    // Not :  getAllLessonProgramUnassigned() **************************************************
    // Henuz Teacher atamasi yapilmamais LessonProgram lar
    @GetMapping("/getAllUnassigned") //http://localhost:8080/lessonPrograms/getAllUnassigned
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER','STUDENT')")
    public List<LessonProgramResponse> getAllUnassigned() {
        return lessonProgramService.getAllLessonProgramUnassigned();
    }

    // Not :  getAllLessonProgramAssigned() **************************************************
    @GetMapping("/getAllAssigned") //http://localhost:8080/lessonPrograms/getAllAssigned
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER','STUDENT')")
    public List<LessonProgramResponse> getAllAssigned() {
        return lessonProgramService.getAllLessonProgramAssigned();
    }

    // Not :  Delete() *************************************************************************
    @DeleteMapping("/delete/{id}") //http://localhost:8080/lessonPrograms/delete/1
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER')")
    public ResponseMessage delete (@PathVariable Long id) {
        return lessonProgramService.deleteLessonProgram(id);
    }

    // Not :  getLessonProgramByTeacher() ******************************************************
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN','MANAGER','ASSISTANTMANAGER')")
    @GetMapping("/getAllLessonProgramByTeacher")  //http://localhost:8080/lessonPrograms/getAllLessonProgramByTeacher
    // Bu endpointi farkli bir sekilde calisacagiz. datayi daha once yaptiklarimizda endpointin icinden olusan {id} gibi
    // field isimlerinden @PathVariable ile iliskilendiriyorduk bu fieldin ismi endpoint de gorunmesin ama silinecek
    // olan objeye ait bilgileri almam lazim diyorsak o zaman bunun ikinci yolu da request uzerinden gonderebiliriz
    // Request ler aslinda birer API packagaler bunlarin attribute leri var bu attributelerin uzerine yeni bilgiler
    // setleyebiliriz. Bu attribute ler uzerinden uniue degerler uzerinden alabiliriz. HttpServletRequest ile gelen
    // requeste ulasabiliriz ve bu request uzerinde herhangi bir islem yapabiliriz. Bir request olacakki asagidaki
    // methoda dallanabilelim. Ve bu methodun dallanmamiza sebeb olan requestte de bu method icinden ulasabiliyoruz
    // ulasabiliyoruz.
    public Set<LessonProgramResponse> getAllLessonProgramByTeacherId(HttpServletRequest httpServletRequest) {

        String username = (String) httpServletRequest.getAttribute("username");// httpServletRequest in
        // .getAttribute() diye bir methodu var, bu string bir deger aliyor buraya kullanicinin unique bir degeri olan
        // "username" i parametre olarak veriyoruz. Ve bu String bir deger aliyor bu nedenle httpServletRequest e cast
        // islemi yapiyoruz (String) ve buradan gelen bilgiyi String data turunde username isimli bir
        // veriable ye atiyoruz
        return lessonProgramService.getLessonProgramByTeacher(username);

        // Bu Datayi:
        // @PathVariable
        // @RequestParam
        // HttpServletRequest
        // @RequestBody
        // Asagidaki yanlis bilgi: anlik olan kullanicinin bilgilerini gonderir
        // Anlik olarak login olan kullaniciya ulasmak icin SpringSecurit in bir methodu olan @getPrincipal()
        // yollorindan birini kullanabilriz. Hocam Methodu gonderebilirmisiniz
    }

    // Not :  getLessonProgramByStudent() ******************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','STUDENT','TEACHER')")
    @GetMapping("/getAllLessonProgramByStudent") //http://localhost:8080/lessonPrograms/getAllLessonProgramByStudent
    public Set<LessonProgramResponse> getAllLessonProgramByStudent(HttpServletRequest httpServletRequest) {
        // HttpServletRequest Classini almamizin sebebi Requestteki unique bir deger olan username ulasmak.

        // @getPrincipal() methodu o an currently login olan kullanicinin id si uzerinden kullaniciyi getirecegi icin
        // mesela bir Teacher bir Studenti getirmek istediginde o an kendisi login oldugu icin user bilgisi olarak
        // kendi bilgileri gelir. Bir ogrenci kendi Lesson Programina bakmak istediginde @getPrincipal() ile kendi
        // id si gelecegi icin kendine ait LessonProgrami cagirabilir.

        // @RequestBody ile bir Json dosya olarak da alabiliriz Requestin body sinde username ne ise onun uzerinden
        // ihtiyacimiz ne ise onu getirebiliriz.


        //Asagidaki statamenti Service Layer de de yazabiriz
        String username = (String) httpServletRequest.getAttribute("username");
        // httpServletRequest Class inin .getAttribute() diye bir methodu var bunun ile ("username") in value sini getir
        // .getAttribute methodu gelen objein data turune bakmaz.. bu method ne gelecegini bilmedigi icin Obje yi en
        // parent data type olan Object olarak donduruyor bu nedenle String bir Variable nin icine atilacaksa gelen
        // obje Object degerini Casting yapmaliyiz burada String bir data turune sahip bir variable ye atanacagi icin
        // (String) ile casting islemi yapiliyor

        return lessonProgramService.getLessonProgramByStudent(username);
    }

    // Not :  getAllWithPage() ******************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANTMANAGER','TEACHER','STUDENT')")
    @GetMapping("/search")
    public Page<LessonProgramResponse> search(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ){
        return lessonProgramService.search(page,size,sort,type);
    }
}
