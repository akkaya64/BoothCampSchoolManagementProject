package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.AdvisorTeacher;
import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.entity.concretes.Student;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.ChooseLessonProgramWithId;
import com.schoolmanagement.payload.request.StudentRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.StudentResponse;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.utils.CheckSameLessonProgram;
import com.schoolmanagement.utils.FieldControl;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final AdvisorTeacherService advisorTeacherService;
    private final FieldControl fieldControl;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final LessonProgramService lessonProgramService;

    // Not: Save() **********************************************************
    public ResponseMessage<StudentResponse> save(StudentRequest studentRequest) {

        // Kontrol-1 StudentRequest den gelen baska bir tablodan getirilen bir data var (private Long advisorTeacherId;)
        // boyle bir data varmi kontrol etmeliyiz, varsa getir yoksa exception firlat
        // !!! AdvisorTeacher kontrolu
        // Bir service clasinda logical islem yaparken baska bir roll un depositorysine direk gitmemiz ileride
        // sorunlara neden olabilir bu nedenle o roll un kendi service classi izerinden repository sine erismeliyiz.
        AdvisorTeacher advisorTeacher = advisorTeacherService.getAdvisorTeacherById(studentRequest.getAdvisorTeacherId())
                // AdvisorTeacherService Classinda getAdvisorTeacherById methodu olusturuldu
                .orElseThrow(() -> new ResourceNotFoundException
                        (String.format(Messages.NOT_FOUND_ADVISOR_MESSAGE, studentRequest.getAdvisorTeacherId())));
        // String format ile yazildi

        // Kontrol-2
        // !!! Dublicate kontrolu
        // Bu bir update methodu bu nedenle duplicate kontrolu yapilmali
        fieldControl.checkDuplicate(studentRequest.getUsername(), studentRequest.getSsn()
                , studentRequest.getPhoneNumber(), studentRequest.getEmail());

        //Bu kontrollerden gectiyse artik DTO yu POJO ya donusturmeliyiz.

        //!!! Student DTO -> POJO
        Student student = studentRequestToDto(studentRequest);
        // !!! student nesnesindeki eksik datalari setliyoruz
        student.setStudentNumber(lastNumber());//Ogrenci icin ogrenci numarasi olusturan bir method yaziyoruz
        student.setAdvisorTeacher(advisorTeacher);
        student.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
        student.setActive(true); // Zaten yeni kayit oldugu icin actifligini default olarak true ya cekiyoruz
        student.setPassword(passwordEncoder.encode(studentRequest.getPassword()));

        // !!! Response Nesnesi olusturuluyor.
        // Save leme islemini POJO->DTO donusumunu yapan createStudentResponse() methodunun parametresinde yapiyoruz.
        return ResponseMessage.<StudentResponse>builder()
                // yukarida artik tum fieldlari ile olusmus olan entity(POJO) student objesini spring Spring Data nin
                // hazir CRUD operasyonlarindan save() methoduna parametre olarak vererek Repository e
                // kayit ediyoruz. Kaydederken save() methodu bir pojo donduruyordu  Save islemi gerceklestikten sonra
                // donen bu student pojo sunu da Pojo yu DTO donusumunu yapan createStudentResponse() methoduna
                // parametre olarak vermis oluyoruz. yani tam anlami ile bir tek tas ile iki elma dusurmek gibi.
                .object(createStudentResponse(studentRepository.save(student)))
                //Pojo yu DTO ya donduren createStudentResponse methodunu asagida yazdik.
                .message("Student saved Successfully")
                .build();
    }

    //DTO yu Pojo ya ceviren yardimci method
    private Student studentRequestToDto(StudentRequest studentRequest) {
        return Student.builder()
                .fatherName(studentRequest.getFatherName())
                .motherName(studentRequest.getMotherName())
                .birthDay(studentRequest.getBirthDay())
                .birthPlace(studentRequest.getBirthPlace())
                .name(studentRequest.getName())
                .surname(studentRequest.getSurname())
                .password(studentRequest.getPassword())
                .username(studentRequest.getUsername())
                .ssn(studentRequest.getSsn())
                .email(studentRequest.getEmail())
                .phoneNumber(studentRequest.getPhoneNumber())
                .gender(studentRequest.getGender())
                .build();
    }

    public int lastNumber() {// Bu method son Student objesi icin onceki uretilen numaralardan sonraki last
        // StudentNumber ini uretecek.
        if (!studentRepository.findStudent()) {// Bu if methodu studentRepository de ogrenci varsa degil yoksa calismali
            // ve studentRepository de olusacak olan ilk sansli ogrenci icin 1000 numarasini dondurmeli bu nedenle if in
            // parametresini (!) tersliyoruz yani Student yoksa gelen deger false olacak ve biz bu boolean degeri
            // tersledigimiz icin false olan deger true ya donecek ve if in icine girip ilk ogrencinin numarasi olan
            // 1000 numarasini dondurecek. Eger bir kere bile DB deki ogrenci tablosuna bir Student create edildilmis
            // ise studentRepository de ogrenci var mi kontrolu bize boolean bir true dondurecek ama if in
            // parametresinide bu degeri tersledigimiz icin artik gelen true degeri false olacagi icin if in icine
            // girmeyecek ve 1000 sayisi artik dondurulemeyecek. Burada sisteme kayit edilen ilk Student in
            // numarasi uretilecek
            return 1000;
        }

        // ilk kayit edilen Studentten sonraki her student icin bir Ogrenci numarasi uretmeliyiz bununicin asagida
        // yazdigimiz returna verdigimiz parametrede kullanilmak uzere DB deki student tablosunun studentNumber
        // fieldindaki en buyuk sayiyi getiren bir method olusturacagiz ve bu gelen MAX buyuklukteki sayiya +1 ekleyip
        // yeni olusan ogrencinin studentNumber i olarak setlemek uzere dondurecegiz.
        return studentRepository.getMaxStudentNumber() + 1;

    }

    // Student Pojo sunu StudentResponse e ceviren method... Kullaniciya responseMessage donduruken kullaniciya
    // gonderecegimiz StudentResponse objesini olusturuyoruz..
    public StudentResponse createStudentResponse(Student student) {
        return StudentResponse.builder()
                .userId(student.getId())
                .username(student.getUsername())
                .name(student.getName())
                .surname(student.getSurname())
                .birthDay(student.getBirthDay())
                .birthPlace(student.getBirthPlace())
                .phoneNumber(student.getPhoneNumber())
                .gender(student.getGender())
                .email(student.getEmail())
                .fatherName(student.getFatherName())
                .motherName(student.getMotherName())
                .studentNumber(student.getStudentNumber())
                .isActive(student.isActive())
                .build();
    }

    // Not: changeActiveStatus() *********************************************
    public ResponseMessage<?> changeStatus(Long id, boolean status) {
        // Kontrol-1 requestten gelen id li student repository de var mi?
        // !!! id kontrolu
        Student student = studentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));
        // eger requestten gelen id nin student objesini DB de bulduysa bu gelen student object ini Student data
        // turundeki student konteynirina koyuyoruz

        student.setActive(status); //student objesinin setActive() methoduna student in requestten yeni gelen, boolean
        //  true yada false bir deger olarak donen status u setlenmesi icin parametre olarak veriyoruz.

        studentRepository.save(student); // aktive lik durumu guncellenmis olan studenti DB ye tekrar gonderiyoruz.

        return ResponseMessage.builder()// yapilan islemin sonucunu kullaniciya donduruyoruz
                .message("Student is " + (status ? "active" : "passive")) // status un durumuna bak (?) eger true ise
                // active yaz eger degilse (:) passive yaz. Kullanici eger donen sonuc true ise su mesaji gorecek
                // Student is active eger degilse de Student is passive message sini gorecek
                .httpStatus(HttpStatus.OK)
                .build();

    }

    // Not: getAllStudent() *******************************************************
    public List<StudentResponse> getAllStudent() {
            return studentRepository.findAll()
                    .stream()
                    .map(this::createStudentResponse)
                    .collect(Collectors.toList());

    }

    // Not: updateStudent() ******************************************************
    public ResponseMessage<StudentResponse> updateStudent(Long userId, StudentRequest studentRequest) {

        // Kontrol-1
        // !!! Student var mi kontrolu
        // Request den gelen id nin karsiligi olana bir ogrenci var mi?
        // Eger varsa Student turunde bir student variable ine map liyoruz...
        Student student = studentRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));

        // Kontrol-2
        // !!! AdvTeacher kontrolu
        // Ogrenci kayit edilirken ogrenciye atanmis AdvisorTeacher var mi?
        // Eger varsa AdvisorTeacher turundeki advisorTeacher variable ina set liyoruz...
        AdvisorTeacher advisorTeacher = advisorTeacherService.getAdvisorTeacherById(studentRequest.getAdvisorTeacherId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(Messages.NOT_FOUND_ADVISOR_MESSAGE,
                                studentRequest.getAdvisorTeacherId())));

        // Dublicate Kontrolu
        fieldControl.checkDuplicate(studentRequest.getUsername(), studentRequest.getSsn(), studentRequest
                .getPhoneNumber(), studentRequest.getEmail());

        // !!! DTO -> POJO
        Student updatedStudent = createUpdatedStudent(studentRequest, userId);
        updatedStudent.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
        updatedStudent.setAdvisorTeacher(advisorTeacher);
        updatedStudent.setStudentNumber(student.getStudentNumber());
        updatedStudent.setActive(true);

        studentRepository.save(updatedStudent);

        return ResponseMessage.<StudentResponse>builder()
                .object(createStudentResponse(updatedStudent))
                .message("Student updated Successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private Student createUpdatedStudent(StudentRequest studentRequest, Long userId) {
        return Student.builder()
                .id(userId)
                .fatherName(studentRequest.getFatherName())
                .motherName(studentRequest.getMotherName())
                .birthDay(studentRequest.getBirthDay())
                .birthPlace(studentRequest.getBirthPlace())
                .name(studentRequest.getName())
                .surname(studentRequest.getSurname())
                .password(studentRequest.getPassword())
                .username(studentRequest.getUsername())
                .ssn(studentRequest.getSsn())
                .email(studentRequest.getEmail())
                .phoneNumber(studentRequest.getPhoneNumber())
                .gender(studentRequest.getGender())
                .userRole(userRoleService.getUserRole(RoleType.STUDENT))
                .build();

    }

    // Not: deleteStudent() ******************************************************
    public ResponseMessage<?> deleteStudent(Long studentId) {
        // !!! id var mi kontrolu
        Student student = studentRepository.findById(studentId).orElseThrow(()->
                new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));

        studentRepository.deleteById(studentId);

        return ResponseMessage.builder()
                .message("Student Deleted Successfully ")
                .httpStatus(HttpStatus.OK)
                .build();
    }


    // Not: getStudentByName() ***************************************************
    public List<StudentResponse> getStudentByName(String studentName) {
        return studentRepository.getStudentByNameContaining(studentName)
                //TODO bu kismi tekrar dinle ve commetlerini yaz
                .stream()
                .map(this::createStudentResponse)
                .collect(Collectors.toList());
    }

    // Not: getStudentById() ******************************************************
    public Student getStudentByIdForResponse(Long id) {//Bu zamana kadar kullaniciya bir response dondurduk ama
        // bu method baska classlardan cagirilip kullanilacagi icin ve burada kullaniciya bir response objesi
        // dondurmeyecegimiz icin bu method da objeyi Pojo olarak donduruyoruz. Artik bir pojo dundurdugumuz
        // icin baska classlardan cagirip logical islemler icin kullanabilecegiz
        return studentRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));
    }

    // Not: getAllStudentWithPage() ***********************************************
    public Page<StudentResponse> search(int page, int size, String sort, String type) {

        // Pageable pageable = PageRequest.of(page, size, Sort.by(type,sort));

        // Yukaridaki code blogu assagidaki if li code blogu ile ayni isi yapiyor.

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        if (Objects.equals(type, "desc")) {
            pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        }

        return studentRepository.findAll(pageable).map(this::createStudentResponse);
        // studentRepository DB ile iliskili oldugu icin DB den gelen data lar POJO formatinda biz bunu lamda ile
        // DB den gelen bu datalari(this) createStudentResponse methodunu kullanarak response turune yani
        // DTO ya cevirecegiz
    }

    // Not: chooseLessonProgramById() *********************************************
    public ResponseMessage<StudentResponse> chooseLesson(String username,//chooseLesson methodundan bir Student
                                                         // username i geliyor ve secilen LessonProgramin id
                                                         // bilgileri geliyor.
                                                         ChooseLessonProgramWithId chooseLessonProgramRequest) {

        // !!! Student ve LessonProgram kontrolu
        // SpringFrameWork un methodlari turetilebilen methodlardi buradan yola cikarak SpringFrameWork findById()
        // methodunu tureterek StudentRepository de findByUsername() methodu olusturacagiz bu method
        // studentRepository e gidip requestBody den gelen username DB deki Student tablosunda var mi yok mu kontrol
        // edecek varsa username ile gelen student objesini Student data type indeki student degiskeninin icine
        // setleyecek eger yoksa orElseThrow ile exception firlatacagiz. Yapiyi null olmaktan kurtardik
        Student student = studentRepository.findByUsername(username).orElseThrow(()->
                new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));

        //!!! talep edilen lessonProgram getiriliyor
        // request den gelen LessonProgram lari icinde LessonProgramlar barindiran sadece benzersiz verileri icine kabul
        // eden Set yapidaki bir lessonPrograms variable ine setliyoruz-mapliyoruz. bunun icinde lessonProgramService
        // katinda id den yola cikarak pojo bir lessonProgram donduren method yamismiyiz diye kontrol ediyoruz ki
        // Allahtan boyle bir method yazmisiz yoksa response bir LessonProgram donduren bir methodu cagirip  oradan
        // gelen response datalari pojoya ceviren methodu bu classin icinde bir yerde yada LessonProgramService
        // classinin icinde bir yerde olusturracaktik ama zaten boyle bir method olusturulmus getAllLessonProgramById()
        // buraya cagirdigimiz .getAllLessonProgramById() methoduna isini yapabilmesi icin parametresine
        // chooseLessonProgramRequest den .getLessonProgramId() methodu ile gelen id yi veriyoruz.
        Set<LessonProgram> lessonPrograms = lessonProgramService.getAllLessonProgramById(chooseLessonProgramRequest
                .getLessonProgramId());

        // lessonPrograms in icine, eger DB de request den gelen id ye ait object varsa setlenmis olacak
        // bunun kontrolunu if yapisi ile kontrol ediyoruz eger yoksa lessonPrograms sin size 0 olacak
        // ve bir exception firlatilacak (Set yapilarin bir size i olur)
        if(lessonPrograms.size()==0){
            throw  new ResourceNotFoundException(Messages.LESSON_PROGRAM_NOT_FOUND_MESSAGE);
        }

        // !!! Ogrencinin mevcut lessonProgramini getiriyoruz
        // Student in halihazirda bir LessonProgrami da olabilir. bunu kontrol etmemiz lazim student tablosuna gidecegiz
        // buradaki lessonProgram lari getirecegiz. bunun icin de Student entity classindaki  getLessonsProgramList()
        // methodunu burada cagiriyoruz. gelen degerler entity class indan gelecegi icin POJO yapisinda olacak bu nedenle
        // gelen degerleri icinde sadece unique LessonProgram Set yapidaki studentLessonProgram variable inin icine koyuyouz
        Set<LessonProgram> studentLessonProgram = student.getLessonsProgramList();

        //!!! lesson icin dublicate kontrolu
        // Conflict varmi kontrol etmemiz lazim. Requestten gelen talep edilen LessonProgram ile DB de halihazirda var
        // olan LessonProgram lar arasinda ayni olanlar varmi kontrol edilecek. Utils package sinin icinde
        // CheckSameLessonProgram yardimci class i olusturmustuk, bu classinda LessonProgram larin duplicateligini
        // kontrol eden checkLessonPrograms() methodu vardi onu burda cagiriyoruz ve requestten gelen talep edilen
        // LessonProgramlar (lessonPrograms) ile DB de bulunan mevcud LessonProgramlari getirip icine koydugumuz
        // (studentLessonProgram) variable ini parametre olarak veriyoruz. Eger bir eslesme bulursa code yukaridan
        // asagiya dogru gelen code buradan itibaren devam etmeyecek  ve bu methodu tetikleyen Controller tarafina
        // bir exception gonderecek.
        CheckSameLessonProgram.checkLessonPrograms(studentLessonProgram,lessonPrograms);

        // Herhangi bir conflict yoksa talep edilen lessonPrograms larin hepsini icinde DB den gelen lessonProgram lari
        // barindiran studentLessonProgram variable ina Java nin util kutuphanesinde bulunan .addAll() methodu ile
        // ekliyoruz.
        studentLessonProgram.addAll(lessonPrograms);

        // artik icinde guncellenmis yada yeni olusmus lessonProgramlari barindiran studentLessonProgram variable ini,
        // DB den getirilen student leri iceren student variable inin LessonsProgramList fieldinin icine setliyoruz.
        student.setLessonsProgramList(studentLessonProgram);


        // Student savedStudent =  studentRepository.save(student);
        // Yukaridaki save methodu objenin kendisini donduruyor bu donen objeyi Student POJO su turundeki savedStudent
        // variable sinin icine koyabiliriz artik elimizde guncellenerek DB ye kayit edilmis bir student objesi ve
        // icinde DB deki bu guncellenmis objeyi barindiran savedStudent variable var. Kullaniciya yeni Objeyi
        // responseMessage ile birlikte gondermeliyiz. Bu nedenle POJO objesi olan savedStudent objesini
        // POJO->DTO(response) Donusumunu yapan methpda parametre olarak verebiliriz
        // .object(createStudentResponse(savedStudent)) baska bir yolda zaten yeni update edilmis olan ve
        // studentRepository ye bir (student) pojo su donduren SpringframeWorkun .save(student) methodunu
        // asagidaki gibi direkt koyabiulirim
        return ResponseMessage.<StudentResponse>builder()
                .message("Lessons added to Student")
                .object(createStudentResponse(studentRepository.save(student)))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    // Not : getAllStudentByAdvisorUsername() ********************************************
    public List<StudentResponse> getAllStudentByTeacher_Username(String username) {

        return studentRepository.getStudentByAdvisorTeacher_Username(username)
                .stream()
                .map(this::createStudentResponse)
                .collect(Collectors.toList());
    }

    public boolean existByUsername(String username) {
        return studentRepository.existsByUsername(username);
    }

    public boolean existById(Long studentId) {
        return studentRepository.existsById(studentId);
    }


    public boolean existsById(Long studentId) {
        return studentRepository.existsById(studentId);
    }


    public List<Student> getStudentByIds(Long[] studentIds) {
        return studentRepository.findByIdsEquals(studentIds);
    }
}
