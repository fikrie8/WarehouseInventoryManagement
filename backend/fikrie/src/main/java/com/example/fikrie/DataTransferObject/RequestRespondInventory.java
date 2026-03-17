package com.example.fikrie.DataTransferObject;

import com.example.fikrie.Model.Inventory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestRespondInventory {

    private int statusCode;
    private String category;
    private String sku;
    private String name;
    private String location;
    private int quantity;
    private String supplier;
    private String message;
    private String error;
    private Inventory inventory;
    private List<Inventory> listOfInventory;
}
