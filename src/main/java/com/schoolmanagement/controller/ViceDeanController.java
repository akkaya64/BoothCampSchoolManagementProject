package com.schoolmanagement.controller;

import com.schoolmanagement.payload.request.ViceDeanRequest;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.payload.response.ViceDeanResponse;
import com.schoolmanagement.service.ViceDeanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("vicedean") //Base Mapping
@RequiredArgsConstructor
public class ViceDeanController { //Kullanicidan gelen Requestleri Controller Class lari karsilar

    //Injection Area
    private final ViceDeanService viceDeanService; // Controller Class lari direct Service Class katmanlari ile baglantili olur.
    //

    // Not :  Save() *************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")// ViceDean lari Admin ve Manager kayit islemi icin yetkilendiriyoruz
    @PostMapping("/save") // http://localhost:8080/vicedean/save

    // ViceDeanRequest 'in yani; kayit islemi basarili bir sekilde olusturulan user bilgisinin, kullaniciya islemin
    // yapildigina dair bir mesaj ve bir status code ile birlikte gonderilmesi gerekiyor. Kullanicinin kaydedilmesi icin
    // olusturdugu ViceDeanRequest verileri bize request in body sinden bir Json veri olarak gelmesi gerekiyor
    // Kullaniciya dondurmek istedigimiz ViceDeanRequest bize Request in body sinden gelecek bu nedenle requesti Json
    // bir data yan cevirmek icin @RequestBody annotation ile isaretliyoruz ve gelen verileri @Valid ile validation
    // isleminden geciriyoruz.
    // YANI  ViceDeanRequest DEN GELEN @RequestBody ANNOTATION u ILE JSON BIR YAPIYA CEVRILEN, @Valid ANNOTATION u ILE
    // VALODATION ISLEMINDEN GECEN viceDeanRequest OBJESINI <ViceDeanResponse> TURUNDE ResponseMessage OLARAK DONDUR
    // DEMIS OLUYORUZ
    public ResponseMessage<ViceDeanResponse> save(@RequestBody @Valid ViceDeanRequest viceDeanRequest) {
        //

        // @ResponseEntity ilede kullaniciya yaptigi isin sonucu ile String bir bilgi ve status kodunu gonderebilirdik,
        // verebilirdik ama PO veya Team Lead tarafindan bize gelen requirement nedeni ile kullaniciya bir obje de
        // dondurmemiz gerekiyor bu nedenle custom olarak kedimiz ResponseEntity den turettigimiz ResponseMessage 'i
        // kullaniyoruz. Aksi takdirde tum Rolelerin Controller Classlarin icine gidip bir suru code yazmak ve status
        // code larini da degirtirmek zorunda kalacaktik

        // Kullaniciya ResponseMessage turunde birazdan, responseMessage donen objelerini olusturdugumuz payload dosyasinin
        // cinde bulunan response package 'sinin icine ViceDeanResponse Classini dondurmesi icin <ViceDeanResponse>
        // parametre olarak veriyoruz.


        return viceDeanService.save(viceDeanRequest);
        // yukarida verdigimiz private final ViceDeanService viceDeanService; injection bagimliligini kullanarak
        // ViceDeanService Classina git, kullanicinin viceDeanRequest 'in  parent class 'i olan  BaseUserRequest 'i
        // kullanarak olusturdugu requesti, ViceDeanService Classinda buluna seve() methodunun icine koy demis oluyoruz

        // Bunu yapabilmek BaseUserRequest Classindan BaseUserRequest Classina bagimliligi azaltmak ve ileriye donuk
        // esnek bir yapi olusturabilmek icin BaseUserRequest parent Classindan extend edilen yardimci bir ara class
        // olusturyoruz

        //Service Class da ki save method 'una kullanicidan gelen bir role parametre si gondememiz lazim. Bunun icin
        //Payload package 'sinin altindaki request package 'sinin icine ViceDeanRequest Class 'i olusturuyoruz.



        //Service katina gidip Logical islemler ile save islemini tamamlayacagiz




    }

    // Not :  UpdateById() ********************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @PutMapping("/update/{userId}") // http://localhost:8080/vicedean/update/1
    public ResponseMessage<ViceDeanResponse> update(@RequestBody @Valid ViceDeanRequest viceDeanRequest
            ,@PathVariable Long userId){
        return viceDeanService.update(viceDeanRequest, userId);
    }

    // Not :  Delete() *************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @DeleteMapping("/delete/{userId}") // http://localhost:8080/vicedean/delete/1
    public ResponseMessage<?> delete(@PathVariable Long userId){

        return viceDeanService.deleteViceDean(userId);

    }

    // Not :  getById() ************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("/getViceDeanById/{userId}")  // http://localhost:8080/vicedean/getViceDeanById/1
    public ResponseMessage<ViceDeanResponse> getViceDeanById(@PathVariable Long userId) {

        return viceDeanService.getViceDeanById(userId);

    }

    // Not :  getAll() *************************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("/getAll")  // http://localhost:8080/vicedean/getAll
    public List<ViceDeanResponse> getAll(){
        return viceDeanService.getAllViceDean();
    }

    // Not :  getAllWithPage() ********************************************************************
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    @GetMapping("/search")
    public Page<ViceDeanResponse> getAllWithPage(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "sort") String sort,
            @RequestParam(value = "type") String type
    ) {

        return viceDeanService.getAllWithPage(page,size,sort,type);

    }

}

