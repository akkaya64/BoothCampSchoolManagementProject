package com.schoolmanagement.service;

import com.schoolmanagement.entity.concretes.EducationTerm;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.payload.request.EducationTermRequest;
import com.schoolmanagement.payload.response.EducationTermResponse;
import com.schoolmanagement.payload.response.ResponseMessage;
import com.schoolmanagement.repository.EducationTermRepository;
import com.schoolmanagement.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationTermService {

    private final EducationTermRepository educationTermRepository;

    // Not :  Save() *************************************************************************
    public ResponseMessage<EducationTermResponse> save(EducationTermRequest request) {

        //!!! son kayiot tarihi , ders doneminin baslangic tarihinde nsonra olmamali :
        checkEducationTermDate(request);//Asagidaki c=
        /*
        if (request.getLastRegistrationDate().isAfter(request.getStartDate())) {
            throw new ResourceNotFoundException(Messages.EDUCATION_START_DATE_IS_EARLIER_THAN_LAST_REGISTRATION_DATE);
        }

        //!!! bitis tarigi baslangic tarihinden once olmamali
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResourceNotFoundException(Messages.EDUCATION_END_DATE_IS_EARLIER_THAN_START_DATE);
        }

        // !!! ayni term ve baslangic tarihine sahip birden fazla kayit var mi kontrolu
        if (educationTermRepository.existsByTermAndYear(request.getTerm(), request.getStartDate().getYear())) {
            throw new ResourceNotFoundException(Messages.EDUCATION_TERM_IS_ALREADY_EXIST_BY_TERM_AND_YEAR_MESSAGE);
        }

         */

        // !!! save metoduna dto- pojo donusumu yapip gonderiyoruz
        EducationTerm savedEducationTerm = educationTermRepository.save(createEducationTerm(request));

        // !!! response objesi olusturuluyor
        return ResponseMessage.<EducationTermResponse>builder()
                .message("Education Term created")
                .object(createEducationTermResponse(savedEducationTerm))
                .httpStatus(HttpStatus.CREATED)
                .build();

    }



    private EducationTerm createEducationTerm(EducationTermRequest request) {

        return EducationTerm.builder()
                .term(request.getTerm())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .lastRegistrationDate(request.getLastRegistrationDate())
                .build();
    }

    private EducationTermResponse createEducationTermResponse(EducationTerm response) {

        return EducationTermResponse.builder()
                .id(response.getId())
                .term(response.getTerm())
                .startDate(response.getStartDate())
                .endDate(response.getEndDate())
                .lastRegistrationDate(response.getLastRegistrationDate())
                .build();

    }

    // Not :  getById() ************************************************************************
    public EducationTermResponse get(Long id) {

        // !!! ya yoksa kontrolu
        if (!educationTermRepository.existsByIdEquals(id)) { // existsById de calisir
            throw new ResourceNotFoundException(String.format(Messages.EDUCATION_TERM_NOT_FOUND_MESSAGE, id));
        }

        // !!! POJO - DTO donusumu ile response hazirlaniyor
        return createEducationTermResponse(educationTermRepository.findByIdEquals(id)); // findById de calisir
    }

    // Not :  getAll() *************************************************************************
    public List<EducationTermResponse> getAll() {

        return educationTermRepository.findAll()
                .stream()
                .map(this::createEducationTermResponse)
                .collect(Collectors.toList());
    }

    // Not :  getAllWithPage() ******************************************************************
    public Page<EducationTermResponse> getAllWithPage(int page, int size, String sort, String type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        if (Objects.equals(type, "desc")) {
            pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        }

        return educationTermRepository.findAll(pageable).map(this::createEducationTermResponse);
    }

    // Not :  Delete() *************************************************************************
    public ResponseMessage<?> delete(Long id) {


        if (!educationTermRepository.existsById(id)) {//existsByIdEquals; SpringBoothJPA nin hazir saglamis oldugu
            // methodlardan. repository de code yazmamaiza gerek yok
            throw new ResourceNotFoundException(String.format(Messages.EDUCATION_TERM_NOT_FOUND_MESSAGE, id));
        }

        educationTermRepository.deleteById(id);

        return ResponseMessage.builder()
                .message("Education Term Deleted")
                .httpStatus(HttpStatus.OK)
                .build();


    }

    // Not :  UpdateById() ********************************************************************
    public ResponseMessage<EducationTermResponse> update(Long id, EducationTermRequest request) {

        // !!! id kontrolu
        checkEducationTermExists(id);
        /*      if(!educationTermRepository.existsById(id)){
              throw new ResourceNotFoundException(String.format(Messages.EDUCATION_TERM_NOT_FOUND_MESSAGE,id));
        }
        */ // Id kontrolu Eski versiyonu

        // !!! getStartDate ve lastRegistrationDate kontrolu
        if(request.getStartDate()!=null && request.getLastRegistrationDate()!=null) {
            if(request.getLastRegistrationDate().isAfter(request.getStartDate())) {
                throw new ResourceNotFoundException(Messages.EDUCATION_START_DATE_IS_EARLIER_THAN_LAST_REGISTRATION_DATE);
            }
        }

        // !!! startDate-endDate kontrolu
        if(request.getStartDate()!= null && request.getEndDate()!=null){
            if(request.getEndDate().isBefore(request.getStartDate())){
                throw new ResourceNotFoundException(Messages.EDUCATION_END_DATE_IS_EARLIER_THAN_START_DATE);
            }
        }

        ResponseMessage.ResponseMessageBuilder<EducationTermResponse> responseMessageBuilder =
                ResponseMessage.builder();

        EducationTerm updated = createUpdatedEducationTerm(id,request);
        educationTermRepository.save(updated);

        return responseMessageBuilder
                .object(createEducationTermResponse(updated))
                .message("Education Term Updated Successfully")
                .build();

    }

    private EducationTerm createUpdatedEducationTerm(Long id, EducationTermRequest request) {
        return EducationTerm.builder()
                .id(id)
                .term(request.getTerm())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .lastRegistrationDate(request.getLastRegistrationDate())
                .build();
    }

    // Not :  getById() ********************************************************************
    public EducationTerm getById(Long educationTermId) {

        checkEducationTermExists(educationTermId);

        return educationTermRepository.findByIdEquals(educationTermId);
    }


        /*
        // !!! id kontrolu
        if (!educationTermRepository.existsById(id)) {
            throw new ResourceNotFoundException(String.format(Messages.EDUCATION_TERM_NOT_FOUND_MESSAGE, id));
        }

        // !!! getStartDate ve lastRegistrationDate kontrolu
        if(request.getStartDate()!=null && request.getLastRegistrationDate()!=null) {
            //Education term in baslangic tarihi ve son kayit tarihi null degilse ise iceri gir
            if (request.getLastRegistrationDate().isAfter(request.getStartDate())){
                // Ve Eger Last Registration Date,  Start Date den sonra girildiyse exception firlat
                throw new ResourceNotFoundException(Messages.EDUCATION_START_DATE_IS_EARLIER_THAN_LAST_REGISTRATION_DATE);
            }

        }


        // !!! startDate-endDate kontrolu, Start Date End Date nin oncesinde omamasi lazim.
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())){
                throw new ResourceNotFoundException(Messages.EDUCATION_END_DATE_IS_EARLIER_THAN_START_DATE);
            }
        }


        ResponseMessage.ResponseMessageBuilder<EducationTermResponse> responseMessageBuilder =
                ResponseMessage.builder();

// kendi yazdigim return code blugu
        // code blogu daha clean
//        return ResponseMessage.<EducationTermResponse>builder()
//                .message("Education Term Updated")
//                .object(createEducationTermResponse(updatedEduTerm))
//                .httpStatus(HttpStatus.CREATED)
//                .build();

        // bu ResponseMessage dondurme yontemi best practice degil egitim amacli buraya yazildi.
        // Fieldlari setlenmeden once ici bos bir obje olusturuluyor.
        EducationTerm updatedEduTerm = createUpdateEduTerm(request, id);

        educationTermRepository.save(updatedEduTerm);

        return responseMessageBuilder
                .object(createEducationTermResponse(updatedEduTerm))
                .message("Education Term Updated")
                .build();

    }

    // Kullanicidan gelen yeni DTO education term datasi, icinde id olan bir pojo veri turune cevrildi
    private EducationTerm createUpdateEduTerm(EducationTermRequest educationTermRequest, Long id) {
        return EducationTerm.builder()
                .id(id)
                .term(educationTermRequest.getTerm())
                .startDate(educationTermRequest.getStartDate())
                .endDate(educationTermRequest.getEndDate())
                .lastRegistrationDate(educationTermRequest.getLastRegistrationDate())
                .build();
    }

 */

    //Not:  ---> EDUCATION-TERM-SERVICE <---

    //!!! ODEV-1 : ya yoksa kontrolleri method uzerinden cagrilmali
    private void checkEducationTermExists(Long id) {
        if (!educationTermRepository.existsByIdEquals(id)) {
            throw new ResourceNotFoundException(String.format(Messages.EDUCATION_TERM_NOT_FOUND_MESSAGE, id));
        }
    }
    //!!! ODEV-2 : save methodundaki tarih kontrolleri ayri bir method uzerinden cagrilmali
    private void checkEducationTermDate(EducationTermRequest request){
        //!!! son kayit tarihi , ders doneminin baslangic tarihinden sonra olmamali :

        if(request.getLastRegistrationDate().isAfter(request.getStartDate())) {
            throw new ResourceNotFoundException(Messages.EDUCATION_START_DATE_IS_EARLIER_THAN_LAST_REGISTRATION_DATE);
        }

        //!!! bitis tarigi baslangic tarihinden once olmamali
        if(request.getEndDate().isBefore(request.getStartDate())){
            throw  new ResourceNotFoundException(Messages.EDUCATION_END_DATE_IS_EARLIER_THAN_START_DATE);
        }

        // !!! ayni term ve baslangic tarihine sahip birden fazla kayit var mi kontrolu
        if(educationTermRepository.existsByTermAndYear(request.getTerm(), request.getStartDate().getYear())) {
            throw  new ResourceNotFoundException(Messages.EDUCATION_TERM_IS_ALREADY_EXIST_BY_TERM_AND_YEAR_MESSAGE);
        }
    }
}