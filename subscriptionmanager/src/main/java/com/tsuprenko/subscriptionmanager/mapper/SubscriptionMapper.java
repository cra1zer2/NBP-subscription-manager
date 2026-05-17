package com.tsuprenko.subscriptionmanager.mapper;

import com.tsuprenko.subscriptionmanager.domain.Subscription;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionRequest;
import com.tsuprenko.subscriptionmanager.dto.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toResponse(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    Subscription toEntity(SubscriptionRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(SubscriptionRequest request, @MappingTarget Subscription subscription);
}
