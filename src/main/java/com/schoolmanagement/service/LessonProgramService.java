package com.schoolmanagement.service;
// LessonProgramService Classimiza hos geldiniz sizi burada gormek gercekten heyecan verici Intelji-Springfreamwork ve
// lombook birlikteligi sizi burada gormekten mutluluklar duyar. Sizi is ustunde gormeyi cok isteriz. hadi buyrun ise
// koyulun lutfen. Simdiden size kolayliklar gelsin.. Birazcik kafa yemeye hazir olun.... :)) ha ha haaa. Kusuk bi
// hatirlatma bu bir Service Katmani bunu Springe bildirmelisin dostum ise oradan basla.

import com.schoolmanagement.entity.concretes.EducationTerm;
import com.schoolmanagement.entity.concretes.Lesson;
import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.exception.BadRequestException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.LessonProgramRequest;
import com.schoolmanagement.payload.response.LessonProgramResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.repository.LessonProgramRepository;
import com.schoolmanagement.utils.Messages;
import com.schoolmanagement.utils.TimeControl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

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
    // bilgisi de eklemeliyiz


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




    }


}

