package com.schoolmanagement.service;


import com.schoolmanagement.entity.concretes.AdvisorTeacher;
import com.schoolmanagement.entity.concretes.Meet;
import com.schoolmanagement.entity.concretes.Student;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.exception.ConflictException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.MeetRequestWithoutId;
import com.schoolmanagement.payload.request.UpdateMeetRequest;
import com.schoolmanagement.payload.response.MeetResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.repository.MeetRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.utils.Messages;
import com.schoolmanagement.utils.TimeControl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MeetService {
    private final MeetRepository meetRepository;
    private final AdvisorTeacherService advisorTeacherService;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    // Not :  Save() *************************************************************************
    public ResponseMessage<MeetResponse> save(String username, MeetRequestWithoutId meetRequest) {

        // vadvisorTeacher degiskeni uzerinden toplanti yapilacak
        AdvisorTeacher advisorTeacher = advisorTeacherService.getAdvisorTeacherByUsername(username).orElseThrow(()->
                new ResourceNotFoundException(String.format(Messages.NOT_FOUND_ADVISOR_MESSAGE_WITH_USERNAME, username)));

        // !!! toplanti saat kontrolu. Bu kontrol boolean bir deger donduruyor. check methodunu sagladigi deger true
        // olursa exception firlat. start time stop time dan sonra ayarlandi ise check methodu true bir deger dondurecek
        if(TimeControl.check(meetRequest.getStartTime(), meetRequest.getStopTime()))
            throw new BadRequestException(Messages.TIME_NOT_VALID_MESSAGE);

        // !!! toplantiya katilacak ogrenciler icin yeni meeting saatlerinde cakisma var mi kontrolu
        for (Long studentId : meetRequest.getStudentIds()) {
            boolean check =    studentService.existsById(studentId); // requesttten gelen studentId li ogrenci DB de
            // var mi? varsa existsById() methodu ile kontrol et. bu method boolean bir deger donduruyor donen degeri
            // boolean data turundeki check degiskenin icine koy

            //existsById() methodu Student i bulamaz ise false deger dondurecek. DB de bir student olmama durumunda
            // exception firlatilacak. if methodunun icine girebilmesi icin eger Student yoksa gelen false degerini (!)
            // ile true ya cekiyoruz ki if emethodunun icine girip  Studentin bulunamadigina dair bir exception firlatilsin
            if(!check) throw  new ResourceNotFoundException(String.format(Messages.NOT_FOUND_USER2_MESSAGE, studentId));

            // mevcut meet in yeni meet ile tarih ve saat lerinin cakisma durumlarini kontrol eden bir methodu  save
            // scope sinin disinda olusturup burda cagirdik ve parametrelerini verdik.
            checkMeetConflict(studentId, meetRequest.getDate(),meetRequest.getStartTime(), meetRequest.getStopTime());
        }

        // !!! Meet e katilacak olan Student lar getiriliyor
        List<Student> students = studentService.getStudentByIds(meetRequest.getStudentIds());

        // !!! Meet nesnesi olusturup ilgili fieldlar setleniyor
        Meet meet = new Meet();// Meet turunde yeni bir meet objesi olusturuldu
        meet.setDate(meetRequest.getDate());
        meet.setStartTime(meetRequest.getStartTime());
        meet.setStopTime(meetRequest.getStopTime());
        meet.setStudentList(students);
        meet.setDescription(meetRequest.getDescription());
        meet.setAdvisorTeacher(advisorTeacher);

        //!!! save islemi
        Meet savedMeet = meetRepository.save(meet);

        //!!! Response nesnesi olusturuluyor

        return ResponseMessage.<MeetResponse>builder()
                .message("Meet Saved Successfully")
                .httpStatus(HttpStatus.CREATED)
                .object(createMeetResponse(savedMeet))// yeni POJO meet i  response a ceviren createMeetResponse()
                // methodu asagida yaziyoruz
                .build();
    }

    private void checkMeetConflict(Long studentId, LocalDate date, LocalTime startTime, LocalTime stopTime){

        // requestten gelen studentId li Studente ait bir meet varmi kontrolu
        // Bir Studentin birden fazla meet i olabilir bu nedenle list yapi kurduk
        List<Meet> meets = meetRepository.findByStudentList_IdEquals(studentId);
        // TODO : meet size kontrol edilecek

        // for Ich yapisi ile meetRepository den gelen meets list yapisinin icindeki startTime ve StopTime ile
        // reguestten gelen startTime ve StopTime aeasinda bir conflict varmi kontrolu
        for(Meet meet : meets){

            LocalTime existingStartTime =  meet.getStartTime();// StudentId ile Studentlere ait meetRepository den
            // getirdigimiz meet objelerini barindiran meets degiskeninin icinden Meet trundeki meet ile cektigimiz
            // objenin getStartTime() ini alip LocalTime turundeki existingStartTime ismini verdigimiz variable nin
            // icinde koyuyoruz. daha sonra bunlari yeni meet in startTime ile karsilastiracagiz.

            LocalTime existingStopTime =  meet.getStopTime();

            // if methodunun icinde kontroller yapilacak
            if(meet.getDate().equals(date) // DB den gelen Date ile checkMeetConflict methodunun parametresine
                    // verdigimiz yeni meet in Date leri cakisiyor mu?
                    && // and operatoru ile date kontrolu ve bundan sonra gelecek kontrollerden biri bile hata verirse
                    // exception firlatilacak
                    ((startTime.isAfter(existingStartTime) && startTime.isBefore(existingStopTime)) ||
                            // yeni gelen meetingin startTime bilgisi mevcut mettinglerden herhangi birinin startTim,e
                            // ve stopTime arasinda mi ???
                            (stopTime.isAfter(existingStartTime) && stopTime.isBefore(existingStopTime)) ||
                            //  yeni gelen meetingin stopTime bilgisi mevcut mettinglerden herhangi birinin startTim,e
                            //  ve stopTime arasinda mi ???
                            (startTime.isBefore(existingStartTime) && stopTime.isAfter(existingStopTime)) ||
                            //  yeni gelen meetingin startTime ve stopTime bilgisi mevcut mettinglerden herhangi birinin
                            //  startTim,e ve stopTime disinda mi ??? yani mevcut start ve stop Time yeni star ve Stop
                            //  Time in arasinda mi kaliyor?
                            (startTime.equals(existingStartTime) && stopTime.equals(existingStopTime)))){
                            // Yeni gelen start ve stop time lar mevcut start ve stop time lar ile cakisiyor mu?

                // throw new ConflictException(Messages.MEET_EXIST_MESSAGE);
                throw new ConflictException("HATAAAAA");
            }


        }

    }
    private MeetResponse createMeetResponse(Meet meet) {
        return MeetResponse.builder()
                .id(meet.getId())
                .date(meet.getDate())
                .startTime(meet.getStartTime())
                .stopTime(meet.getStopTime())
                .description((meet.getDescription()))
                .advisorTeacherId(meet.getAdvisorTeacher().getId())
                .teacherSsn(meet.getAdvisorTeacher().getTeacher().getSsn())
                .teacherName(meet.getAdvisorTeacher().getTeacher().getName())
                .students(meet.getStudentList())
                .build();
    }

    // Not : getAll() *************************************************************************
    public List<MeetResponse> getAll() {

        return meetRepository.findAll()
                .stream()
                .map(this::createMeetResponse)
                .collect(Collectors.toList());
    }


    // Not :  getMeetById() ********************************************************************
    public ResponseMessage<MeetResponse> getMeetById(Long meetId) {

        Meet meet = meetRepository.findById(meetId).orElseThrow(()->
                new ResourceNotFoundException(String.format(Messages.MEET_NOT_FOUND_MESSAGE,meetId)));

        return ResponseMessage.<MeetResponse>builder()
                .message("Meet Successfully found")
                .httpStatus(HttpStatus.OK)
                .object(createMeetResponse(meet))
                .build();
    }

    // Not : getAllMeetByAdvisorAsPage() **************************************************
    public Page<MeetResponse> getAllMeetByAdvisorTeacherAsPage(String username, Pageable pageable) {
        AdvisorTeacher advisorTeacher = advisorTeacherService.getAdvisorTeacherByUsername(username).orElseThrow(()->
                new ResourceNotFoundException(String.format(Messages.NOT_FOUND_ADVISOR_MESSAGE_WITH_USERNAME,username)));

        return meetRepository.findByAdvisorTeacher_IdEquals(advisorTeacher.getId(), pageable) // advisorTeacher.getMeet()
                .map(this::createMeetResponse);
    }

    // Not :  getAllMeetByAdvisorTeacherAsList() *********************************************
    public List<MeetResponse> getAllMeetByAdvisorTeacherAsList(String username) {
        AdvisorTeacher advisorTeacher = advisorTeacherService.getAdvisorTeacherByUsername(username).orElseThrow(()->
                new ResourceNotFoundException(String.format(Messages.NOT_FOUND_ADVISOR_MESSAGE_WITH_USERNAME,username)));
     return meetRepository.getByAdvisorTeacher_IdEquals(advisorTeacher.getId())
                    .stream()
                    .map(this::createMeetResponse)
                    .collect(Collectors.toList());


    }

    // Not :  delete() ***********************************************************************
    public ResponseMessage<?> delete(Long meetId) {
       Meet meet = meetRepository.findById(meetId).orElseThrow(()->
                new ResourceNotFoundException(String.format(Messages.MEET_NOT_FOUND_MESSAGE, meetId)));

        meetRepository.deleteById(meetId);
        return ResponseMessage.builder()
                .message("Meet Deleted Successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }


    // Not :  update() ***********************************************************************
    public ResponseMessage<MeetResponse> update(UpdateMeetRequest meetRequest, Long meetId) {
        // !!!  ODEV : save-update kontrol kisimlari ortak method uzerinden cagirilacak
        Meet getMeet = meetRepository.findById(meetId).orElseThrow(()->
                new ResourceNotFoundException(String.format(Messages.MEET_NOT_FOUND_MESSAGE, meetId)));


        // !!! Time Control
        if (TimeControl.check(meetRequest.getStartTime(),meetRequest.getStopTime())) {
            throw new BadRequestException(Messages.TIME_NOT_VALID_MESSAGE);
        }

        // !!! her ogrenci icin meet conflict kontrolu
        // !!! if in icinde request den gelen meet ile orjinal meet objesinde date,startTime ve stoptime
        // bilgilerinde degisiklik yapildiysa checkMeetConflict metoduna girmesi saglaniyor
        if(!(getMeet.getDate().equals(meetRequest.getDate()) &&
                getMeet.getStartTime().equals(meetRequest.getStartTime()) &&
                getMeet.getStopTime().equals(meetRequest.getStopTime())) ){
            for (Long studentId : meetRequest.getStudentIds()) {
                checkMeetConflict(studentId,meetRequest.getDate(),meetRequest.getStartTime(),meetRequest.getStopTime());
            }
        }

        // TODO request'ten gelen id'lere ait ogrenci var mi kontrolu
        List<Student> students = studentService.getStudentByIds(meetRequest.getStudentIds());
        //!!! DTO--> POJO
        Meet meet = createUpdatedMeet(meetRequest,meetId);
        meet.setStudentList(students);
        meet.setAdvisorTeacher(getMeet.getAdvisorTeacher());

        Meet updatedMeet = meetRepository.save(meet);

        return ResponseMessage.<MeetResponse>builder()
                .message("Meet Updated Successfully")
                .httpStatus(HttpStatus.OK)
                .object(createMeetResponse(updatedMeet))
                .build();

    }

    private Meet createUpdatedMeet(UpdateMeetRequest updateMeetRequest, Long id){
        return Meet.builder()
                .id(id)
                .startTime(updateMeetRequest.getStartTime())
                .stopTime(updateMeetRequest.getStopTime())
                .date(updateMeetRequest.getDate())
                .description(updateMeetRequest.getDescription())
                .build();
    }

    // Not :  getAllMeetByStudent() **********************************************************
    public List<MeetResponse> getAllMeetByStudentByUsername(String username) {
        Student student = studentService.getStudentByUsernameForOptional(username).orElseThrow(()->
                new ResourceNotFoundException((Messages.NOT_FOUND_USER_MESSAGE)));

        return meetRepository.findByStudentList_IdEquals(student.getId())
                .stream()
                .map(this::createMeetResponse)
                .collect(Collectors.toList());
    }

    // Not :  getAllWithPage() **********************************************************
    public Page<MeetResponse> search(int page, int size) {

        Pageable pageable = PageRequest.of(page,size, Sort.by("id").descending());
        return meetRepository.findAll(pageable).map(this::createMeetResponse);
    }
}
