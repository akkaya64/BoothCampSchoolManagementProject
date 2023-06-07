package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.Lesson;
import com.schoolmanagement.exception.ConflictException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.LessonRequest;
import com.schoolmanagement.payload.response.LessonsResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.repository.LessonRepository;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;

    // Not :  Save() *************************************************************************
    public ResponseMessage<LessonsResponse> save(LessonRequest lessonRequest) {
        // Hey!!! Hosgeldiniz bu Classi sizin olusturdugunuzu asla unutmayacagiz size cok minnettariz Artik Service
        // katindasiniz bu cok harika degil mi :) burada neler yapabileceginizi bir dusunsenize :) Lutfen kayit yapmak
        // istediginiz Lesson daha once kayit edilmis mi bir kontrol edin yani conflict edilmis mi? Bu control baska
        // islemlerde de kullanilabilr lutfen bunun icin bir yardimci method olusturun ve o methodu buradan cagirip
        // kullanin. tekrar soyleyeyim ayni methodu baska islemler de de kullanabilirsiniz . kolay gelsin

        // !!! Conflict kontrolu yapiyoruz.
        if (existsLessonByLessonName(lessonRequest.getLessonName())){
            // if 'e parametre olarak verdiginiz code blogu sunu yapacak request den gelen LessonName
            // existsLessonByLessonName methodu ile Db de daha once varmi kontrol edilecek eger sonuc true donerse
            // if in icine girecek ve asagidaki exception u throw yapacak. ama oncelikle utils packagesindeki
            // Messages calasinin icinde  bir exception mesaji olusturmalisiniz. bundan sonrasini ben hallederim
            // kullanici ayni isimli bir ders girmeye calisirsa artik onu uyarma benim gorevim.
            throw new ConflictException(String.format(Messages.ALREADY_REGISTER_LESSON_MESSAGE,
                    lessonRequest.getLessonName()));


        }

        // Hey!!! Haricasin artik DTO da olsa elinde Json formatinda tum kontrolleri yapimis bir obje var. Dilersiniz
        // artik bunu save methodu ile DB de kalici hale getirelim nedersiniz. Json formatinda DB de veri tutamiyorduk
        // bunun icin lutfen benim icin Json gelen veriyi asagidaki code blogunu kullanarak pojo ya cevirirmisin
        // simdiden cok tesekkurler.... Sanirim bunu yapabilmek icin bir yardimci methoda ihtiyacin olacak, bu methodu
        // olusturrken simdiden iyi sanslar..
        Lesson lesson = createLessonObject(lessonRequest);//Tebrikler!!! artik nur topu gibi bir Lesson objeniz var v
        // e artik bu DB de kayitli

        // Simdi lutfen kullaniciya bir mesaj vermemiz gerektigini biliyorsun ve tahmin ettigim gibi hemen buraya bir
        // return dondurme codelarini yazacaksin. Hadi bakalim.

        // Hey kucuk bir hatirlatma: DB deki lesson objesi pojo ama kullanicinin bunu okuyabilmesi icin bu obje k
        // ullaniciya Json olarak gitmeli. Artik sen ne yapacagini biliyorsun :)

        return ResponseMessage.<LessonsResponse>builder()
                .object(createLessonResponse(lessonRepository.save(lesson)))// Ipucu!!! simdiye kadar yukarida
                // Json u pojo ya cevirdigin code blogundan sonra yaptigin  kayit islemi var ya
                // lessonRepository.save(lesson) iste onu yukarida yazip burada cagirip Json formatina cevirip object
                // olarak veriyordun ya artik bunu tek islemde yapabileceksin yukarida yazip burada cagirmana gerek yok
                // lesson unun DB ye kayit ilemini burada yapabilirsin ve bunu yine burda Json formatina cevirisin.
                // Nasil ama :) Bir tasla iki kus vurmak boyle olsa gerek...
                .message("Lesson Crated Successfully")
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    private boolean existsLessonByLessonName(String lessonName){
        // Hey!!! Ben bir yardimci methodum ve coook faydaliyim benim icimdeki codelari ihtiyac duydugun her yerde
        // kullanabilirsin sadece beni cagir yeter hemen gelirim ve bir cirpida gorevimi yaparim. benim gorevim;
        return lessonRepository.existsLessonByLessonNameEqualsIgnoreCase(lessonName);
        // lessonRepository repository sine gitmek icinde bulundugun bu methoda parametre olarak verilen kullanicidan
        // gelen String turundeki lesson ismini buyuk kucuk harf duyarliligina bakmadan DB de daha once kullanilip
        // kullanilmadigini kontrol etmek. Bunun icin isterseniz yukarida kirmizi yanan
        // existsLessonByLessonNameEqualsIgnoreCase bu code blogun uzerine gidip LessonRepository de bir methodunu
        // olusturalim boylelikle bu methodu kontrol icin kullnabilirim. merak etmeyin methot olustuktan sonra artik
        // kirmizi yanmayacak.
        // Bu bir turetilen method mudur yoksa buna bir query yazilmasi gerekiyor mu? Merak ediyorsan cevabini
        // hemen vereyim...
        // exists bir keyword
        // Lesson bir entity class
        // By bir keyword
        // LessonName Lesson entity class inda ki bir field
        // Equals bir keyword
        // IgnoreCase de bir keyword
        // Yani!!! hadi gine iyisin kurtardim seni :) bir query yazmana gerek yok ExistsBy i yukaridaki gibi turetebilirsin
        // ne kadar sade ve temiz degil mi?
    }

    private Lesson createLessonObject(LessonRequest request){

        return Lesson.builder()
                .lessonName(request.getLessonName())
                .creditScore(request.getCreditScore())
                .isCompulsory(request.getIsCompulsory())//HEY!! zorunlu olmama durumu icin bir kontrol yok sanirim
                // lutfen bu kontrolu eklmeyi unutma. nereye eklenecegini sen biliyorsun benim soylememe gerek yok
                .build();

    }

    private LessonsResponse createLessonResponse(Lesson lesson) {
        return LessonsResponse.builder()
                .lessonId(lesson.getLessonId())
                .lessonName(lesson.getLessonName())
                .creditScore(lesson.getCreditScore())
                .isCompulsory(lesson.getIsCompulsory())
                .build();
    }


    // Not :  Delete() *************************************************************************
    public ResponseMessage deleteLesson(Long id) {

        // Simdiye kadar Springframework den gelen existBy ile keyword u nunu gelistirip  oyle  calistin ama artik
        // bunu sindirdiysen, anladiysan bunun nasil calistigini senin icin daha iyi bir yolumuz var o da orElseThrow...
        // Bunun tatini cikart dostum :)
        Lesson lesson = lessonRepository.findById(id).orElseThrow(()->{
            // Spring den gelen findById ile de calisabilirsin dostm, findById ye methoda verdigimiz parametreyi objeyi
            // bulmasi icin verirsin. Note: findById Optional bir yapi dondur.
            // findById Optional bir yapi dondudugu icin spring in orElseThrow() methodunu kullanabilirsin..
            // orElseThrow() methodundan sonra Lamda ile exception firlatabilirisin. harika degilmi dostum :) benden
            // sana tavsiye firsatini buldukca bu sekilde yaz gitsin basit sade siple anlasilir fazla code
            // kalabaligina gerek yok
            return new ResourceNotFoundException(String.format(Messages.NOT_FOUND_LESSON_MESSAGE, id));//Dostum Dostum
            // umarim util deki message clasinda buna uygun bir uyari mesaji olusturmussundur . zaten olusturmadiysa
            // kirmizi ile CTE hatasi alirsin gerci, RTE hatasi almaktan bin kat daha iyidir ama hata hatadir.
        });

        lessonRepository.deleteById(id);//Tebrikler!!! sildin ama dikkatli ol danisarak sil....
        //Repoya hic gitmeye gerek kalmadi cunku hazir CRUD operasyonlarini kullandik

        return ResponseMessage.builder()
                .message("Lesson is deleted successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }

    // Not :  getLessonByLessonName() **********************************************************
    public ResponseMessage<LessonsResponse> getLessonByLessonName(String lessonName) {

        Lesson lesson = lessonRepository.getLessonByLessonName(lessonName).orElseThrow(()->{
            // Dostum oncelikle LessonRepository classinda getLessonByLessonName adindaki methodu olustur
            // yukaridaki code blogu bir lesson donecegi icin method orada olusunca generic yapisini Lesson olarak
            // degistirmey unutma

            // Hey.. Sen sen evet sen sayin Developer getLessonByLessonName((lessonName))aslinda turetilen bir method
            // bu method dan sonra orElseThrow() methodunu setleyerek getLessonByLessonName() methodunun Optional bir
            // yapiya sahip olacagini belirtmis oluyorsun. Burada Hemen lambda expression i yaz gec....
            // lesson bulunamadi diye mesaji ben gonderirirm sen hixc merak etme cunku
            return new ResourceNotFoundException(String.format(Messages.NOT_FOUND_LESSON_MESSAGE, lessonName));
        });

        // Kullaniciya herzamanki mesajimizi, status kodumuzu ve objemizi verelim degil mi?

        return ResponseMessage.<LessonsResponse>builder()
                .message("Lesson Successfully found")
                .object(createLessonResponse(lesson))
                .build();
    }

    // Not :  getAllLesson() **********************************************************************
    public List<LessonsResponse> getAllLesson() {

        return lessonRepository.findAll()
                .stream()
                .map(this::createLessonResponse)
                .collect(Collectors.toList());
    }

    // Not :  getAllWithPage() **********************************************************
    public Page<LessonsResponse> search(int page, int size, String sort, String type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        if(Objects.equals(type,"desc")) {
            pageable = PageRequest.of(page,size,Sort.by(sort).descending());
        }

        return lessonRepository.findAll(pageable).map(this::createLessonResponse);
    }

    // Not :  getAllLessonByLessonIds() *****************************************************
    public Set<Lesson> getLessonByLessonIdList(Set<Long> lessons) {

        return lessonRepository.getLessonByLessonIdList(lessons);
    }


}
