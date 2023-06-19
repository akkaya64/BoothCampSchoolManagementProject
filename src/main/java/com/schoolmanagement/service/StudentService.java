package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.AdvisorTeacher;
import com.schoolmanagement.entity.concretes.Student;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.StudentRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.StudentResponse;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.utils.FieldControl;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final AdvisorTeacherService advisorTeacherService;
    private final FieldControl fieldControl;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

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
        public List<StudentResponse> getAllStudent () {
            return studentRepository.findAll()
                    .stream()
                    .map(this::createStudentResponse)
                    .collect(Collectors.toList());
        }
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

}
