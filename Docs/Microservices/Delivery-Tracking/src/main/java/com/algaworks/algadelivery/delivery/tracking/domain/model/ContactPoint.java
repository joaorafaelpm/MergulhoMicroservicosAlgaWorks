package com.algaworks.algadelivery.delivery.tracking.domain.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ContactPoint {

    private String zipCode ;
    private String street ;
    private String number ;
    private String complements ;
    private String name ;
    private String phone ;

}
