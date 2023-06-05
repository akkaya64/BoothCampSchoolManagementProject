package com.schoolmanagement.payload.response;

import com.schoolmanagement.payload.response.abstracts.BaseUserResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ViceDeanResponse extends BaseUserResponse {
}

// bir response olusturuken direct BaseUserResponse yi kullanmayip her role icin aslinda ayni ayni turde bir
// role response olusturmamizin sebebi ileride gelebilecek requirement 'in gereklilikleri karsilayabilmek amaci ile
// esnek bir yapi oluturabilmek icin BaseUserResponse Classinda yapacagimiz modifiyelerden bagimli olan tum roller ait
// service classlari etkilenebilir. bunu onlemek ve gerekli olan esnekligi yakalayabilmek icin ViceDeanResponse gibi
// DeanResponse gibi ara Class lar olusturulur. ilerde ViceDean icin responsal bir guncelleme updatedi gerektiginde bu
// guncellemeyi burada yapabiliriz
