package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.abstracts.TeacherResponse;
import com.schoolmanagement.repository.TeacherRepository;
import com.schoolmanagement.utils.FieldControl;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
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
    //private final TeacherRequestDto teacherRequestDto;
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

        }



    }
}
