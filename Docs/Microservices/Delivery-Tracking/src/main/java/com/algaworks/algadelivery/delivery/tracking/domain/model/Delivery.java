package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.event.DeliveryFulfilledEvent;
import com.algaworks.algadelivery.delivery.tracking.domain.event.DeliveryPickedUpEvent;
import com.algaworks.algadelivery.delivery.tracking.domain.event.DeliveryPlacedEvent;
import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true , callSuper = false)
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
public class Delivery extends AbstractAggregateRoot<Delivery> {

    @EqualsAndHashCode.Include
    @Id
    private UUID id ;
    private UUID courierId;

    private OffsetDateTime placedAt ;
    private OffsetDateTime assignedAt ;
    private OffsetDateTime expectedDeliveryAt ;
    private OffsetDateTime fulfilledAt ;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItens ;

    private DeliveryStatus status ;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "sender_zip_code")),
            @AttributeOverride(name = "street", column = @Column(name = "sender_street")),
            @AttributeOverride(name = "number", column = @Column(name = "sender_number")),
            @AttributeOverride(name = "complements", column = @Column(name = "sender_complements")),
            @AttributeOverride(name = "name", column = @Column(name = "sender_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "sender_phone"))
    })
    private ContactPoint sender ;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "recipient_zip_code")),
            @AttributeOverride(name = "street", column = @Column(name = "recipient_street")),
            @AttributeOverride(name = "number", column = @Column(name = "recipient_number")),
            @AttributeOverride(name = "complements", column = @Column(name = "recipient_complements")),
            @AttributeOverride(name = "name", column = @Column(name = "recipient_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "recipient_phone"))
    })
    private ContactPoint recipient ;

    @OneToMany(cascade = CascadeType.ALL , orphanRemoval = true , mappedBy = "delivery")
    private List<Item> itens = new ArrayList<>();

    public static Delivery draft() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItens(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);

        return delivery ;
    }

    public UUID addItem (String name , int quantity) {
        Item item = Item.brandNew(name , quantity , this);
        itens.add(item);
        calculateTotalItens();
        return item.getId();
    }

    public void removeItem (UUID itemId) {
        itens.removeIf(item -> item.getId().equals(itemId));
    }

    public void removeItems () {
        itens.clear();
        calculateTotalItens();
    }

    public void editPreparationDetails (PrepatarionDetails details) {
        verifyIfCanBeEdited();

        setSender(details.getSender());
        setRecipient(details.getRecipient());
        setDistanceFee(details.getDistanceFee());
        setCourierPayout(details.getCourierPayout());
        setExpectedDeliveryAt(OffsetDateTime.now()
                .plus(details.getExpectedDeliveryTime()));
        setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }


    public void place() {
        verifyIfCanBePlaced();
        this.changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
        super.registerEvent(
                new DeliveryPlacedEvent(this.getPlacedAt() , this.getId())
        );
    }

    public void pickUp (UUID courierId) {
        this.setCourierId(courierId);
        this.changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
        super.registerEvent(
                new DeliveryPickedUpEvent(this.getPlacedAt() , this.getId())
        );
    }

    public void markAsDelivered(){
        this.changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetDateTime.now());
        super.registerEvent(
                new DeliveryFulfilledEvent(this.getPlacedAt() , this.getId())
        );
    }

    public List<Item> getItens () {
        return Collections.unmodifiableList(this.itens);
    }

    private void calculateTotalItens() {
        int totalItems = itens.stream().mapToInt(Item::getQuantity).sum();
        setTotalItens(totalItems);
    }

    private void verifyIfCanBePlaced() {
        if (!isFilled()) {
            throw new DomainException("");
        }
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException("");
        }
    }

    private void verifyIfCanBeEdited () {
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException("");
        }
    }

    private boolean isFilled() {
        return this.getSender() != null
                && this.getRecipient() != null
                && this.totalCost != null ;
    }

    private void changeStatusTo (DeliveryStatus newStatus) {
        if (newStatus != null && this.getStatus().canNotChangeTo(newStatus)) {
            throw new DomainException(
                    "Invalid status transition from " + this.getStatus() +
                        " to " + newStatus
            );
        }
        this.setStatus(newStatus);
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PrepatarionDetails {
        private ContactPoint sender ;
        private ContactPoint recipient ;
        private BigDecimal distanceFee ;
        private BigDecimal courierPayout ;
        private Duration expectedDeliveryTime;
    }

}
