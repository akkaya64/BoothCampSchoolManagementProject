package com.schoolmanagement.utils;

import com.schoolmanagement.entity.concretes.LessonProgram;
import com.schoolmanagement.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

public class CheckSameLessonProgram {

    // Bu yapi bir Requestten gelen LessonProgrami iki DB den gelen LessonProgrami alarak calisacak iki datayi
    // kiyaslayacak burada sadece Id ye bakmasi yeterli degil Fieldlari da eslesiyormu diye kontrol etmeli bu nedenden
    // dolayi checkDuplacate methodunu kullanamiyoruz o bu seneryoya uygun degil onda sadece id ler eslesiyormu diye
    // bakiyorduk ve bunu yeterli oldugu cclass lardan cagiriyorduk. ayni fieldlara sahip bir LessonProgram Create
    // edildiginde buna farkli bir id vererek DB ye kayit eder bu nedenle LessonProgramlarin cakismasini onlemek icin
    // Fieldlarini da kontrol etmeliyiz. Biraz ince iscilik yapcaz gari burada.

    //  SENERYO 1) Teacher 'e ilk defa lessonProgram atamasi yapiliyor Bu techer e iki farkli Lesson atanabilir.
    //  Burada bu derslerin date ve time lari cakisiyormu kontrol edilmesi lazim. Cunku bir Teacher ayni anda iki
    //  farkli derse giremez

    // SENERYO 2) Teacher in mevcut dersleri var update edilecek yeni lessonlar eklenecek yeni lessonlarin date and
    // time lari mevcut Lessonlarin Date ve Time lari ile cakisiyormu kontrolunun yapilmasin lazim
    public static void  checkLessonPrograms(Set<LessonProgram> existLessonProgram, Set<LessonProgram> lessonProgramRequest){
        //

        // karsilastirma yapacagimiz sartlari yaziyoruz; Once mevcut LessonProgram bos ise requestten gelen Lesson
        // Programlari karsilastiracagiz.lessonProgramRequest.size() i 0 dan bukse oldugunu kontrol edersek sadece bir
        // tane LessonProgram oldugunda bunu herhangi birseyle karsilastirmayacagimiz icin karsilastirma methodyuna
        // sokmak mantiksiz olur bu nedenle size i birden buyukse olarak olusturuyoruz ki karsilastirma methodu calissin
        if(existLessonProgram.isEmpty() && lessonProgramRequest.size()>1) {
            // yukardaki sartlar saglanmissa lessonProgramRequest den gelen LessonProgram lari karsilastiracak yardimci
            // methodu bu scope nin disinda olusturup bu if in icinde kullanacagiz
            checkDuplicateLessonPrograms(lessonProgramRequest);
        } else {//existLessonProgram in icinin dolu oldugu durumda ise bu else nin icine girecek
            checkDuplicateLessonPrograms(lessonProgramRequest);
            checkDuplicateLessonPrograms(existLessonProgram,existLessonProgram); // asagida olusturulan methodu
            // overload ediyoruz yani iki is birden yaptiriyoruz db deki existLessonProgram ile requestten gelen
            // existLessonProgram i birbiri ile karsilastiriyoruz
        }

    }


    // Bir foreach yapisi ile Date lar ve Time lari tektek kontrol ederiz ama bundan dah iyisi ikisi de aslinda
    // birer String yapi bunlari birbirine Concatenations yapariz yani Date + Time == DateTime gibi bu yeni
    // String data ile eslesen bir deger varmi onun kontrolunu yapariz boylelikle programi hizlandirmis oluruz

