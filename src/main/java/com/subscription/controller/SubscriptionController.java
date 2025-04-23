package com.subscription.controller;

import com.subscription.model.request.SubscriptionRequest;
import com.subscription.model.response.SubscriptionResponse;
import com.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription management APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Create a new subscription")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscription(request));
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "Get subscription by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription found"),
        @ApiResponse(responseCode = "404", description = "Subscription not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SubscriptionResponse> getSubscription(
        @Parameter(description = "ID of the subscription to retrieve") 
        @PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getSubscription(subscriptionId));
    }

    @PostMapping("/{subscriptionId}/cancel")
    @Operation(summary = "Cancel a subscription")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Subscription not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
        @Parameter(description = "ID of the subscription to cancel") 
        @PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(subscriptionId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all subscriptions for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscriptions found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(
        @Parameter(description = "ID of the user") 
        @PathVariable UUID userId) {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(userId));
    }
} 