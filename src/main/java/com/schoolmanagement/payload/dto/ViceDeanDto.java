package com.schoolmanagement.payload.dto;

import com.schoolmanagement.entity.concretes.ViceDean;
import com.schoolmanagement.payload.request.ViceDeanRequest;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class ViceDeanDto {

    // Bu method Pojo dondurecegi icin Return Type olarak ViceDean veriyoruz. adinida dtoViceBean yapiyoruz.
    // Burada artik pojo dunusumu olacagi icin kullanicini girdigi request 'i parametre olarak giriyoruz
    // (ViceDeanRequest viceDeanRequest).
    public ViceDean dtoViceBean(ViceDeanRequest viceDeanRequest){
     // ViceDeanRequest viceDeanRequest DTO  su, dtoViceBean classini kullanilarak Pojo olan ViceDean turune ceviriyor.


        return ViceDean.builder()
                .birthDay(viceDeanRequest.getBirthDay())
                .username(viceDeanRequest.getUsername()) // viceDeanRequest den gelen username i .getUsername()  ile
                // getirip username 'e setliyoruz. boylelikle burda DTO yu Pojo ya cevirmis oluyoruz
                // Diger tum fieldlara icin de bu viceDeanRequest den ki datayi .get() methodu ile getirip
                // setleme islemini yapiyoruz.
                .name(viceDeanRequest.getName())
                .surname(viceDeanRequest.getSurname())
                .password(viceDeanRequest.getPassword())// passwordu poja cevirdik ama bunu bu sekilde DB ye kayit
                // edemyiz Service classinda passwordu encode etmemiz lazim
                .ssn(viceDeanRequest.getSsn())
                .birthPlace(viceDeanRequest.getBirthPlace())
                .phoneNumber(viceDeanRequest.getPhoneNumber())
                .gender(viceDeanRequest.getGender())
                .build();


        // Burada guzel olan custum olarak hangi fieldlari setleyebilecegimiz secebiliyor olmamaiz, illa tum
        // fieldlar setlenecek diye birsey yok.

    }
}