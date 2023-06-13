package com.schoolmanagement.payload.request;

import com.schoolmanagement.payload.request.abstracts.BaseUserRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TeacherRequest extends BaseUserRequest {
    // BaseUserRequest Classinda olmayan bazi user filedlarina burada TeacherRequest Classini kullanarak
    // ihtiyacimiz olan fieldlari burada Kullanicidan istiyoruz.

    @NotNull(message = "Please select Lesson") //Teacherin
    private Set<Long> lessonsIdList;
    // Teacherin girecegi derslerin id leri burda tutulacak. Id lerin Unique olabilmesi icin Set yapida, id nin
    // data type olan Long Data tipinde gelen parametreleri lessonsIdList variable inde tutuyoruz.
    // Admin veya baska bir yetkili Teacherin isim adres vs bilgilerini girerken bu Teacherin girecegi dersleride
    // setleyebilecek.

    @NotNull(message = "Please select isAdvisor Teacher")
    private boolean isAdvisorTeacher;// is keyword u ile baslayan yapilarda Lombok dan kaynakli olarak @Getter
    // anotationu duzgun calismiyor bu nedenle Boolean olarak yazdimiz data type i boolean olarak duzeltmemiz lazim
    // Bu Teacher rehber ogretmen mi degil mi bunu sorguluyoruz.

    @NotNull(message = "Please enter your email")
    @Email(message = "Please enter valid email")
    @Size(min=5, max=50 , message = "Your email should be between 5 and 50 chars")
    private String email;

}
