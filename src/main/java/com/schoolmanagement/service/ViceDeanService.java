package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.ViceDean;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.dto.ViceDeanDto;
import com.schoolmanagement.payload.request.ViceDeanRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.ViceDeanResponse;
import com.schoolmanagement.repository.ViceDeanRepository;
import com.schoolmanagement.utils.CheckParameterUpdateMethod;
import com.schoolmanagement.utils.FieldControl;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Service kati oldugu icin annote ediliyor.
@RequiredArgsConstructor
public class ViceDeanService {

    private final ViceDeanRepository viceDeanRepository;// Burada ViceDean logical islemleri yapmamiz icin
    // ViceDeanRepository Classini buraya injection yapiyoruz
    private final AdminService adminService; // AdminService checkDuplicate methodunu buradaki rolleri olusturup kayit
    // islemini yaparken kullanabilmek icin bu class 'in icine injection yapiyoruz.
    private final ViceDeanDto viceDeanDto; // Bu bagimlilik kullanilarak degisim yapan methodu buraya cagirabiliriz.
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final FieldControl fieldControl;

    // Not :  Save() *************************************************************************
    public ResponseMessage<ViceDeanResponse> save(ViceDeanRequest viceDeanRequest) {

        // Burada oncelikle responseMessage olarak donen objenin unique olmasi gereken degerlerinin
        // (Username, Snn ve PhoneNumber)unique olup olmadiklarini denetleyerek parametre olarak verilen
        // viceDeanRequest objesinin unique ligi kontrol etmemiz gerekiyor.
        // Bu kontrol islemini daha once AdminService Classinda olusturdugumuz checkDuplicate methodu ile yapacagiz
        // checkDuplicate methodunu burada kullanabilmek icin AdminService Classini injection yapiyoruz.


        // adminService 'in checkDuplicate methoduna git orada viceDeanRequest in username 'i, ssn 'i ve
        // phoneNumber 'ini unique olup olmadigini check et.
        // burada bir check edilen parametre olarak verilmis 3 fielden birisinde bile eslesme Duplicate tesbit edilirse
        // code asagiya inmez Controller katmanina gecer ve kullaniciya bir exception firlatir
//        adminService.checkDuplicate(viceDeanRequest.getUsername(), viceDeanRequest.getSsn(),
//                viceDeanRequest.getPhoneNumber());//Elveda sana eski checkDuplicate hosgeldin yeni checkDuplicate
        fieldControl.checkDuplicate(viceDeanRequest.getUsername(), viceDeanRequest.getSsn(),
               viceDeanRequest.getPhoneNumber());

        // parametre olarak verilen degerler unique ise code asagiya iner. Burada artik DB ye kayit islemi yapilacagi
        // icin Json formatinda olan verilerin DTO(Data Transfer Object) dan Pojo ya cevrilmesi gerekir. bunun icin
        // asagida save() methodunun disinda createPojoFromDTO adinda yardimci bir method olustururuyoruz.


        ViceDean viceDean = createPojoFromDTO(viceDeanRequest);
        // Donusum yapilmasi icin createPojoFromDTO 'e parametre olarak viceDeanRequest 'i ver artik Pojo olan degeri
        // ViceDean turundeki viceDean 'e ata...

        // Roll ve password encode islemleri
        // ViceDeanDto Class indaki dtoViceBean methodu ile olusturdugumuz user in fieldlarina ek olarak bir rol
        // atayacagiz ve girilen plain text password unu da encode edip DB ye setleyecegiz.
        viceDean.setUserRole(userRoleService.getUserRole(RoleType.ASSISTANTMANAGER));
        // UserRoleService Katini bu Class a injection 'u kullanip' enum yapidaki RoleType sine erisip ASSISTANTMANAGER
        // rolunu viceDean nin DB deki kullanici rolune setliyoruz

        viceDean.setPassword(passwordEncoder.encode(viceDeanRequest.getPassword()));

        // viceDeanRepository deki Springframework kutuphanesinin icinde bulunan JpaRepository interfaces inde
        // bu kullanicinin bir Id si otomatik olatak generate(Olusturulup) edilip yine JpaRepository nin .save()
        // metodu ile viceDeanRepository de viceDean olarak kalici hale gelecek.
        viceDeanRepository.save(viceDean); // ): :) :) Buyuk kutlama tebrikler......

        // response nesnesi olusturulacak
        return ResponseMessage.<ViceDeanResponse>builder()
                .message("Vice Dean Saved") // Kullaniciya gonderecegimiz bu tarzdaki message ler icin bu sekilde
                // hardcode yazmamamiz gerekiyor. Application yml den alabiliriz yada  bunun icinde biryerlerde bir
                // Class olusturup bu Classi Injectionc edip oradan cekmeliyiz. boylelikle code tekrarlarindan da
                // kurtulmus oluruz. ileride bu mesajlarin degistirilmesi istendiginde tek bir class da degisiklik
                // yaparak kolayca kullaniciya giodecek mesajlari degistirmis oluruz
                .httpStatus(HttpStatus.CREATED)
                .object(createViceDeanResponse(viceDean))// unique degerleride artik create edilmis olan viceDean
                // objesini createViceDeanResponse tekrar Json yapiya cevirip kullaniciya gonderiyoruz.
                // viceDeanRequest uzerinden de kullaniciya kullanici bilgilerini gonderebilirdik ancak unique olan
                // degerlerin unique olup olmadiklari henuz denetlenmemisti. viceDean ile kullaniciya butun
                // kontrollerden gecmis id si olusmus bir kullanici gonderebiliyoruz.
                // Pojo --> DTO Donusum islemini yapacak createViceDeanResponse() methodunu asagida yaziyoruz.
                .build();

    }

