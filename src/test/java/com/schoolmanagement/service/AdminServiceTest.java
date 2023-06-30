package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.Admin;
import com.schoolmanagement.entity.concretes.UserRole;
import com.schoolmanagement.entity.enums.Gender;
import com.schoolmanagement.entity.enums.RoleType;
import com.schoolmanagement.payload.request.AdminRequest;
import com.schoolmanagement.payload.response.AdminResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.repository.*;
import com.schoolmanagement.utils.FieldControl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ViceDeanRepository viceDeanRepository;
    @Mock
    private DeanRepository deanRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private GuestUserRepository guestUserRepository;
    @Mock
    private FieldControl fieldControl;
    @Mock
    private UserRoleService userRoleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks// Yukarida Mock lanan nesneleri ozellestirilmis Mock olan AdminService otomatik olarak inject islemi
    // yapilmis oluyor
    private AdminService adminService;
    @Test
    void testSave_AdminSavedSuccessfully() {
        AdminRequest request = createAdminRequest();
        Admin admin = createAdmin();
        Admin savedAdmin = createAdmin();
        savedAdmin.setId(1L);

        UserRole adminRole = new UserRole(1, RoleType.ADMIN);
        doNothing().when(fieldControl).checkDuplicate(anyString(), anyString(), anyString());

        when(adminRepository.save(any(Admin.class))).thenReturn(savedAdmin);
        when(userRoleService.getUserRole(RoleType.ADMIN)).thenReturn(adminRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        //Davranis sekli belirleniyor......
        ResponseMessage<AdminResponse> reponse = adminService.save(request);

        assertNotNull(reponse);
        assertEquals("Admin saved", reponse.getMessage());
        assertEquals(HttpStatus.CREATED, reponse.getHttpStatus());
        assertNotNull(reponse.getObject());
        assertEquals(savedAdmin.getId(), reponse.getObject().getUserId());
        assertEquals(savedAdmin.getName(), reponse.getObject().getName());

        Mockito.verify(fieldControl,Mockito.times(1))
                .checkDuplicate(anyString(),anyString(),anyString());
        Mockito.verify(adminRepository, Mockito.times(1)).save(any(Admin.class));
        Mockito.verify(userRoleService, Mockito.times(1)).getUserRole(RoleType.ADMIN);
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(admin.getPassword());

    }
    private AdminRequest createAdminRequest(){
        AdminRequest request = new AdminRequest();
        request.setUsername("admin1");
        request.setName("Bob");
        request.setSurname("Marley");
        request.setPassword("12345678");
        request.setSsn("1234567890");
        request.setBirthDay(LocalDate.of(2000,03,30));
        request.setBirthPlace("Usak");
        request.setPhoneNumber("8756893456345");
        request.setGender(Gender.MALE);
        return request;

    }

    private Admin createAdmin(){
        Admin admin = new Admin();
        admin.setUsername("admin1");
        admin.setName("Bob");
        admin.setSurname("Marley");
        admin.setPassword("12345678");
        admin.setSsn("1234567890");
        admin.setBirthDay(LocalDate.of(2000,03,30));
        admin.setBirthPlace("Usak");
        admin.setPhoneNumber("8756893456345");
        admin.setGender(Gender.MALE);
        return admin;
    }

    @Test
    void getAllAdmin() {
        //Hazirlik:
        // .unpaged() ile yalanci bir Pagable yapi olusturuluyor
        Pageable pageable = Pageable.unpaged();
        Page<Admin> expectedPage= mock(Page.class);//  mock(Page.class) yalanci bir obje dondurecek

        when(adminRepository.findAll(pageable)).thenReturn(expectedPage);


        //!!! Aksiyon zamani
        Page<Admin> admins = adminService.getAllAdmin(pageable);

        assertSame(expectedPage,admins);
        verify(adminRepository, times(1)).findAll(pageable);



    }

    @Test
    void deleteAdmin_Successfully() {
        //Test dirasinda ihtiyacimiz olan degiskenleri olusturuyoruz
        Long id = 1L;
        Admin admin = new Admin();
        admin.setId(id);
        admin.setBuilt_in(false);

        // Burada delete methodunun calisip calismadigini testb edecegimiz icin delert methodunun icinde bulunan diger
        // methodlari test etmemize gerek yok. Bu nedenle o metgodlarin davranis seklini burada belirleyerek
        // bir anlamada mock lamis olacagiz, yani mis gibi yapacagiz
        // adminRepository e git(mis)  yukarida olusturdugummuz id yi findById(id) methoduna parametre olarak ver
        // sonra yukaridaki Optional yapidaki ornek admin objesini dondur.
        when(adminRepository.findById(id)).thenReturn(Optional.of(admin));

        // Artik aksiyon zamani hadi bakalim delete methodunu test etme zamani
        String resultMessage = adminService.deleteAdmin(id);
        assertEquals("Admin is deleted Successfully", resultMessage);
        // deleteAdmin() methodunun icindeki delete islemi yapan deleteBYId() methodunun da en az bir kere calistigini
        // kontrol etmemiz lazim.
        verify(adminRepository, times(1)).deleteById(id);
    }
}