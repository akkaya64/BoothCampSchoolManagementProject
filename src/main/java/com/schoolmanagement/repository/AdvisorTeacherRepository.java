package com.schoolmanagement.repository;

import com.schoolmanagement.entity.concretes.AdvisorTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvisorTeacherRepository extends JpaRepository<AdvisorTeacher, Long> {
    Optional<AdvisorTeacher> getAdvisorTeacherByTeacher_Id(Long advisorTeacherId); // default olarak gelen id nin
    // adini(Long id) daha belirleyici olmasi icin (Long advisorTeacherId) olarak duzeltiyoruz... Bazen otomatik create
    // islemlerinde donen deger void olarak gelebiliyor bu durumda bu yapi Optional bir yapi dondurecegi icin methodun
    // donen degerini manuel olarak yukaridaki gibi manuel olarak duzeltiyoruz Optional bir yapi oldugu icinde
    // parametre olarak pojo olan AdvisorTeacher i veriyoruz
}
