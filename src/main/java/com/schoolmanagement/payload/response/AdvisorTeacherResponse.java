package com.schoolmanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AdvisorTeacherResponse {

    // AdvisorResponse AdvisorTeacher in validation suz hali. Cunku AdvisorTeacher DB ye kayit edilmeden once
    // Validation islemlerinden gecmisti kullaniciya response dondururken tekrar valide edilmesine gerek yok
    private Long advisorTeacherId;
    private String teacherName;
    private String teacherSSN;
    private String teacherSurname;
}