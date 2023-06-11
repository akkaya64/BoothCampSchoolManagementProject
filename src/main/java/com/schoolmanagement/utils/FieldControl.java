package com.schoolmanagement.utils;

import com.schoolmanagement.exception.ConflictException;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component // FieldControlleri diger classlara DI yapabilmek icin
@RequiredArgsConstructor // bu iki annotation ile bu classi annotation ediyoruz
public class FieldControl {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final ViceDeanRepository viceDeanRepository;
    private final DeanRepository deanRepository;
    private final TeacherRepository teacherRepository;
    private final GuestUserRepository guestUserRepository;

    // Daha once Dependency Injection(DI) siyonlari method icinde yapmistik boyle oldugunda Hangi class seviyesinde
    // olursa olsun o classlarda bu DI siyonlari tekrar yapacaktik. Bu nedenle Bu DI yonlari burada yukarida oldugu
    // gibi Class seviyesinde yapiyoruz ki tekrar tekrar diger Classl arda DI yapmayalim.

//    public static void checkDuplicate(//AdminRepository adminRepository,
//                                      DeanRepository deanRepository,
//                                      StudentRepository studentRepository,
//                                      TeacherRepository teacherRepository,
//                                      GuestUserRepository guestUserRepository,
//                                      ViceDeanRepository viceDeanRepository,
//                                     String... values)

    // Artik duplacate methodunu asagidaki gibi basit simple bir sekilde yazabiliriz ve artik methodumuz static Scope
    // olmadigi icin AdminService Classinda yaptigimiz checkDuplicate islemi artik calismayacaktir bu nedenle
    // AdminService Katmanina gidip gerekli duzenlemelri yapmaliyiz.
    public void checkDuplicate(String... values){
        String username = values[0];
        String ssn = values[1];
        String phone = values[2];
        String email = "";

        if (values.length == 4) {
            email = values[3];
        }

        if (adminRepository.existsByUsername(username) || deanRepository.existsByUsername(username) ||
                studentRepository.existsByUsername(username) || teacherRepository.existsByUsername(username) ||
                viceDeanRepository.existsByUsername(username) || guestUserRepository.existsByUsername(username)) {
            throw new ConflictException(String.format(Messages.ALREADY_REGISTER_MESSAGE_USERNAME, username));
        } else if (adminRepository.existsBySsn(ssn) || deanRepository.existsBySsn(ssn) ||
                studentRepository.existsBySsn(ssn) || teacherRepository.existsBySsn(ssn) ||
                viceDeanRepository.existsBySsn(ssn) || guestUserRepository.existsBySsn(ssn)) {
            throw new ConflictException(String.format(Messages.ALREADY_REGISTER_MESSAGE_SSN, ssn));
        } else if (adminRepository.existsByPhoneNumber(phone) || deanRepository.existsByPhoneNumber(phone) ||
                studentRepository.existsByPhoneNumber(phone) || teacherRepository.existsByPhoneNumber(phone) ||
                viceDeanRepository.existsByPhoneNumber(phone) || guestUserRepository.existsByPhoneNumber(phone)) {
            throw new ConflictException(String.format(Messages.ALREADY_REGISTER_MESSAGE_PHONE_NUMBER, phone));
        }else if (studentRepository.existsByEmail(email) || teacherRepository.existsByEmail(email)) {
            throw new ConflictException(String.format(Messages.ALREADY_REGISTER_MESSAGE_SSN, email));
        }
    }
}
