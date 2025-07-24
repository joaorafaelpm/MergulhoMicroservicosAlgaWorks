package com.algaworks.algadelivery.delivery.tracking.domain.repository;

import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeliveryRepositoryTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    public void shouldPersist() {
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(createdValidPreparationDetails());

        delivery.addItem("Notebook" , 2);
        delivery.addItem("Teclado" , 5);

        deliveryRepository.saveAndFlush(delivery);

        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId())
                .orElseThrow();

        assertEquals(2 , persistedDelivery.getItens().size());

    }

    private Delivery.PrepatarionDetails createdValidPreparationDetails () {
        ContactPoint sender = ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua da desolação e do desespero")
                .number("100")
                .complements("Ronaldo")
                .name("João Lá ele")
                .phone("(11) 900000-1023")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("23123-122")
                .street("Rua da Felicidade e alegria")
                .number("112")
                .complements("")
                .name("Ronaldo")
                .phone("(11) 93232-1011")
                .build();

        return Delivery.PrepatarionDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("19.90"))
                .courierPayout(new BigDecimal("9.00"))
                .expectedDeliveryTime(Duration.ofMinutes(10))
                .build();
    }

}