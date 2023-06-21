package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.*;
import com.schoolmanagement.entity.enums.Note;
import com.schoolmanagement.exception.ConflictException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.StudentInfoRequestWithoutTeacherId;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.StudentInfoResponse;
import com.schoolmanagement.payload.response.StudentResponse;
import com.schoolmanagement.repository.StudentInfoRepository;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentInfoService {
    private final StudentInfoRepository studentInfoRepository;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final LessonService lessonService;
    private final EducationTermService educationTermService;

    @Value("${midterm.exam.impact.percentage}") // $ suraya git manasinda
    private Double midtermExamPercentage;

    @Value("${final.exam.impact.percentage}")
    private Double finalExamPercentage;


    // Not: save()****************************************************************
    public ResponseMessage<StudentInfoResponse> save(String username, StudentInfoRequestWithoutTeacherId studentInfoRequest) {

        // !!! DTO ve request den gelen Student,Teacher,Lesson ve EducationTerm getiriliyor
        Student student = studentService.getStudentByIdForResponse(studentInfoRequest.getStudentId());//
        // studentService class ina gidecegiz eger bu class da Pojo bir Student objesi donduren method varsa bu methoda
        // studentInfoRequest classindan gelen studentId sini parametre olarak verip Student turundeki student variable
        // inin icine setleyecegiz... !!!! Peki boyle bir id numarasi yoksa burada bir exception almazmaz miyiz.
        // studentService katmanindaki getStudentByIdForResponse() methodunda bunun kontrolu yapilmis ve bir Exception
        // handle edilmis bundan dolayi burada herhangi bir exception almayiz. diger methodda boyle bir exception handle
        // edilmemis olsaydi bu handle islemini burada yapacaktik


        Teacher teacher = teacherService.getTeacherByUsername(username);// TeacherService katmaninda
        // getTeacherByUsername() methodunu olusturduk. httpServletRequest den aldigimiz username i bu methodun
        // parametresine vererek bu username e ait teacher objesini bulup getirecek ve Teacher data turundeki teacher
        // degiskeninin icine koyacak. Eger username ye ait bir teacher objesi yoksa exception u TeacherService
        // katmaninda bu methodun icinde handle edilmisti


        Lesson lesson = lessonService.getLessonById(studentInfoRequest.getLessonId());//LessonService Katmaninda
        // getLessonById() methodunu olusturduk. Bu method parametresine StudentInfoRequestWithoutTeacherId request
        // classindan gelen lessonId sinin karsiligi olan DB deki lesson u bulup Lesson data turundeki lesson
        // degiskeninin icine koyacak. Dursun bir kenarda lazim olur belki :)) . ya yoksa durumu LessonService
        // katmaninda methodun icinde handle edildi


        EducationTerm educationTerm = educationTermService.getById(studentInfoRequest.getEducationTermId());//bir
        // usttekinin aynisi iste. Bana burada uzun uzun yazdirma sikildim zaten. isimizi goren getById() methodu zaten
        // onceden yazmisiz educationTermService Katmanina gidip yeni bir method olusturmak zorunda kalmadik. bu methodu
        // kullanip gectik

        // !!! lesson cakisma varmi kontrolu
        // Bu scopenin disinda bir yardimci method olusturuyoruz. isminide sey koyalim ne olsssun hah checkSameLesson
        // olsun
        if (checkSameLesson(studentInfoRequest.getStudentId(), lesson.getLessonName())) {
            throw new ConflictException(String.format(Messages.ALREADY_REGISTER_LESSON_MESSAGE, lesson.getLessonName()));
        }

        // !!! Ders notu ortalamasi aliniyor
        Double noteAverage = calculateExamAverage(studentInfoRequest.getMidtermExam(),
                studentInfoRequest.getFinalExam());

        // !!! Ders notu Alfabetik olarak hesaplaniyor
        Note note =  checkLetterGrade(noteAverage);// Enum daki Note data turunde note objesi almamiz lazim. bunun icin
        // once asagida sayisal notu harf notuna ceviren checkLetterGrade adinda bir method olusturuyoruz. Bu method
        // noteAverage ortalama sayisal sinav notunu harf sistemli ders notuna cevirecek

        // !!! DTO -> POJO
        StudentInfo studentInfo =  createDto(studentInfoRequest, note, noteAverage) ; // Asagida olusturdugumuz method
        // uzerinden studentInfoRequest, note, noteAverage  datalarinin DTO-->POJO donusumunu yapip StudentInfo data
        // turundeki studentInfi variable inin icine setliyoruz

        // !!! DTO da olmayan fieldlar setleniyor
        // StudentInfo entity calssinda studentInfo tablosunun Student, Teacher, Lesson,EducationTerm gibi fieldlari da var
        // bunlari da DB ye kayit etmeliyiz. Yukarida requestten gelen id ler ile Student, Lesson, EducationTerm
        // objelerini HttpServletRequest den de username attribute methodu ilede username uzerinden Teacherleri getirip
        // kendi degiskenlerine setlemistik o degiskenleri burada studentInfo ya mapliyoruz.
        studentInfo.setStudent(student);//studentInfo degiskenine Student Turunde bir student degiskenini setliyoruz
        studentInfo.setEducationTerm(educationTerm);//studentInfo degiskenine EducationTerm Turunde bir educationTerm
        // degiskenini mapliyoruz
        studentInfo.setTeacher(teacher);//studentInfo degiskenine Teacher Turunde bir teacher degiskenini setliyoruz
        studentInfo.setLesson(lesson);//studentInfo degiskenine Lesson Turunde bir lesson degiskenini mapliyoruz.

        StudentInfo savedStudentInfo = studentInfoRepository.save(studentInfo);//StudentInfoRepository e git yukarida
        // butun fieldlarini setledigimiz studentInfo degiskenini kaydet ve save() methodunun dondurdugu studentInfo
        // Pojo objesini StudentInfo data turundeki savedStudentInfo variable sine maple.

        //!!! Response objesi olusturuluyor
        return ResponseMessage.<StudentInfoResponse>builder()
                .message("Student Info Saved Successfully")
                .httpStatus(HttpStatus.CREATED)
                .object(createResponse(savedStudentInfo)) // Asagida POJO-DTO donusumunu yapan createResponse
                // methodunu yaziyoruz
                .build();


    }


    //Student in StudentInfo field indaki tum studentInfo lari getirmemiz lazim
    private boolean checkSameLesson(Long studentId, String lessonName) {

        return studentInfoRepository.getAllByStudentId_Id(studentId) //Student in StudentInfo field indaki tum
                // studentInfo lari getirmemiz lazim (getAllByStudentId_Id) turetilmis methodunu olusturuyoruz.
                // Bu turetilmis method icin bir Query yazmamiza gerek yok studentInfo tablosundan StudentId fieldindan
                // parametrelede verilen id yi getir demis oluyoruz.
                .stream()
                // bir studentInfo akis olusturuyoruz
                .anyMatch((e) -> e.getLesson().getLessonName().equalsIgnoreCase(lessonName));
        //(e) --> yukaridan gelen butun datalari al burada bu StudentInfo oluyor
        // ->e(bu studentInfo larin)
        // .getLesson() bu method ile Lesson Objesi glecek
        // .getLessonName() gelen Lessonlarin Lesson isimlerini almamiz lazim
        // .equalsIgnoreCase(lessonName) lesson dan gelen lessonName ile Bu methodun parametresinden gelen
        // lessonName leri buyuk kucuk harf duyarliligi olmadan karsilastir. eslesme varsa save methodunun icinde
        // exception firlatacagiz
    }


    private Double calculateExamAverage(Double midtermExam, Double finalExam){

        // resource package sinin altinda application.properties file ina midtermExam ve finalExam in ortalamaya etki eden
        // katsayilarini belirliyoruz.

        // application.properties de asagidaki oranlari verdik
        // midterm.exam.impact.percentage = 0.40
        // final.exam.impact.percentage = 0.60

        // daha sonra bu class da bir degisken olusturmamiz lazim bu degiskenleri bu class in basinda olusturuyoruz.
        // @Value annatotionu ile degerleri application.properties ten almasi gerektigini soyluyoruz

        return ((midtermExam* midtermExamPercentage) + (finalExam*finalExamPercentage));
        // Mesela eger ders zorunlu ise ortalamaya etki eden degerler %50 %50 olsun dersersek bunu bir if methodu
        // yazarak yapabiliriz application.properties den gelen kendimiz olusturdugumuz percentage default degerlerine
        // etki edip method bazinda degistirmis oluruz.
    }

    private Note checkLetterGrade(Double average){

        if(average<50.0) {
            return Note.FF;
        } else if (average>=50.0 && average<55) {
            return Note.DD;
        } else if (average>=55.0 && average<60) {
            return Note.DC;
        } else if (average>=60.0 && average<65) {
            return Note.CC;
        } else if (average>=65.0 && average<70) {
            return Note.CB;
        } else if (average>=70.0 && average<75) {
            return Note.BB;
        } else if (average>=75.0 && average<80) {
            return Note.BA;
        } else {
            return Note.AA;
        }
    }


    //DTO-POJO donusumunu yapan yardimci method
    private StudentInfo createDto(StudentInfoRequestWithoutTeacherId studentInfoRequest,//DTO nun kendisini
                                  Note note,// Request de olmayan Note u setliyoruz
                                  Double average // Yine requestten gelmeyen note avarage ini hesaplayip DB ye
                                  // kayit edecegiz
    ){
        return StudentInfo.builder()
                .infoNote(studentInfoRequest.getInfoNote())//infoNote u request classindan aliyoruz
                .absentee(studentInfoRequest.getAbsentee())//absentee yi request classindan aliyoruz
                .midtermExam(studentInfoRequest.getMidtermExam())//MidtermExam i request classindan aliyoruz
                .finalExam(studentInfoRequest.getFinalExam())//FinalExam i request classindan aliyoruz
                .examAverage(average)//method parametresine verdigimiz avarage degiskenin den getiriyoruz.
                .letterGrade(note)//method parametresine verdigimiz note degiskeninden den getiriyoruz.
                .build();
    }

    private StudentInfoResponse createResponse(StudentInfo studentInfo){
        return StudentInfoResponse.builder()
                .lessonName(studentInfo.getLesson().getLessonName())//StudentInfo Entity Classindan Lesson Entity
                // Classinin LessoName Fieldini getiriyoruz. gelen datayi StudentInfoResponse classinin lessonName
                // degiskeninine mapliyoruz
                .creditScore(studentInfo.getLesson().getCreditScore())
                .isCompulsory(studentInfo.getLesson().getIsCompulsory())
                .educationTerm(studentInfo.getEducationTerm().getTerm())
                .id(studentInfo.getId())//StudentInfo Pojosundan gelen Id yi StudentInfoResponse turundeki
                // id ye setliyoruz
                .absentee(studentInfo.getAbsentee())
                .midtermExam(studentInfo.getMidtermExam())
                .finalExam(studentInfo.getFinalExam())
                .infoNote(studentInfo.getInfoNote())
                .Note(studentInfo.getLetterGrade())
                .average(studentInfo.getExamAverage())
                .studentResponse(createStudentResponse(studentInfo.getStudent()))//Student Pojo objesini Response(DTO)
                // ya ceviren bir methodu asagida yaziyoruz. Coklu field iceren Objelerde bu pojo objeyi farkli bir
                // method da response ye cevirip bu methodu burada kullaniyoruz(createStudentResponse)
                .build();

    }

    public StudentResponse createStudentResponse(Student student) {

        return StudentResponse.builder()
                .userId(student.getId())
                .username(student.getUsername())
                .surname(student.getSurname())
                .name(student.getName())
                .birthDay(student.getBirthDay())
                .birthPlace(student.getBirthPlace())
                .phoneNumber(student.getPhoneNumber())
                .gender(student.getGender())
                .email(student.getEmail())
                .motherName(student.getMotherName())
                .fatherName(student.getFatherName())
                .studentNumber(student.getStudentNumber())
                .isActive(student.isActive())
                .build();
    }


    // Not: delete()****************************************************************
    public ResponseMessage<?> deleteStudentInfo(Long studentInfoId) {

        if(!studentInfoRepository.existsByIdEquals(studentInfoId)) {
            throw new ResourceNotFoundException(String.format(Messages.STUDENT_INFO_NOT_FOUND, studentInfoId));
        }

        studentInfoRepository.deleteById(studentInfoId);

        return ResponseMessage.builder()
                .message("Student Info deleted successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

// hocam bu sabah dan itibaren mesela yukarida yazdig8im methodu otomatik olarak studentInfoRepository de create
// edebilmesi lazim d ama simdi yapmiyor. sanirim birde noktalama isaretlerine karsi bir duyarliligi gelisti kabiul
// etmiyor gecersiz sembol diyor

}