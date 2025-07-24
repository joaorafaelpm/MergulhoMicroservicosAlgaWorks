package com.algaworks.algadelivery.delivery.tracking.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
public class Item {
    @Id
    @EqualsAndHashCode.Include
    private UUID id ;
    private String name ;
    private Integer quantity ;

    @ManyToOne
    @Getter(AccessLevel.PRIVATE)
    private Delivery delivery ;

    static Item brandNew (String name , Integer quantity , Delivery delivery) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName(name);
        item.setQuantity(quantity);
        item.setDelivery(delivery);

        return item;
    }

}