    // Donecek olan deger de Pojo olan ViceDean donecek. Cevirme islemini yapacak methoda viceDeanRequest i
    // parametre olarak veriyoruz (ViceDeanRequest viceDeanRequest). viceDeanRequest uzerinden gelen DTO nun
    // pojo donusumu yapilacak bunu yapmanin iki yolu yar biri burada builder methotlari ile yapabiliriz
    // ikincisi; Bu DTO-Pojo dunusumunu yapan ayri bir class olusturulur ve o class i buraya injection yapariz.
    // Ama burada soyle bir sorun olusuyor bu donusumu yapan calss larin uzerine @Component annotation 'u koymamiz
    // gerekir. DTO Pojo donusumunu yapan birsuru classlar olusacak ve bunlarin uzerinde de bir suru
    // @Component olusacak. cok farkli yerlerde @Companent ler olusuyor. Bunlari bir yerde toplayabiliriz.
    // Classlarin uzerinde de olsa islemi SpringFramework yapiyor. Tek bir yerdde yapsakda islemi
    // Springframework yapiyor. Bunun icin payload in altindaki dto package nin icine ViceDeanDto Classini olusturuyoruz
    // bu classin icinde DTO- Pojo donusumunu yapan method yaziyoruz.

    //Donusumleri Ayri classalar uzerinden yapip buraya Injection yapabiliriz
    //
    private ViceDean createPojoFromDTO(ViceDeanRequest viceDeanRequest){

        // DTO - Pojo
        // Donusum yapan methodu burada cagiriyoruz
        // Donusum islemini yapan .dtoViceBean methodu ile gerekli @Configuration ve @Bean islemleri yapilmis olarak
        // viceDeanDto ' e git, parametre olarak verilen  viceDeanRequest 'i Pojo olan ViceDean ye cevir
        return viceDeanDto.dtoViceBean(viceDeanRequest);// Cok sukur artik DBye kayit edebilecegimiz
                                                        // bir Pojo ViceDean imiz var
        // Bu methodu artik yukarida kullanabiliriz.

    }

    // Donusumleri Ayri classalar uzerinden yapip buraya Injection yapabiliriz ama burada egitim amacli
    // bu sekilde de yapilabilecegi gosterildi. bu sekilde cok fazla code kalabaligi oluyor. bu class
    // karmasiklasiyor ve sisiyor..
    // Eger yanlis anlamadi isem bu methodlarda aslinda @Bean lari create etmis oluyoruz. @Bean lar ile ilgili
    // dersleri tekrar etmeliyim
    private ViceDeanResponse createViceDeanResponse(ViceDean viceDean) {

        return ViceDeanResponse.builder()
                .userId(viceDean.getId())
                .username(viceDean.getUsername())
                .name(viceDean.getName())
                .surname(viceDean.getSurname())
                .birthPlace(viceDean.getBirthPlace())
                .birthDay(viceDean.getBirthDay())
                .phoneNumber(viceDean.getPhoneNumber())
                .ssn(viceDean.getSsn())
                .gender(viceDean.getGender())
                .build();
    }

