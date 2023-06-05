package com.schoolmanagement.payload.request;

import com.schoolmanagement.payload.request.abstracts.BaseUserRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ViceDeanRequest extends BaseUserRequest {
}

// BaseUserRequest Parent clasinin icindeki fieldlar bizim isimiz goruyor bu nedenle burada extra
// herhangi bir code yazmiyoruz
