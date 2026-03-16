package com.example.fikrie.DataTransferObject;

import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Model.Outbound;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestRespondOutbound {
    private int statusCode;
    private String reference;
    private Date dateShipped;
    private String productSku;
    private int quantity;
    private String destination;
    private String remarks;
    private String message;
    private String error;
    private Inventory inventory;
    private List<Outbound> ListOfOutbound;
}