    // Not :  UpdateById() ********************************************************************
    public ResponseMessage<ViceDeanResponse> update(ViceDeanRequest newViceDean, Long managerId) {

        Optional<ViceDean> viceDean = viceDeanRepository.findById(managerId);
        // Icerisinde generic olarak <ViceDean> alan Optional yapidaki viceDean variable 'a; viceDeanRepository deki
        // .findById() methodu ile (managerId) yi bulup atiyoruz. Eger boyle bir kullanici bulunamadiysa, fieldlari null gelirse
        // bir hata status code ile bir Exception firlatmamiz lazim.

        //bu ici bos mu? ve Unique ligi check eden if statement lerin diger katmanlarda  ve diger rollerin
        // katmanlarinda da var oyleyse bu yapiyi utilsveya benzeri bir packagenin icinde bir class olusturup calss 'in
        // icinde generic bir method yazip katmanli yapilar lustururken aldigi degere gore ihtiyac halinde kullanabilir
        if(!viceDean.isPresent()) { // isPresent() bos mu ? kontrolunu yapiyor ve boolean olarak true donduruyor.
            // donen true degerini terslememiz gerekiyor yani fieldlar null donerse isPresent() true deger verecek
            // ve code if statement inin icine giremeyecek ve bizde bos donen degerler icin bir exception alamayacagiz
            // bu nedenle viceDean.isPresent() bosmu true gelmesi ihtimaline karsi (!isareti ile tersliyoruz) ve
            // asagidaki hatayi throw u aliyoruz.
            throw new ResourceNotFoundException(String.format(Messages.NOT_FOUND_USER2_MESSAGE,managerId));

        }else if(!CheckParameterUpdateMethod.checkParameter(viceDean.get(), newViceDean )) {
            // checkParameter() methodu boolean olarak true donecek olursa code bu if statement blogunun icine
            // girmeyecek. checkDuplicate methodunu calistirabilmek icin if in icinin false deger dondurmesi lazim
            // bu nedenle if in icindeki parametreyi (!) tersliyoruz. Eger checkParameter methodundan gelen deger
            // True ise yani de deki unique olamasi gereken deger requestten gelen Unique ola
            // masi gereken degerle eslesirse checkDuplicate methodu calissin eger degerler Unique degilse kullaniciya
            // exception mesaji dondursun.

            // Unique datalarda degisiklik var mi? karsilastiran methodu daha once
            // yazmistik(CheckParameterUpdateMethod.checkParameter). CheckParameterUpdateMethod Classinda
            // karsilastirmanin yapilmasi icin DB deki viceDean 'i get() methodu ile getirip parametre olarak veriyoruz
            // ve karsilastima icin requestten gelen yeni newViceDean 'ida parametre olarak veriyoruz
            // adminService.checkDuplicate(newViceDean.getUsername(), newViceDean.getSsn(), newViceDean.getPhoneNumber());
            // Elveda yukaridaki checkDuplicate hosgeldin yeni assagidaji checkDuplicate
            fieldControl.checkDuplicate(
                    newViceDean.getUsername(),
                    newViceDean.getSsn(),
                    newViceDean.getPhoneNumber()
            );
        }

        // Elimizde kullanicidan gelen Unique datala sahip bir newViceDean objesi var bu objenin verilerini Pojo dan
        // Json formattaki bir DTO ya cevirmemiz lazim. Bunun icin asagida Pojo-->DTO cevirisini yapan
        // createUpdatedViceDean adinda method yazip bu ceviri islemini yapan classi burada cagirip parametre olarak
        // managerId ile cagirdigimiz newViceDean objesini verip Json formatina cevrilmis haliyle ViceDean Type inda ki
        // updatedViceDean variable sine atiyoruz.
        ViceDean updatedViceDean = createUpdatedViceDean(newViceDean, managerId);

        // yeni bir obje olmamasina ragmen password un da updatade edilmis olmasi ihtimaline karsi password u
        // encode edip update edilmis userin DB deki password fiealdina setliyoruz.
        updatedViceDean.setPassword(passwordEncoder.encode(newViceDean.getPassword()));

        // createUpdatedViceDean methodunda userRoleType setlenmemisti onu burada setliyoruz. cunku kullanicinin kendisi
        // requestte kendi role typeni setleyemez bunu ancak manager i ve admini yapabilir. Db de hangi role type i
        // kayitli ise onu Pojo dan Json formata cevirip kullaniciya gonderebilmek icin setliyoruz.
        updatedViceDean.setUserRole(userRoleService.getUserRole(RoleType.ASSISTANTMANAGER));

        viceDeanRepository.save(updatedViceDean);//save() methodu ile update edilmis
        // Vice Deani (updatedViceDean) 'i viceDeanRepository 'a kaydet diyoruz.
        // !!! Tebrikler update islemi tamamlandi. simdi hemen asagidaki responseMessage ile kullabniciya ubdate islemi
        // ile ilgili status kodunu mesaji ve user i gonderebiliriz.

        return ResponseMessage.<ViceDeanResponse>builder()
                .message("Vice Dean Updated")
                .httpStatus(HttpStatus.CREATED)
                .object(createViceDeanResponse(updatedViceDean))
                .build();

    }

