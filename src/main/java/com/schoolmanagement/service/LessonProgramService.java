package com.schoolmanagement.service;
// LessonProgramService Classimiza hos geldiniz sizi burada gormek gercekten heyecan verici Intelji-Springfreamwork ve
// lombook birlikteligi sizi burada gormekten mutluluklar duyar. Sizi is ustunde gormeyi cok isteriz. hadi buyrun ise
// koyulun lutfen. Simdiden size kolayliklar gelsin.. Birazcik kafa yemeye hazir olun.... :)) ha ha haaa. Kusuk bi
// hatirlatma bu bir Service Katmani bunu Springe bildirmelisin dostum ise oradan basla.

import com.schoolmanagement.entity.concretes.EducationTerm;
import com.schoolmanagement.entity.concretes.Lesson;
import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.entity.concretes.Teacher;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.dto.LessonProgramDto;
import com.schoolmanagement.payload.request.LessonProgramRequest;
import com.schoolmanagement.payload.response.LessonProgramResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.TeacherResponse;
import com.schoolmanagement.repository.LessonProgramRepository;
import com.schoolmanagement.utils.Messages;
import com.schoolmanagement.utils.TimeControl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service // hey buraya bu annotationu koyman cok akillaca cok iyi gidiyosun bir isik var sende :) well done, keep going.
// bunun icinde aslinda @Component annotation da var. Yani bu Classtan bir instance olustugu anda default olarak
// singleton scope olarak olusacak ve uygulamanin herhangi bir yerine injection islemini yapabileceksin..
// tabiki sen bunu biliyordun ama yineden ben bir hatirlatayim dedim ileride lazim olur belki...
// Malum balik hafizalisin ya...

@RequiredArgsConstructor //
public class LessonProgramService {
    private final LessonProgramRepository lessonProgramRepository;// @RequiredArgsConstructor olmasa bu
    // injectionu buraya ekleyemezdin dostum istersen bir dene :) yat kalk Springframework e tesekkur et
    //Simdi Controller katmanini olusturmalisin

    private final LessonService lessonService;// Lessonlari getirecegiz
    private final EducationTermService educationTermService; //termleri getirrecegiz cunku LessonPrograma term
    // bilgisi de eklemeliyiz.
    private final LessonProgramDto lessonProgramDto;



    // Not :  Save() *************************************************************************
    public ResponseMessage<LessonProgramResponse> save(LessonProgramRequest request) {// Controller Clasda otamatik
        // olarak create edttiginde adi save methoduna verilen parametrenin adi defauld olarak lessonProgramRequest
        // geldi bunu codelar uzamasin diye ismi request olarak degistirildi.

        // !!! Lesson Programda olacak dersleri LessonService uzerinden getiriyorum
        Set<Lesson> lessons = lessonService.getLessonByLessonIdList(request.getLessonIdList());// DB ye kayit hazirligi
        // lessonService katmaninina git .getLessonByLessonIdList() methoduna verececegimiz id leri repositoryden getir
        // Simdi bu methoda kullanicidan Id List cinsinden bir request verimemiz gerekiyor request.getLessonIdList()
        // yani ihtiyac duydugun lesson listesini request.getLessonIdList() ile aldin. lessonService service nin
        // .getLessonByLessonIdList() methoduna verdin. bu da sana DB den ne getirecek? tabi ki lessons lar getirecek
        // peki bu lessonlar hangi turde olacak? Set yapida bir Lesson entity collectionlari verecek.

        // Lesson programinin icinde lesson lar var bunlari aldin afferin!!! LessonProgram in icine baska ne alman
        // lazim?? tabiki termleri de almalisin hadi bakalim codu yaz.... ama once term servicenin bagimliligini
        // eklmeyi unutma. bagimliligi class bazindaki hangi annotation ile olusturabiliyordun hatirliyor musun??



        // !!! EducationTerm id ile getiriliyor
        EducationTerm educationTerms = educationTermService.getById(request.getEducationTermId());// DB ye kayit hazirligi
        // educationTermService classindaki .getById methoduna .getEducationTermId() ile request den aldigin id yi
        // EducationTerm type indeki educationTerm adindaki POJO veriable sine ata...

        // !!! yukarda gelen lessons ici bos degilse zaman kontrolu yapiliyor :
        if (lessons.size() == 0 ){
            throw new ResourceNotFoundException(Messages.NOT_FOUND_LESSON_IN_LIST);
        } else if(TimeControl.check(request.getStartTime(), request.getStopTime())) {
            //Burada bir yenilik yapalim kendi Custom exceptionumuzu olusturalim
            throw new BadRequestException(Messages.TIME_NOT_VALID_MESSAGE);
        }
        // eger yukarida olusturulan requestler null gelmezse artik LessonProgramRequest den gelen zaman bilgilerini
        // kontrol edilmesi lazim, Kontrol islemlerini zaman kontrolu yapmak isteyen farkli class lar da da
        // kullanabilmek icin utuls packagesinin icinde bir method yazip burada cagiracagiz...

        // Oncelikle kullanicadan gelen bilgileri DTO ya ceviren lessonProgramRequestToDTO methodunu yaziyoruz...
        // daha sonra request den gelen Json formatindaki datalari asagida lessonProgram repository e gonderebilmek icin
        // Pojo turunde ki lessonProgram konteynirina atiyorum
        LessonProgram lessonProgram = lessonProgramRequestToDTO(request,lessons);

        // lessonProgram konteynirinda EducationTerm bilgisi yok bunu da eklememiz lazim.
        lessonProgram.setEducationTerm(educationTerms);

        // Artik hersey hazir simdi DB de kalici hale getirebiliriz
        LessonProgram sevedLessonProgram = lessonProgramRepository.save(lessonProgram);

        // ResponseMessage objesi donduruyoruz... once pojo-->DTO donusumunu yapacak
        // createLessonProgramResponseForSaveMethod adindaki methodu bu class icinde yaziyoruz
        return ResponseMessage.<LessonProgramResponse>builder()
                .message("Lesson Program is Created")
                .httpStatus(HttpStatus.CREATED)
                .object(createLessonProgramResponseForSaveMethod(sevedLessonProgram))
                .build();
    }