    // Burada once requestten gelen LessonProgram lari kontrol edecegiz ve bu gelen datalarin unique olup olmadiklarini
    // String Set bir List yapisi ile calisip kontrol edecegiz Yani Set yapisi benzersiz degerleri aliyordu icine ayni
    // ozelliklere ait baska bir deger eklemeye calistigimizda yeni degeri eklmeiyor ve add() methodu false donuyor.
    // bu set yapisinin icine daha once giren bir date ve time lari ayni lessonProgram setlenmeye calisirsa exception verecek
    private static void checkDuplicateLessonPrograms(Set<LessonProgram> lessonPrograms) {
        // Set yapidaki Set<LessonProgram> icindeki Lessons larin Start Date ile Start Time lari ayni ise
        // exception handle edip firlatacagiz

        // iki farkli Date ve Time verilerini concatenations yaptiktan sonra icine koyacagimiz yapiyi olusturuyoruz
        // Bir Set yapi olusturuyoruz bu yapi icine unique String degerler alacak(DateTime)
        // Ismini uniqueLessonProgramKeys
        // bunun da ici bos bir sekilde HashSet ten olusmasini sagliyoruz.
        Set<String> uniqueLessonProgramKeys = new HashSet<>();

        // for yapisini olusturuyoruz
        // data type 'i LessonProgram olacak, adinida lessonProgram veriyoruz bu foreach loop parametrede verdigimiz
        // requestten gelen LessonPrograms set yapisindaki Listin icinde dolasacak
        for (LessonProgram lessonProgram : lessonPrograms ) {

            // concatenation islemini yapiyoruz, lessonProgram.getDay().name() ile lessonProgram.getStartTime()
            // birlestirip String yapidaki lessonProgramKey variablesinin icine koyuyoruz
            // lessonProgram.getDay().name(); lessonProgram a git .getDay() gunu getir .name() gunun ismini ver
            // lessonProgram.getStartTime(); LessonProgram a git .getStartTime() methodu ile zamani getir.
            // autoCasts islemi ile time  otomatikmen String bir yapiya donmus oluyor
            String lessonProgramKey = lessonProgram.getDay().name() + lessonProgram.getStartTime(); // Artik elimizde
            // her LessonProgram icin elimizde unique bir deger var. Her lessonProgram icin bir lessonProgramKey generate ettik
            // sistem su sekilde calisacak Math lesson a ait Lesson program icinde artik
            // Date(Sali) + Time(11 00) == DateTime(Sali1100) gibi bir deger var. Ayni sekilde Physic Lesson nuna ait
            // LessonProgram icinde de (Sali1100) gibi bir unique degeri var. Set yapilar tekrarli yapilari kabul etmez
            // yeni setlenmek isten deger daha once var olan deger ile eslesiyorsa yeni deger setlenmez. Bunun icin
            // yeni setlenecek objenin Bu set yapinin icinde var mi yok mu kontrolunun yapilmasi lazim yeni deger ile
            // eslesen bir obje varsa exception atilmasi gerekir.
            // Iki field i tek tek kontrol etmiyoruz ikisini birlestirp tek bir field i kontrol ediyoruz.
            // Set yapilarda add methodu true yada false dondurur eklendiyse true eklenmediyse false dondurur. False
            // firliyorsa o zaman bir duplicate var


            // her add islemi yapildiginda uniqueLessonProgramKeys in icine lessonProgramKey unique degerini koyuyoruz
            // If statement i ile uniqueLessonProgramKeys in icine yeni bir lessonProgramKey eklemek istedigimizde
            // uniqueLessonProgramKeys in bu yeni lessonProgramKey i icerip icermedigini kontrol etmemiz lazim.
            // Eger iceriyorsa exception firlatilacak
            if(uniqueLessonProgramKeys.contains(lessonProgramKey)){
                throw  new BadRequestException(Messages.LESSON_PROGRAM_EXIST_MESSAGE);
            }
            // icermiyorsa da uniqueLessonProgramKeys 'a lessonProgramKey 'i add islemi basarili bir sekilde
            // tamamlanmis olacak
            uniqueLessonProgramKeys.add(lessonProgramKey);
        }
    }

    // Burada daha once olusturulmus existLessonProgram larin icinde requestten gelen lessonProgramRequest lar var mi?
    // kontrol edilcek


    public static void checkDuplicateLessonPrograms(Set<LessonProgram> existLessonProgram, Set<LessonProgram> lessonProgramRequest ){
        // once existLessonProgram a gidecegiz startTime ini alacagiz lessonProgramRequest in icinde varmi kontrol edecegioz
        // Daha sonra ayni sekilde day bilgisinin kontrolu yapilacak.

        // nested for yapisi ilede yapabiliz ancak ikinci for girmeden lambda stream API ile method chain kurarak da
        // yapabilabilir. nested for yapmamiza gerek yok.
        // existLessonProgram ile lessonProgramRequest ikisi de da Set yapida oldugu icin lamda ile yazabiliriz

        for (LessonProgram requestLessonProgram : lessonProgramRequest) {
            // LessonProgram data turunde requestLessonProgram adinda bir veri gelecek bu veri de lessonProgramRequest
            // in icinden gelecek yani requestLessonProgram, lessonProgramRequest in icinde dolasacak.

            // foreach loop lessonProgramRequest in icinde dolasak
            // stream API de existLessonProgram i bir akis haline getirecek...
            // If yapisina parametre olarak, existLessonProgram stream akisindan elde ettigimiz getStartTime() ile
            // foreach loop dan gelen requestLessonProgram in getStartTime() esitmi diye kontrol edecegiz.
            // ve(&&) lessonProgram dan gelen tarihin(getDay()) ismi(name()) ile  forloop dongusun den gelen
            // tarihin(getDay()) ismi(name()) esitmi diye kontrol ediyoruz. Eger ikisinden birisinde esitlik varsa o
            // o zaman true olacak ve code if yapisinin icine girip throw u calistiracak
            if(existLessonProgram.stream().anyMatch(lessonProgram ->
                    lessonProgram.getStartTime().equals(requestLessonProgram.getStartTime()) &&
                            lessonProgram.getDay().name().equals(requestLessonProgram.getDay().name()))) {
                throw  new BadRequestException(Messages.LESSON_PROGRAM_EXIST_MESSAGE);
            }

        }

    }

    // TODO : startTime baska bir lessonProgramin startTime ve endTime arasindami kontrolu eklebnecek
}