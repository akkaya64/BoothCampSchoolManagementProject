package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.AdvisorTeacher;
import com.schoolmanagement.entity.concretes.Teacher;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.response.AdvisorTeacherResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.repository.AdvisorTeacherRepository;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvisorTeacherService {
    private final AdvisorTeacherRepository advisorTeacherRepository;
    //private final AdvisorTeacher advisorTeacher;
    private final UserRoleService userRoleService;

    // Not: deleteAdvisorTeacher() ******************************************************
    public ResponseMessage<?> deleteAdvisorTeacher(Long id) {
        // advisorTeacherRepository de yukarida verdigimiz id de bir AdvisorTeacher varsa AdvisorTeacher data
        // type ndeki advisorTeacher variable ine ata ama eger yoksa orElseThrow() user bulunamadi exceptionu nu dondur.
        AdvisorTeacher advisorTeacher = advisorTeacherRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));
        // eger varsa springframework bir zahmet bu user i siliver.
        advisorTeacherRepository.deleteById(id);// Methoda parametre olarak verdigimiz id ilede silebiliriz
        // advisorTeacher degiskenindeki user objesinin id sini getirerek de silebiliriz
        // advisorTeacherRepository.deleteById(advisorTeacher.getId());

        // baska bir silme islemini de su sekilde yapabiliriz spring framework den gelen delete() methoduna
        // advisorTeacher variable inide gondererek silebiliriz
       // advisorTeacherRepository.delete(advisorTeacher);

       return ResponseMessage.builder()
               .message("Advisor Teacher Deleted Successfully")
               .httpStatus(HttpStatus.OK)
               .build();
    }

    // Not: getAllAdvisorTeacher() ******************************************************
    public List<AdvisorTeacherResponse> getAllAdvisorTeacher() {
        return advisorTeacherRepository.findAll() // advisorTeacherRepository den findAll() methoduyla bir pojo objesi
        // geliyor bunu stream() akisi ile DTO ya map lememiz lazim
                .stream()
                .map(this :: createResponseObject)
                .collect(Collectors.toList());// Pojo gelen akisi Dto ya mapleme islemi yapabilmemiz icin once Pojo DTO donusumunu yaqpan
        // yardimci methodu olusturmaliyiz
    }

    private AdvisorTeacherResponse createResponseObject(AdvisorTeacher advisorTeacher){
        return AdvisorTeacherResponse.builder()// builder() methodu ile AdvisorTeacherResponse objesi olusturuyoruz
                .advisorTeacherId(advisorTeacher.getId())//AdvisorTeacherResponse Class inin advisorTeacherId
                // variable ina DB deki advisorTeacher in getId() id sini getirip atiyoruz.
                .teacherName(advisorTeacher.getTeacher().getName())// AdvisorTeacherResponse Class inin teacherName
                // variable ina DB deki advisorTeacher tablosuna gidip @OneT0One iliski kurulu olan Teacher tablosunu
                // getirip bu teacher toblosundan da Teacher in ismini getirip atayacagiz
                .teacherSurname(advisorTeacher.getTeacher().getSurname())// Yine advisorTeacher tablosuna gidip
                // aralarinda @OneToOne iliski bulunan getTeacher() teacher i getirip oradan da
                // getSurname() teacher in soy ismini alip advisorTeacher in teacherSurname variable ina atayacagiz
                .teacherSSN(advisorTeacher.getTeacher().getSsn())//DB deki advisorTeacher dan teacher i getTeacher() getir,
                // gelen teacherin SSN numarsina getSsn() al AdvisorTeacherResponse clasindaki teacherSSN variable ina ata
                .build();

    }

    // Not: getAllAdvisorTeacherWithPage() **********************************************
    public Page<AdvisorTeacherResponse> search(int page, int size, String sort, String type) {
        Pageable pageable = PageRequest.of(page,size, Sort.by(sort).ascending());
        if(Objects.equals(type,"desc")){
            pageable = PageRequest.of(page,size, Sort.by(sort).descending());
        }

        return advisorTeacherRepository.findAll(pageable).map(this::createResponseObject);
    }

    // Not: TeacherService icin gerekli methodlar **********************************************
    // TeacherService katmaninda kayit islemi gerceklestirilen bir teacher in Advisor olup olmadigini
    // setledigimiz yardimci logicalcodelar

    // Not: saveAdvisorTeacher() ****************************************************
    public void saveAdvisorTeacher(Teacher teacher) {// TeacherServiceden create ettigimiz bu methoda parametre
        // olarak gelen Teacher type indeki savedTeacher ismini (Teacher savedTeacher),
        // teacher olarak (Teacher teacher) degistirebiliriz. ama bunun icin advisorTeacher Classini bu clasa
        // injection etmeliyiz

        AdvisorTeacher advisorTeacher = AdvisorTeacher.builder()
                .teacher(teacher)// AdvisorTeacher in teacher variable ina TeacherService classindan gelen,
                // saveAdvisorTeacher methoduna parametre olarak verdigimiz DB ye kayit edilen
                // teacher(sevedTeacher) i atiyoruz.
                .userRole(userRoleService.getUserRole(RoleType.ADVISORTEACHER))// AdvisorTeacher in userRole variable
                // ina enum olan RoleType.ADVISORTEACHER rolunu atiyoruz.
                .build();
        advisorTeacherRepository.save(advisorTeacher);

//        builder() methodu ile olusturdugumuz AdvisorTeacher objesini save methoduna asagidaki gibi parametre olarak
//        vererek te yapabiliriz ancak yukaridaki gibi AdvisorTeacher Objesini bir variable a atayarak bu veriable ida
//        save methoduna parametre olarak vererek daha clean bir save islemi yapmis oluruz.

//        advisorTeacherRepository.save(AdvisorTeacher.builder()
//                .teacher(teacher)
//                .userRole(userRoleService.getUserRole(RoleType.ADVISORTEACHER))
//                .build());
    }

    // Not: updateAdvisorTeacher() ****************************************************

    public void updateAdvisorTeacher(boolean status, Teacher teacher) {//Code yazimini kisaltmak icin
        // TeacherService den otomatik olarak create ettigimiz de default olarak olusan boolean advisorTeacher in ve
        // Teacher savedTeacher veriablellerinin isimlerini  boolean status ve Teacher teacher olarak degistiriyoruz.

        // !!! teacher objesi uzerinden teacherId ile iliskilendirilmis AdvisorTeacher nesnesini DB den bulup getiriyoruz
        Optional<AdvisorTeacher> advisorTeacher =
                //keyWord ler kullanarak bir method olusturacagiz.
                advisorTeacherRepository.getAdvisorTeacherByTeacher_Id(teacher.getId());// advisorTeacherRepository e git
        // Teacher_Id Teacher in id si ile, ilgili id nin getAdvisorTeacher tablosunu getir.(TeacherId diye bir variable
        // olmadigi icin Teacher in id si oldugunu belirtmek icin (_) isaretb ile ayiriyoruz Teacher_Id) diyoruz.
        // parametre olarakda teacher den gelen id yi veriyoruz (teacher.getId()).
        // getAdvisorTeacherByTeacher_Id() methodunu buradan otomatik olarak repository katmaninda create ediyoruz.
        // methodun dondurdugu deger Optional bir deger oldugu icin bunu <AdvisorTeacher> parametreli Optional bir
        // degisken olan advisorTeacher container in icine setliyoruz.

        // Elimizde bir AdvisorTeacher objesi olmaliki ihtiyac duydugumuz islemleri onun uzerinde yapabilelim.
        // builder() methodu ile  AdvisorTeacher turunde bir Object olusturuyoruz bu objenin fieldlarindan biri teacher
        // digeri RoleType.. AdvisorTeacher Object ini AdvisorTeacherBuilder interface sinden turetilen AdvisorTeacher
        // turundeki advisorTeacherBuilder variable ina atiyoruz.
        AdvisorTeacher.AdvisorTeacherBuilder advisorTeacherBuilder = AdvisorTeacher.builder()
                .teacher(teacher)
                .userRole(userRoleService.getUserRole(RoleType.ADVISORTEACHER));
        // Henuz bu yapi tam olarak tamamlanmadigi icin eksik datalari var (isAdvisorTeacher rolu yeniden setlenecegi
        // icin ve hebnuz belli olmadigi icin)o datalar setlenmeden gonderilmesin diye build() methodunu cagirmiyoruz.
        // bunun yerine AdvisorTeacher entity classindaki @Builder annotationundan gelen AdvisorTeacherBuilder
        // interface sini kullaniyoruz. Builder() methodu henuz calismiyor, bos bir kalip olusturduk.

        if(advisorTeacher.isPresent()) {// yukaridan gelen advisorTeacher objesinin ici dolu mu? isPresent().
            // yukarida Optional yapidaki advisorTeacher variable ini orElseThrow gibi bir yapi ile kontrol
            // etmedigimiz icin burada handle ediyoruz.

            if(status){// eger isAdvisorTeacher i true ise advisorTeacher object ini guncelliyoruz ve DB ye kayit ediyoruz.
                //isAdvisorTeacher false ye cekildigindeteacher in AdvisorTeacher rolu ile hicbir iliskisi kalmamis oluyor.
                advisorTeacherBuilder.id(advisorTeacher.get().getId());// advisorTeacher in id si eksik. id sini
                // setlemeliyizki kayit islemi yapilabilsin.  advisorTeacher in get() methodunun getId() methodu ile
                // id sini getir ve advisorTeacherBuilder in eksik olan .id sine setle
                advisorTeacherRepository.save(advisorTeacherBuilder.build());// advisorTeacherBuilder.build() ile
                // olusan yeni Object i advisorTeacherRepository in save() methodu ile DB de ye kayit ediyoruz.
            } else {// eger request true degilse advisorTeacher i siliyoruz. Yani artik teacher tablosu ile AdvisorTeacher
                // tablosu arasindaki iliskiyi sonlandigi icin AdvisorTeacher tablosunda gereksiz yer kaplayan objesini
                // silinmesi gerekiyor.
                advisorTeacherRepository.deleteById(advisorTeacher.get().getId());// simdiye kadar advisorTeacher
                // taplosu ile iliskisi olan ve isAdvisorTeacher statusu false cekilen bu teacherin requestten gelen id
                // ye gore advisorTeacher toplosundaki tum datalarini siliyoruz.
            }
        } else {// eger advisorTeacher.isPresent() eger bossa yani teacher in ADVISORTEACHER rolu hic yoksa
            // advisorTeacherBuilder.build() objesini advisorTeacherRepository tablosuna direct kayit ediyoruz
            advisorTeacherRepository.save(advisorTeacherBuilder.build()); // TODO buraya bakilacak
        }

/*        if (advisorTeacher.isPresent()) {
            if (status) {
                advisorTeacherBuilder.id(advisorTeacher.get().getId());
                advisorTeacherRepository.save(advisorTeacherBuilder.build());
            } else {
                advisorTeacherRepository.deleteById(advisorTeacher.get().getId());
            }
        }*/

    }

    // Not: StudentService icin gerekli metod ***************************
    // Student rolu olusturlurken Student e id numarasi kullanarak bir AdvisorTeacher fieldi eklenmesi gerekiyor.
    // bu nedenle StudentService Classinda save methodunu olusturken buraya gelip id si ile AdvisorTeacher getiren
    // methodu create edecegiz. Bu method Student objesi olusturulurken studentin advisorTeacher ini student object ine
    // eklerken calistirilacak.. Studenti create ederken advisor teacher ini de belirlemis olacagiz.
    public Optional<AdvisorTeacher> getAdvisorTeacherById(Long id) {
        return advisorTeacherRepository.findById(id);
    }

    public Optional<AdvisorTeacher> getAdvisorTeacherByUsername(String username) {

        return advisorTeacherRepository.findByTeacher_UsernameEquals(username);
    }
}
