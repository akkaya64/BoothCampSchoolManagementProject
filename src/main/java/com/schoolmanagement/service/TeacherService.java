package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.entity.concretes.Teacher;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.dto.TeacherRequestDto;
import com.schoolmanagement.payload.request.ChooseLessonTeacherRequest;
import com.schoolmanagement.payload.request.TeacherRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.TeacherResponse;
import com.schoolmanagement.repository.TeacherRepository;
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
public class TeacherService {//Evet hadi bu Classi insa etmeye once ihtiyac duyacagimniz bagimliliklari injection
    // olarak kurmakla baslayalim
    private final TeacherRepository teacherRepository;
    private final LessonProgramService lessonProgramService;
    private final FieldControl fieldControl;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRequestDto teacherRequestDto;
    private final UserRoleService userRoleService;
    private final AdvisorTeacherService advisorTeacherService;


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
            // TODO AdvisorTeacher creation isleminden ekleme yazilacak
            // Burasi kayit in logical islemlerinin yapildigi Teacher save() methodu burada Teacher creation islemi
            // yapilacak bu islemi yaparken bu Teacher Objesinin AdvisorTeacher olup olmadigini burada belirleyecegiz
            // isAdvisor a burada bakacagiz. request classindan gelen Teacher Objesinin boolean type da bir
            // isAdvisorTeacher variable i vardi.
            // Bu Teacher i create ederken isAdvisorTeacher requestten true gelirse AdvisorTeacherService git burada
            // saveAdvisorTeacher diye bir method var burada bu Teacher i AdvisorTeacher olarak kaydet demeliyiz.

