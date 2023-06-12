package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.entity.concretes.Teacher;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.payload.dto.TeacherRequestDto;
import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.abstracts.TeacherResponse;
import com.schoolmanagement.repository.TeacherRepository;
import com.schoolmanagement.utils.FieldControl;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

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

        if (lessons.size() == 0){// Set yapidaki listenin size eger 0 ise yani bos gelmisse if in icine girecek.
            // utils packasine gidip Message Classinin icinde once firlatacagimiz mesagenin icerigini uretiyoruz.
            throw  new BadRequestException(Messages.LESSON_PROGRAM_NOT_FOUND_MESSAGE);
        }else {// eger gelen set yapisindaki list dolu ise Duplacate kontrolu yapilacak

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

        private TeacherResponse createTeacherResponse(Teacher teacher){
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
}