    private LessonProgram lessonProgramRequestToDTO(LessonProgramRequest lessonProgramRequest, Set<Lesson> lessons){
       return lessonProgramDto.dtoLessonProgram(lessonProgramRequest , lessons);
    }

    private LessonProgramResponse createLessonProgramResponseForSaveMethod(LessonProgram lessonProgram) {
        return LessonProgramResponse.builder()
                .day(lessonProgram.getDay())
                .startTime(lessonProgram.getStartTime())
                .stopTime(lessonProgram.getStopTime())
                .lessonProgramId(lessonProgram.getId())
                .lessonName(lessonProgram.getLesson())
                .build();
    }


    // Not :  getAll() *************************************************************************
    public List<LessonProgramResponse> getAllLessonProgram() {

        return lessonProgramRepository.findAll()
                .stream()
                .map(this::createLessonProgramResponse)
                .collect(Collectors.toList());

    }
    public LessonProgramResponse createLessonProgramResponse(LessonProgram lessonProgram) {
        return LessonProgramResponse.builder()
                .day(lessonProgram.getDay())
                .startTime(lessonProgram.getStartTime())
                .stopTime(lessonProgram.getStopTime())
                .lessonProgramId(lessonProgram.getId())
                .lessonName(lessonProgram.getLesson())
                .teachers(lessonProgram.getTeachers()
                        //bu yapi bize pojo dondurdu bunu DTO ya cevirmemiz lazim
                        .stream()
                        .map(this::createTeacherResponse)
                        //.stream dan gelen aksi assagida Pojo DTO donusumunu yapmasi icin olusturdugumuz yardimci
                        // methodu kullanarak map in icinde DTO ya donusturuyoruz
                        .collect(Collectors.toSet()))// codelar hala stream akisinin icinde bu kodelar Set list
                // yapisina ceviriyoruz
                //TODO Student yazilinca buraya ekleme yapilacak
                .build();
    }

    public TeacherResponse createTeacherResponse(Teacher teacher){// yukarida ihtiyac duyulan pojo-->DTO donusumu
        // yapan yardimci method
        return TeacherResponse.builder()
                .userId(teacher.getId())
                .name(teacher.getName())
                .surname(teacher.getSurname())
                .birthDay(teacher.getBirthDay())
                .birthPlace(teacher.getBirthPlace())
                .ssn(teacher.getSsn())
                .phoneNumber(teacher.getPhoneNumber())
                .gender(teacher.getGender())
                .email(teacher.getEmail())
                .username(teacher.getUsername())
                .build();
    }


    // Not :  getById() ************************************************************************
    public LessonProgramResponse getByLessonProgramId(Long id) {

        LessonProgram lessonProgram =  lessonProgramRepository.findById(id).orElseThrow(()->{
            throw new ResourceNotFoundException(String.format(Messages.NOT_FOUND_LESSON_MESSAGE,id) );
        });

        // return lessonProgramRepository.findById(id).map(this::createLessonProgramResponse).get();
        return createLessonProgramResponse(lessonProgram);
    }

    // Not :  getAllLessonProgramUnassigned() **************************************************
    public List<LessonProgramResponse> getAllLessonProgramUnassigned() {

        return lessonProgramRepository.findByTeachers_IdNull()
                // Pojo yu DTO ya ceviriyoruz
                // stream ile pojo olarak bir akis gelecek bu akisdan gelen datalari map ile
                // seve methodunda kullandigimiz  createLessonProgramResponse methoduna arguman olarak gonder diyoruz
                // hala String olarak geliyor gelen string datayi Collectors un toList() methodu ile topla-collec ve
                // dondur diyoruz
                .stream()
                .map(this::createLessonProgramResponse)
                .collect(Collectors.toList());
    }