            // Teacher daki bu degisiklik teacher tablosunda degil AdvisorTeacher tablosunda yapilacak. Bir teacher
            // objesinin advisor olup olmadigini anlamak icin isAdvisorTeacher field ina bakmamiz yeterli. Eger true
            // setlenmis ise bu teacherin advisor islemleri icin iliski icinde bulundugu advisor_teacher tablosuna gidecegiz
            if (teacherRequest.isAdvisorTeacher()){ // teacherRequest e git bunun isAdvisorTeacher() methodu true donerse
                // if in icine gir. AdvisorTeacherService git. birazdan olusturacagimiz saveAdvisorTeacher methodunu
                // calistir diyecegiz ama bunun icin once AdvisorTeacherService e gidebilmek icin
                // AdvisorTeacherService i yukarida bu class a injection yapmaliyiz.

                advisorTeacherService.saveAdvisorTeacher(savedTeacher); // ekleme yapilacak arguman olarak da kayit
                // islemi yapilmis saverTeacher objesini veriyoruz
            }


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
        // Hazir CRUD operasyonunu kullanacagiz. DB den veri getiriyorsak .findAll() methodunu kullandigimiz
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
        Optional<Teacher> teacher = teacherRepository.findById(userId); // Optional olarak varsa aldik.bir yapi
        // Optional olarak bir obje donuyorsa bu yapinin bos gelme ihtimali var, bu nedenle bos gelip gelmnediginin
        // kontrolu yapilmali

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
            // etmemiz lazim. eger degistirilmedi ise unique kontrolu yapmamiza gerek yok. bunun icin update methodunun
            // disinda bir email in de duplacateligini kontrol eden bir method yazarak handle ettik.
            fieldControl.checkDuplicate(newTeacher.getUsername(),//
                    newTeacher.getSsn(),
                    newTeacher.getPhoneNumber(),
                    newTeacher.getEmail());
        }
        // kayit islemini tamamlayabilmemiz icin elimizdeki DTO Set yapidaki List imizi Pojo Class a cevirmemiz lazim
        // bunun icin updateTeacher scope sinin hemen disinda bunu yapan yardimci bir method yaziyoruz. Teacher Pojo
        // Classini return edecek createUpdatedTeacher() methodunu yaziyoruz.


        Teacher updatedTeacher =  createUpdatedTeacher(newTeacher, userId); // artik pojo olan update edilmis Object
        // imizi updatedTeacher veriable nin icine atiyoruz.

        // !!! password encode ediliyor
        updatedTeacher.setPassword(passwordEncoder.encode(newTeacher.getPassword()));// passwordEncoder classina gidip
        // encode() methodun parametresine request den gelen passwordu verip ister degistirilmis ister degistirilmemis
        // olsun encode ettikten sonra  updatedTeacher degiskenine setliyoruz.

        // Set password daki setleme mantigi ile ayni sebebden dolayi Lessonlarin update yapilmis olma ihtimali nedeni
        // ile ister degistirilmis ister degistirilmemis olsun requestten gelen lessons lari !!! Lesson program
        // setliyoruz. zaten yukarida Lessons lari getirmistik birdaha getLessonsIdList() methodu ile idye gore
        // lessonslari getirme islemini yapmaya gerek yok zaten yapilmisi var kullan gec.
        updatedTeacher.setLessonsProgramList(lessons); // TODO buraya bakilacak


        Teacher savedTeacher = teacherRepository.save(updatedTeacher);//Artik updatedTeacher i DB ye gonderebiliriz.
        // TODO AdvisorTeacher eklenince yazilacak
        advisorTeacherService.updateAdvisorTeacher(newTeacher.isAdvisorTeacher(), savedTeacher);
        // advisorTeacherService e git AdvisorTeacherService Classinad create edecek oldugumuz .updateAdvisorTeacher()
        // update methoduna paremetre olarak requestten gelen newTeacher objesinin isAdvisorTeacher() methodu getir
        // yani true mu false mi isAdvisorTeacher() boolean degerini ve savedTeacher objesini verecegiz


        return ResponseMessage.<TeacherResponse>builder()
                .object(createTeacherResponse(savedTeacher)) // updatedTeacher da yazilabilir
                .message("Teacher updated Successfully")
                .httpStatus(HttpStatus.OK)
                .build();




    }
    private boolean checkParameterForUpdateMethod(Teacher teacher, TeacherRequest newTeacherRequest) {
        return teacher.getSsn().equalsIgnoreCase(newTeacherRequest.getSsn())
                || teacher.getUsername().equalsIgnoreCase(newTeacherRequest.getUsername())
                || teacher.getPhoneNumber().equalsIgnoreCase(newTeacherRequest.getPhoneNumber())
                || teacher.getEmail().equalsIgnoreCase(newTeacherRequest.getEmail());
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

    // Not: getTeacherByName() **************************************************
    public List<TeacherResponse> getTeacherByName(String teacherName) {

        // DB den gelecek Pojo datalari DTO ya cevirmeliyiz
        // burada keyword lari kullanarak bir get methodu turetmeliyiz. Keywordlar kullandigimiz icin Repositoryde
        // create edilen methodun icine baska herhangi bir code yazmamaiza gerek yok.
        // eger teacherRepository
        // requestten gelen (teacherName) teacher name i
        // iceriyorsa Containing
        // onu getTeacherByName() methodu ile getir. gelen bu deger bir pojo yapi olacagi icin stream() akisina al
        // bu akistan gelen verileri map ile DTO verisine create et....
        return teacherRepository.getTeacherByNameContaining(teacherName)
                // Containing String manipulation methodlarindan bir tanesi
                .stream()
                .map(this::createTeacherResponse)
                .collect(Collectors.toList());
    }

    // Not: deleteTeacher() *****************************************************
    public ResponseMessage<?> deleteTeacher(Long id) {

        teacherRepository.findById(id).orElseThrow(()->{
            throw new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE);
        });

        // lessonProgram tablosunda teacher kaldirilacak ?
        teacherRepository.deleteById(id); //SpringFrameWork un hazir CRUD operasyonlarindan deleteById(id) methoduna
        // Repository den gelen id yi veriyoruz ve silme islemini tamliyoruz.

        // lessonProgram tablosunda teacher kaldirilacak ?

        return ResponseMessage.builder()// artik silindigi icin kullaniciya bir obje dondurulmeyecek
                .message("Teacher is Deleted")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Not: getTeacherById() ****************************************************
    public ResponseMessage<TeacherResponse> getSavedTeacherById(Long id) {

        //Eger DB de bir obje varsa Pojo olarak getirecek v teacher variable in icine koyacak yoksa exception firlatacak
        Teacher teacher = teacherRepository.findById(id)// Springframework kendi kendine security bir yapi oldugu icin
                // Spring in  findById() methodu paramatre olarak default da Long data type inda bir id aliyor ve
                // Optional bir yapi da bir Teacher donduruyor. Yani bizi nullPoinException dan kurtaran bir class...
                // eger bu field bos biir sekilde gelirse nullPointException alinacak demis oluyoruz.
                .orElseThrow((()-> new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE)));

        return ResponseMessage.<TeacherResponse>builder()
                .object(createTeacherResponse(teacher))
                .message("Teacher Successfully found")
                .httpStatus(HttpStatus.OK)
                .build();

    }

    // Not: getAllWithPage() ****************************************************
    public Page<TeacherResponse> search(int page, int size, String sort, String type) {

        Pageable pageable = PageRequest.of(page,size, Sort.by(sort).ascending());
        if(Objects.equals(type, "desc")){
            pageable = PageRequest.of(page,size, Sort.by(sort).descending());
        }

        return teacherRepository.findAll(pageable).map(this::createTeacherResponse);//DB den gelen datalar DTO ya
        // cevirdik
    }

    // Not: addLessonProgramToTeachersLessonsProgram() **********************************
    public ResponseMessage<TeacherResponse> chooseLesson(ChooseLessonTeacherRequest chooseLessonRequest) {

        //!!! ya teacher yoksa
        Teacher teacher = teacherRepository.findById(chooseLessonRequest.getTeacherId()).orElseThrow(//Gelen teacherId
                // Optional yapida oldugu icin ya yoksayi hemen handle etmemiz lazim. Yoksa exception firlatacagiz
                ()-> new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE));

        //!!! LessonProgram getiriliyor Requestten gelen id yi kullanarak Db den LessonProgram lari cekip LessonProgram Typenda Set List
        // Yapisindaki lessonsProgram variablenin icine koyacagiz.
        Set<LessonProgram> lessonPrograms = lessonProgramService.getAllLessonProgramById(chooseLessonRequest
                .getLessonProgramId());

        // !!!  LessonProgram ici bos mu kontrolu
        if(lessonPrograms.size()==0) {
            throw new ResourceNotFoundException(Messages.LESSON_PROGRAM_NOT_FOUND_MESSAGE);
        }


        // yukarida ekleyecegizmiz ders programini olusturduk ama simdi bu LessonProgrami ekleyecegimiz Teacherin da
        // daha once olusturulmus bir LessonProgrami olabilir. bu yeni olusturulan ders programini eski ders
        // programinin uzerine keleyecegiz

        // !!! Teacher in mevcut ders programi getiriliyor
        Set<LessonProgram> existLessonProgram = teacher.getLessonsProgramList();
        // LessonProgram zaten Set yapida oldugu icin unique yapida bu nedenle olusacak olan DB de var olan ile ayni mi
        // diye bir daha checkDuplicate gibi bir methoda gerek yok.
        // Yeni olusturulan LessonProgram DB de varsa bie exception firlayacak bunu handle etmemiz gerekiyor. Bunu
        // yeni bir class olusturup icine gerekli methodu yazacagiz. Bunu eklenecek olan LessonProgram indaki
        // Lesson larin baslama ve bitis zmanlari ile Teacher in mevcud LessonProgramin daki Lesson larin
        // baslama ve bitis zamanlarinin cakisip cakismadigini kontrol edecegiz.


        CheckSameLessonProgram.checkLessonPrograms(existLessonProgram,lessonPrograms);
        existLessonProgram.addAll(lessonPrograms);// existLessonProgram 'a utils kutuphanesinde bulunan .addAll
        // methodunu kullanarak requestten gelen lessonPrograms lari getirip setliyoruz.
        teacher.setLessonsProgramList(existLessonProgram);//teacher pojo classina teacher in .setLessonsProgramList()
        // methoduna existLessonProgram objesini vererek setliyoeuz
        Teacher savedTeacher = teacherRepository.save(teacher); //  teacherRepository nin .save() methoduna teacher
        // pojo sunu vererek DB de kalici hale getiriyoruz. Daha sonra bu teacher objesini kullaniciya geri
        // dondurebilmek icin Teacher data type indaki savedTeacher variablesine atiyoruz

        return ResponseMessage.<TeacherResponse>builder()
                .message("LessonProgram added to Teacher")
                .httpStatus(HttpStatus.CREATED)
                .object(createTeacherResponse(savedTeacher))
                .build();

    }

    // !!! StudentInfoService katmaninda layerinde kullanilmak icin olusturuldu
    // Bir service katmaninda POJO donduren bir method varsa bu method baska bir service katmaninda kullanilmak uzere
    // olusturulmustur yada soyle kotu bir tasarim yapilmistir service katmani pojo donduruyordur controller katmaninda
    // POJO-DTO donusumu yapiliyordur
    public Teacher getTeacherByUsername(String username) {
        if (!teacherRepository.existsByUsername(username)){//request den gelen username eger DB de yoksa boolean olarak
            // false donecek ama degeri false olunca if methodunun icine giremiyorduk bu nedenle false gelen degeri
            // tersleyip if in icine girip username bulunamadi diye exception firlatiyoruz. varsa true gelecek ama
            // tersleme islemi yaptigimiz icin true degeri false donecek ve if in icine girmeyip direk if in disinda
            // bulunan return u aktive edecek
            throw new ResourceNotFoundException(Messages.NOT_FOUND_USER_MESSAGE);
        }

        return teacherRepository.getTeacherByUsername(username);//TeacherRepository de getTeacherByUsername methodu
        // olusturuldu. Cunku username uzerinden getirilen Teacher objesine ihtiyaci var.
    }
}

















