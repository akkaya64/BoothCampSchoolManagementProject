package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.LessonProgram;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LessonProgramRepository extends JpaRepository<LessonProgram, Long> {
                                             // Merhaba Projeye yeni bir Ozellik eklmede yine birlikteyiz :) bu cok
    // heyecen verici. Bu interface yi olusturdugun icin eminim ki mutlu hissediyorsundur. Hadi bakalim ise once
    // LessonProgramRepository interfacesini i JpaRepository den extends etmektenn baslayalim, biliyorsun
    // JpaRepository ler generic yapilardir ve diamond <> icine entitty classi ve PK yaptigimiz unique bir deger
    // olan id nin data type ni yaziyoruz. aynen su sekilde: <LessonProgram, Long> . Artik LessonProgramRepository
    // Springframework un JpaRepository interfacesini kullanarak LessonProgram, Long ozelliginde bir repository
    // katmani olusturduk...
    // Simdi tam da bir Service katmani olusturmanin zamani


}