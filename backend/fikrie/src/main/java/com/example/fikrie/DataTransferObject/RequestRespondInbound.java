package com.example.fikrie.DataTransferObject;

import com.example.fikrie.Model.Inbound;
import com.example.fikrie.Model.Inventory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestRespondInbound {

    private int statusCode;
    private String reference;
    private Date dateReceived;
    private String productSku;
    private int quantity;
    private String location;
    private String remarks;
    private String message;
    private String error;
    private List<Inbound> listOfInbound;
}
