package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.model;

import java.time.ZonedDateTime;

public class Submission {

    private String name;
    private String email;
    private String phone;
    private String comments;

    private String idempotencyKey;
    private FormEnum form;
    private OrderFormSelectionEnum orderFormSelectionEnum;
    private OrganizationIdEnum organizationId;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastUpdatedDate;
    private Integer version;
    private Boolean isSpam;

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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public Boolean getSpam() {
        return isSpam;
    }

    public void setSpam(Boolean spam) {
        isSpam = spam;
    }
}