    // Pojo --> DTO Json formatina ceviren method.
    private ViceDean createUpdatedViceDean(ViceDeanRequest viceDeanRequest, Long managerId) {

        return ViceDean.builder()
                .id(managerId)
                // viceDeanRequest de Id olmadigi icin DB de bulunan managerId yi Json formata cevirmek icin
                // burada setliyoruz.

                .username(viceDeanRequest.getUsername())
                .ssn(viceDeanRequest.getSsn())
                .name(viceDeanRequest.getName())
                .surname(viceDeanRequest.getSurname())
                .birthPlace(viceDeanRequest.getBirthPlace())
                .birthDay(viceDeanRequest.getBirthDay())
                .phoneNumber(viceDeanRequest.getPhoneNumber())
                .gender(viceDeanRequest.getGender())
                .build();
    }


    // Not :  Delete() *************************************************************************
    public ResponseMessage<?> deleteViceDean(Long managerId) {

        // Delete edilecek kullanici DB de varmi diye id ile kontrol edilecek(Id li kullanici Db de varmi kontrol
        // etme islemini sik olarak kullaniyoruz. bunun icin yeni bir class olusturup orada generic bir
        // method yazilabilir.)
        Optional<ViceDean> viceDean = viceDeanRepository.findById(managerId);

        if(!viceDean.isPresent()) {
            throw new ResourceNotFoundException(String.format(Messages.NOT_FOUND_USER2_MESSAGE,managerId));
        }

        viceDeanRepository.deleteById(managerId); // viceDeanRepository nin Springframeworkundan gelen deleteById()
        // methodu na Db den gelen managerId yi kullanarak user i siliyoruz.

        //Kullaniciya islem ile ilgili bir messaj ve status gonderiyoruzz. obje silindigi icin objenin kendisini gondermiyoruz
        return ResponseMessage.builder()
                .message("Vice Dean Deleted")
                .httpStatus(HttpStatus.OK)
                .build();
    }


    // Not :  getById() ************************************************************************
    public ResponseMessage<ViceDeanResponse> getViceDeanById(Long managerId) {

        Optional<ViceDean> viceDean = viceDeanRepository.findById(managerId);

        if(!viceDean.isPresent()) {
            throw new ResourceNotFoundException(String.format(Messages.NOT_FOUND_USER2_MESSAGE,managerId));
        }

        return ResponseMessage.<ViceDeanResponse>builder()
                .message("Vice Dean Successfully Found")
                .httpStatus(HttpStatus.OK)
                .object(createViceDeanResponse(viceDean.get()))//viceDean Optional bir yapinin icinde oldugu icin direkt
                //bunun icin springFrameWork den gelen get() methodunu kullaniyoruz getiremiyoruz
                .build();

    }

    // Not :  getAll() *************************************************************************
    public List<ViceDeanResponse> getAllViceDean() {

        // Lambda
        // Bilgiler DB den gelecegi icin pojo olacak, oncelikle bunu DTO ya cevirip List yapida dondurecegiz
        return viceDeanRepository.findAll()
                .stream()
                //stream API ile bir akis olustur....
                .map(this::createViceDeanResponse)
                // viceDeanRepository den gelen pojo olan bu(this) akisi createViceDeanResponse dan Json yapiya
                // donusturerek akisa devam et
                .collect(Collectors.toList()); // Collector yapisi ile json yapiyi List haline cevir.
    }

    // Not :  getAllWithPage() ********************************************************************
    public Page<ViceDeanResponse> getAllWithPage(int page, int size, String sort, String type) {

        Pageable pageable = PageRequest.of(page,size, Sort.by(sort).ascending());
        if(Objects.equals(type,"desc")) {
            pageable = PageRequest.of(page,size,Sort.by(sort).descending());
        }

        return viceDeanRepository.findAll(pageable).map(this::createViceDeanResponse);
    }

    private void checkDeanExists(Long deanId){

    }
}