    // Not :  getAllLessonProgramAssigned() **************************************************
    public List<LessonProgramResponse> getAllLessonProgramAssigned() {

        return lessonProgramRepository.findByTeachers_IdNotNull()
                .stream()
                .map(this::createLessonProgramResponse)
                .collect(Collectors.toList());
    }


    // Not :  Delete() *************************************************************************
    public ResponseMessage deleteLessonProgram(Long id) {
        // !!! id kontrolu
        lessonProgramRepository.findById(id).orElseThrow(()->{
            throw new ResourceNotFoundException(String.format(Messages.NOT_FOUND_LESSON_MESSAGE,id));
        });

        lessonProgramRepository.deleteById(id);

        // !!! bu lessonPrograma dahil olan teacher ve student lardada degisiklik yapilmasi gerekiyor , biz bunu
        //  lessonProgram entity sinifi icinde @PreRemove ile yaptik

        return ResponseMessage.builder()
                .message("Lesson Program is deleted Successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Not :  getLessonProgramByTeacher() ******************************************************
    public Set<LessonProgramResponse> getLessonProgramByTeacher(String username) {
        return lessonProgramRepository.getLessonProgramByTeacherUsername(username)
                .stream()
                .map(this::createLessonProgramResponseForTeacher)
                .collect(Collectors.toSet());
    }

    public LessonProgramResponse createLessonProgramResponseForTeacher(LessonProgram lessonProgram) {
        return LessonProgramResponse.builder()
                .day(lessonProgram.getDay())
                .startTime(lessonProgram.getStartTime())
                .stopTime(lessonProgram.getStopTime())
                .lessonProgramId(lessonProgram.getId())
                .lessonName(lessonProgram.getLesson())
                //TODO Student yazilinca buraya ekleme yapilacak
                .build();
    }

    // Not :  getLessonProgramByStudent() ******************************************************
    public Set<LessonProgramResponse> getLessonProgramByStudent(String username) {

        // lessonProgramRepository classina git oradaki .getLessonProgramByStudentUsername(username) methodu ile
        //  bu statament bir Pojo donduruyor bunu bize bir DTO olarak donmesi gerekiyor.
        return lessonProgramRepository.getLessonProgramByStudentUsername(username)
                // lessonProgramRepository classindan .getLessonProgramByStudentUsername(username) methodu ile DB den
                // POJO olarak gelen stream akisini
                .stream()
                // map ile lessonProgramRepository.getLessonProgramByStudentUsername(username) buradan gelen POJO
                // username bilgilerini asagida pojo -> DTO donusumunu yapan yardimci methodunu kullanarak
                // DTO ya cevirip
                .map(this::createLessonProgramResponseForStudent)
                // donusumu yapilan data lari Set yapida tutuyoruz
                .collect(Collectors.toSet());
    }

    public LessonProgramResponse createLessonProgramResponseForStudent(LessonProgram lessonProgram){

        return LessonProgramResponse.builder()
                .day(lessonProgram.getDay())
                .startTime(lessonProgram.getStartTime())
                .stopTime(lessonProgram.getStopTime())
                .lessonProgramId(lessonProgram.getId())
                .lessonName(lessonProgram.getLesson())
                .teachers(lessonProgram.getTeachers()
                        .stream()
                        .map(this::createTeacherResponse)
                        // Yukarida getAll methodu icin kullandigimiz pojo->DTO donusumunu yapan yardimci methodu
                        // map in cinde kullandik
                        .collect(Collectors.toSet()))
                .build();

    }

    // Not :  getAllWithPage() ******************************************************************
    public Page<LessonProgramResponse> search(int page, int size, String sort, String type) {

        Pageable pageable = PageRequest.of(page,size, Sort.by(sort).ascending());
        if(Objects.equals(type,"desc")) {
            pageable = PageRequest.of(page,size, Sort.by(sort).descending());
        }

        return lessonProgramRepository.findAll(pageable).map(this::createLessonProgramResponse);
    }

    // Not: getLessonProgramById() ***************************************************************
    public Set<LessonProgram> getAllLessonProgramById(Set<Long> lessonsIdList) {
        // Kendimiz LessonProgramlari Db den getirmek icin LessonProgramRepository katmanina gidecegiz orada assagida
        // .getLessonProgramByLessonIdList() olarak verdigimiz method ile DB den TeacherRequestten gelen Lesson nun id
        // sini kullanarak LessonProgram lari getirecegiz . Bunu lessonProgramRepository Katmaninda
        // .getLessonProgramByLessonIdList() methodunun icinde yapacagimiz Query ile yapacagiz
        return lessonProgramRepository.getLessonProgramByLessonProgramIdList(lessonsIdList);

    }
}

