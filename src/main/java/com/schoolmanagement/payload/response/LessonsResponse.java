package com.schoolmanagement.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class LessonsResponse {
    // Burada Entity Lesson nun @Valid valitation  olmamis hali olacak. Cunku uygulamanin performansi olumsuz
    // etkilenmesin diye bu bilgiler DB ye giderken Valide edildi tekrar DB den gelen bilgileri valide etmeye gerek yok.

    private Long lessonId; // Best Practice: Id olusturulurken ismini sadece id yazmak yerine belirleyici bir id ismi
                           // berilmeli mesela lessonId gibi
    private String lessonName;
    private int creditScore;
    private boolean isCompulsory;// zorunlu mu?
}
