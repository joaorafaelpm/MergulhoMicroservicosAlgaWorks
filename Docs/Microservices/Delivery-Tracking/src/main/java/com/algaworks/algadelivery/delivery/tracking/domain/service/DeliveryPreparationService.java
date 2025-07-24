package com.algaworks.algadelivery.delivery.tracking.domain.service;

import com.algaworks.algadelivery.delivery.tracking.api.domain.ContactPointInput;
import com.algaworks.algadelivery.delivery.tracking.api.domain.DeliveryInput;
import com.algaworks.algadelivery.delivery.tracking.api.domain.ItemInput;
import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import com.algaworks.algadelivery.delivery.tracking.domain.model.*;
import com.algaworks.algadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryPreparationService {

    private final DeliveryRepository deliveryRepository;

    private final DeliveryTimeEstimationService deliveryTimeEstimationService;
    private final CourierPayoutCalculationService courierPayoutCalculationService;

    @Transactional
    public Delivery draft (DeliveryInput input) {
        Delivery delivery = Delivery.draft();

        handlePreparation(input , delivery);


        return deliveryRepository.saveAndFlush(delivery);

    }

    @Transactional
    public Delivery edit(UUID deliveryId , DeliveryInput input) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new DomainException("Não encontrado")
        );
        delivery.removeItems();

        handlePreparation(input , delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    private void handlePreparation(DeliveryInput input, Delivery delivery) {
        ContactPointInput senderInput = input.getSender();
        ContactPointInput recipientInput = input.getRecipient();

        ContactPoint sender = ContactPoint.builder()
                .phone(senderInput.getPhone())
                .name(senderInput.getName())
                .complements(senderInput.getComplement())
                .zipCode(senderInput.getZipCode())
                .street(senderInput.getStreet())
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .phone(recipientInput.getPhone())
                .name(recipientInput.getName())
                .complements(recipientInput.getComplement())
                .zipCode(recipientInput.getZipCode())
                .street(recipientInput.getStreet())
                .build();

        DeliveryEstimate estimate = deliveryTimeEstimationService.estimate(sender, recipient);
        BigDecimal calculatePayout = courierPayoutCalculationService.calculatePayout(estimate.getDistanceInKm());

        BigDecimal distanceFee = calculateFee(estimate , estimate.getDistanceInKm());

        var preparationDetails = Delivery.PrepatarionDetails.builder()
                .recipient(recipient)
                .sender(sender)
                .expectedDeliveryTime(estimate.getEstimatedTime())
                .courierPayout(calculatePayout)
                .distanceFee(distanceFee)
                .build();



        delivery.editPreparationDetails(preparationDetails);

        for (ItemInput item : input.getItems()) {
            delivery.addItem(item.getName() , item.getQuantity()) ;
        }
    }

    private BigDecimal calculateFee(DeliveryEstimate estimate, Double distanceInKm) {
        return new BigDecimal("3")
                .multiply(new BigDecimal(distanceInKm))
                .setScale(2 , RoundingMode.HALF_EVEN);

    }


}
