package com.schoolmanagement.entity.concretes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Entity // ContactMessage DB ile iliskili bi class bu nedenle @Entity ile anotation yapiyoruz. DB de asagidaki
        // fieldlari ile birlikte bir tablo olusturacak
@Data // getter, setter, toString, hashcode and it also brings equals style method to check if two methods are equal
      // to each other.
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true) // It allows us to make changes by making a copy of the existing object instead of
                           // creating a new object.
public class ContactMessage implements Serializable {//


    // In this class, when an Anonymous user wants to ask something without registration, we need to get some
    // data from this user.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name ;
    @NotNull
    private String email ;
    @NotNull
    private String subject ;
    @NotNull
    private String message ;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")// There is a lot of unnecessary information in the
    // json output. we just format the data we want in a more beautiful way, only the date information will come.
    private LocalDate date;

    //ContactMessage entity class is ready. This entity class will have controller, service and repository layers

}
