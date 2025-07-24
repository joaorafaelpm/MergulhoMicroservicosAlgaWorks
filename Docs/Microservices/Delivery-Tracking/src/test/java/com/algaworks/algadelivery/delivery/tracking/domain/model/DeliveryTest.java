package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
class DeliveryTest {

    @Test
    public void shouldChangeToPlaced() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());

        delivery.place();

        assertEquals(DeliveryStatus.WAITING_FOR_COURIER , delivery.getStatus());
        assertNotNull(delivery.getPlacedAt());
    }

    @Test
    public void shouldNotPlace () {
        Delivery delivery = Delivery.draft();

        delivery.place();

        assertThrows(DomainException.class , () -> delivery.place());
        assertEquals(DeliveryStatus.DRAFT , delivery.getStatus());
        assertNull(delivery.getPlacedAt());
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