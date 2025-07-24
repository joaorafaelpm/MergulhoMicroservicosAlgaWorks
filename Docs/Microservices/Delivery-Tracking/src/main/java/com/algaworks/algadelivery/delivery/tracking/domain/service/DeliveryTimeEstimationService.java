package com.algaworks.algadelivery.delivery.tracking.domain.service;

import com.algaworks.algadelivery.delivery.tracking.api.domain.ContactPointInput;
import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.algaworks.algadelivery.delivery.tracking.domain.model.DeliveryEstimate;

public interface DeliveryTimeEstimationService {

    DeliveryEstimate estimate (ContactPoint sender , ContactPoint receiver) ;

}
