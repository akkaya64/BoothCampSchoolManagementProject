package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.entity.concretes.Teacher;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.dto.TeacherRequestDto;
import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.abstracts.TeacherResponse;
import com.schoolmanagement.repository.TeacherRepository;
import com.schoolmanagement.utils.CheckParameterUpdateMethod;
import com.schoolmanagement.utils.FieldControl;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {//Evet hadi bu Classi insa etmeye once ihtiyac duyacagimniz bagimliliklari injection
    // olarak kurmakla baslayalim
    private final TeacherRepository teacherRepository;
    private final LessonProgramService lessonProgramService;
    private final FieldControl fieldControl;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRequestDto teacherRequestDto;
    private final UserRoleService userRoleService;
    // private final AdvisorTeacherService advisorTeacherService;


    public ResponseMessage<TeacherResponse> save(TeacherRequest teacherRequest) {
        // TeacherRequest den buraya gelen bazi Lesson ID leri olacak Lesson larin kendileri gelmiyor sadece Id leri
        // geliyor. burada oncelikle gelen id li Lesson DB de varmi kontrol edilmeli

        Set<LessonProgram> lessons = lessonProgramService.getAllLessonProgramById(teacherRequest.getLessonsIdList());
        // Bu yapi lessonProgramService katina gidecek orada buradan otomatic create ettigimiz .getAllLessonProgramById
        // methodu kullanarak parametresine verdigimiz teacherRequest in getLessonsIdList() method na verdigimiz
        // Lesson id nin LessonProgramini Set yapida lessons a atayacak. teacherRequestten gelen lesson id si ile
        // lesson programi bulma islemini LessonProgramService clasina create ettigimiz .getAllLessonProgramById
        // methodu yapacak

        // LessonProgramService katmaninin LessonProgramRepository Katmanindan getirdigi lessons variable ina
        // atatigimiz Set yapidaki LessonProgram objesi bos mu degilmi kontrolu yapilacak. Eger bos sa bir exception
        // firlatilmasi gerekecek.

        if (lessons.size() == 0) {// Set yapidaki listenin size eger 0 ise yani bos gelmisse if in icine girecek.
            // utils packasine gidip Message Classinin icinde once firlatacagimiz mesagenin icerigini uretiyoruz.
            throw new BadRequestException(Messages.LESSON_PROGRAM_NOT_FOUND_MESSAGE);
        } else {// eger gelen set yapisindaki list dolu ise Duplacate kontrolu yapilacak

            // FieldController Classinda repository interfacelerini method parametresinde vermis olsaydik bu class da
            // Fieldlarin duplacateligini kontrol ettigimiz tum repository interfacelerini bu class a DI sion yapacaktik
            // projemiz bosu bosuna hantal bir yapiya sahip olacakti biz neyaptik repository interfacelerinin
            // DI sionlarini FieldController Classinda class seviyede yaptik boylelikle bu methoducagrildigi her class a
            // repository interfacelerini DI siyon yapmaktan kurtulduk kardeism. Su assagidaki FieldController
            // Classindan cagirdigimiz checkDuplicate methoduna bir baksaniz burada ne kadar sade ne kadar temiz duruyor.
            // insanin baktikca ici aciliyor. mutluluktan aglayabiliriz degilmi. ne guzel. Allah Allah ya ne guzel oldu.

            fieldControl.checkDuplicate(
                    teacherRequest.getSurname(),
                    teacherRequest.getSsn(),
                    teacherRequest.getPhoneNumber(),
                    teacherRequest.getEmail()
            );


            // Kayit islemini yaparken TeacherRequest den gelen fieldlara ek olarak burada lessonProgram ida Teacher a
            // field olarak eklememiz lazim. Ders porogramsiz bir Teacher olmaz. Buraya gelen datalar henuz Json
            // formattaki DTO bizim bunlari DB nin anlayacagi Pojo turune cevirecek yardimci methoda ihtiyacimiz var
            // bu yardimci methodu asagida yazacagiz

            // Ayrica Requetten gelen password encode edilmeli bunu icnde passwordlari encode eden methodun icinde
            // bulundugu Classi yukariada bu classa Injection ederek baslayabilirisin umarim boyle bir class
            // olusturdugumuzu ve method yazdigimizi hatirliyorsundur


            // !!! dto -> POJO donusumu
            Teacher teacher = teacherRequestToDto(teacherRequest);
            // !!! Rol bilgisi setleniyor
            teacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));
            // !!! dersProgrami ekleniyor
            teacher.setLessonsProgramList(lessons);
            // !!! sifre encode ediliyor
            teacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));
            // !!! Db ye kayit islemi
            Teacher savedTeacher = teacherRepository.save(teacher);

            return ResponseMessage.<TeacherResponse>builder()
                    .message("Teacher saved successfully")
                    .httpStatus(HttpStatus.CREATED)
                    .object(createTeacherResponse(savedTeacher))
                    .build();
        }

    }

    private Teacher teacherRequestToDto(TeacherRequest teacherRequest) {
        // Burada DTO --> Pojo donusumunu Object Bean ile yapacagiz... buAma once payload packagenin icinde DTO packagesinin icine
        // TeacherRequestToDto adinda bir class olusturuyoruz ki once Kullnicinin girdigi datalari Json formatinda
        // artik uzerinde islem yapabilmek icin codesal  olarak alalabilelim.
        // Yani burada artik sunu yapiyoruz teacherRequestToDto class ina TeacherRequest teacherRequest i parametre
        // olarak verdik ki teacherRequest once bir DTO ya cevrilsin sonra burada Bu Json yapiyi
        // Pojo olarak kayit edelim

        return teacherRequestDto.dtoTeacher(teacherRequest);
        // teacherRequestDto classindaki .dtoTeacher methodu parametresindeki (teacherRequest) i DTO ya cevirip Teacher
        // Pojo su olarak return edecek
    }

    private TeacherResponse createTeacherResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .userId(teacher.getId())
                .username(teacher.getUsername())
                .name(teacher.getName())
                .surname(teacher.getSurname())
                .birthDay(teacher.getBirthDay())
                .birthPlace(teacher.getBirthPlace())
                .ssn(teacher.getSsn())
                .phoneNumber(teacher.getPhoneNumber())
                .gender(teacher.getGender())
                .email(teacher.getEmail())
                .build();
    }

    // Not: getAll() **********************************************************
    public List<TeacherResponse> getAllTeacher() {
        //Hazir CRUD operasyonunu kullanacagiz. DB den veri getiriyorsak .findAll() methodunu kullandigimiz
        // anda veriler pojo olarak gelcek. Bu Pojo verileri Lambda ile .stream() akisina alacagiz ve bu akisi map ile
        // createTeacherResponse methodunu kullanarak DTO ya cevirecegiz. gelen veri hala artik DTO olarak bir
        // .stream() akisinda. Java Utils kutuphanesinde bulunan collect abstrack klasinin child i olan Collector
        // clasindaki .toList() methodunu kullanarak List yapiya ceviriyoruz
        return teacherRepository.findAll()
                .stream()
                .map(this::createTeacherResponse)
                .collect(Collectors.toList());
    }

    // Not: updateTeacherById() ************************************************
    public ResponseMessage<TeacherResponse> updateTeacher(TeacherRequest newTeacher, Long userId) {
        // Ek Bilgi; DB den Optional data type inde gelen objenin icindeki fieldlari almak icin get ile
        // cagirmamiz gerekiyor Optional olmayanalarda o getlerin kalkmasi lazim ona dikkat etmek lazim

        //!!! id uzerinden DB deki teacher nesnesi getiriliyor
        Optional<Teacher> teacher = teacherRepository.findById(userId); // Optional olarak varsa aldik

        // DTO uzerinden eklenecek lessonlar getiriliyor
        // TeacherRequest de BaseUserRequest den fazla olarak icinde Lessonlari barindiran Set yapida bir Lesson lara
        // ait idList var. Teacher e Lesson atamasi yapmak yada sahip oldugu Lessonlari guncellemek icin bu idList lere
        // ait Lessonlari buraya getirip kullanacagiz ama bunun icin LessonProgramService gidip bu Set List yapisindaki
        // id lere ait Lessonlari Set List yapisinda getirecek methodu a parametre olarak  newTeacher request inin
        // .getLessonsIdList()  methodu ile gelen Set List yapisindaki lesson lari verip parametreden gelen bu objeyi
        // Set<LessonProgram> lessons yapisina atamamiz lazim.
        Set<LessonProgram> lessons = lessonProgramService.getAllLessonProgramById(newTeacher.getLessonsIdList());

        // Kontroller icin orElseThrow yapisi ile calismadigimiz icin burada kontrolleri if yapilari ile
        // kendimiz handle ediyoruz
        if(!teacher.isPresent()){ // Teacher Objesinin bos gelip gelmedigini kontrol edip bossa exception firlatiyoruz.
            throw new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE);//Message Clasindan getiriyoruz
        } else if(lessons.size()==0){// Teacher Objesi dolu geldiyse Lesson larin gelip gelmedigini kontrol ediyoruz
            // Lessonlar gelmediyse yani List in size == 0 ise exception firlatiyoruz.
            throw new BadRequestException(Messages.LESSON_PROGRAM_NOT_FOUND_MESSAGE);//Message Clasindan getiriyoruz
        } else if (!checkParameterForUpdateMethod(teacher.get(), newTeacher)) { // Var olan bir Teacher Objesini update
            // ediyoruz bu objemiz zaten unique kontrolu yapilarak olusturuldu. Ama bu objenin update edilebilen unique
            // olmasi gereken degerleri var bu degerlerin degistirilmis olmasi ihtimali icin bu degerler degistirildi
            // ise unique lik kontrolu yapmamiz lazim. iste burada bu degerlerin degistirilip degistirilmedigini kontrol
            // etmemiz lazim. eger degistirilmedi ise unique kontrolu yapmamiza gerek yok
            fieldControl.checkDuplicate(newTeacher.getUsername(),//
                    newTeacher.getSsn(),
                    newTeacher.getPhoneNumber(),
                    newTeacher.getEmail());
        }
        // kayit islemini tamamlayabilmemiz icin elimizdeki DTO Set yapidaki List imizi Pojo Class a cevirmemiz lazim
        // bunun icin updateTeacher scope sinin hemen disinda bunu yapan yardimci bir method yaziyoruz. Teacher Pojo
        // Classini return edecek createUpdatedTeacher() methodunu yaziyoruz.



    }
    private Teacher createUpdatedTeacher(TeacherRequest teacher, Long id){// updateTeacher method una parametre olarak
        // verdigimiz DB den gelen userId yi burada parametre olarak veriyoruz (cunku var olan bir kullaniciyi
        // update edecegiz) ki bu user id sine ait  kullanicidan gelen bilgileri Pojo  ya cevirebilelim.

        return Teacher.builder()
                // yeni bir id degil zaten var olan id yi veriyoruz ki id ayni kalsin id PathVariable dan geliyor.
                .id(id)
                .username(teacher.getUsername())
                .name(teacher.getName())
                .surname(teacher.getSurname())
                .ssn(teacher.getSsn())
                .birthDay(teacher.getBirthDay())
                .birthPlace(teacher.getBirthPlace())
                .phoneNumber(teacher.getPhoneNumber())
                .isAdvisor(teacher.isAdvisorTeacher())
                .userRole(userRoleService.getUserRole(RoleType.TEACHER))
                .gender(teacher.getGender())
                .email(teacher.getEmail())
                .build();
    }

}

















