package com.cedarmeadowmeats.orderworkflow.sendemailconfirmation.model;

import java.time.ZonedDateTime;

public class Submission {

    private String name;
    private String email;
    private String phone;
    private String comments;
    private String referral;
    private String eventDate;
    private String venue;
    private FormEnum form;
    private OrderFormSelectionEnum orderFormSelectionEnum;
    private OrganizationIdEnum organizationId;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastUpdatedDate;
    private Integer version;

    public Submission() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public OrderFormSelectionEnum getOrderFormSelectionEnum() {
        return orderFormSelectionEnum;
    }

    public void setOrderFormSelectionEnum(OrderFormSelectionEnum orderFormSelectionEnum) {
        this.orderFormSelectionEnum = orderFormSelectionEnum;
    }

    public OrganizationIdEnum getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(OrganizationIdEnum organizationId) {
        this.organizationId = organizationId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public FormEnum getForm() {
        return form;
    }

    public void setForm(FormEnum form) {
        this.form = form;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public ZonedDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(ZonedDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
