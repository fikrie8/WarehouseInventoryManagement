package com.example.fikrie.Service;

import com.example.fikrie.DataTransferObject.InventoryDTO;
import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Exception.ResourceNotFoundException;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Repository.InventoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    public RequestRespondInventory getInventory(String requestInventoryName) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        Inventory inventory = inventoryRepo.findByName(requestInventoryName)
                .orElseThrow(() -> new ResourceNotFoundException("getInventory failed: Inventory not found with name: " + requestInventoryName));
        requestRespondInventory.setInventory(inventory);
        requestRespondInventory.setStatusCode(200);
        requestRespondInventory.setMessage("Inventory with name : " + requestInventoryName + " found successfully");

        return requestRespondInventory;
    }

    public RequestRespondInventory register(RequestRespondInventory inventoryRegistrationRequest) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try {
            Inventory inventory = new Inventory();
            inventory.setCategory(inventoryRegistrationRequest.getCategory());
            inventory.setSku(inventoryRegistrationRequest.getSku());
            inventory.setName(inventoryRegistrationRequest.getName());
            inventory.setLocation(inventoryRegistrationRequest.getLocation());
            inventory.setQuantity(inventoryRegistrationRequest.getQuantity());
            inventory.setSupplier(inventoryRegistrationRequest.getSupplier());
            Inventory inventoryRegistered = inventoryRepo.save(inventory);
            if(inventoryRegistered.getId() > 0) {
                requestRespondInventory.setInventory(inventoryRegistered);
                requestRespondInventory.setStatusCode(200);
                requestRespondInventory.setMessage("Inventory has been registered successfully");
    @Transactional
    public RequestRespondInventory bulkRegister(MultipartFile file, int header) {
        List<Inventory> inventoryList = new ArrayList<>();
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            //getSheetAt(i) means reading the file from the (i) tab. not the (i) column/row.
            Sheet sheet = workbook.getSheetAt(0);
            for(Row row : sheet) {
                //There are possibilities the excel is not standardized. The starting row might be
                // different for every file. To skip any title/empty lines inside the excel sheet
                if(row.getRowNum() <= header) {
                    continue;
                }
                String sku = getCleanCellValue(row.getCell(1));
                String itemName = getCleanCellValue(row.getCell(3));
                if(sku.isEmpty() || itemName.isEmpty()) {
                    continue;
                }
                //Since we have standardized the column index(0 = No, 1 = SKU Code, 2 = Category, ...),
                //The assignment value from row.getCell(i) to the Inventory should follow that.
                Inventory inventory = new Inventory();
                inventory.setSku(getCleanCellValue(row.getCell(1)));
                inventory.setCategory(getCleanCellValue(row.getCell(2)));
                inventory.setName(getCleanCellValue(row.getCell(3)));
                inventory.setLocation(getCleanCellValue(row.getCell(4)));
                inventory.setQuantity(Integer.parseInt(getCleanCellValue(row.getCell(5))));
                inventory.setSupplier(getCleanCellValue(row.getCell(6)));
                inventoryList.add(inventory);
            }
            if(!inventoryList.isEmpty()) {
                inventoryRepo.saveAll(inventoryList);
                requestRespondInventory.setStatusCode(200);
                requestRespondInventory.setMessage("Inventory batch has been registered successfully");
            }
        } catch (Exception e) {
            log.error("BulkRegister failed for file: " + file.getOriginalFilename() + ", : " + e);
            throw new DataIntegrityViolationException("BulkRegister failed: due to file parsing or data constraints: " + e.getMessage());
        }
        return requestRespondInventory;
    }

    //The getCleanCellValue method is a custom helper utility.
    // It checks if the cell contains any string, number, or a boolean, converts it into a clean String, then trims off any accidental spaces
    // This is to make sure Java doesn't throw any NullPointerException.
    private String getCleanCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue); // Drops trailing decimals from whole numbers (e.g., "1.0" -> "1")
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    @Transactional
    public RequestRespondInventory updateInventory(String sku, Inventory requestUpdateInventory) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();

            Inventory inventoryInDb = inventoryRepo.findBySku(sku)
                    .orElseThrow(() -> new ResourceNotFoundException("updateInventory failed: Inventory not found by SKU: " + sku));

            Inventory updatedinventory = getInventoryFields(requestUpdateInventory, inventoryInDb);
            Inventory savedInventory = inventoryRepo.save(updatedinventory);
            requestRespondInventory.setInventory(savedInventory);
            requestRespondInventory.setStatusCode(200);
            requestRespondInventory.setMessage("Inventory updated successfully");

        return requestRespondInventory;
    }

    @Transactional
    public RequestRespondInventory deleteInventory(Integer requestInventoryById) {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        Inventory inventory = inventoryRepo.findById(requestInventoryById)
                .orElseThrow(() -> new ResourceNotFoundException("deleteInventory failed: Inventory not found by Id: " + requestInventoryById));
        inventoryRepo.deleteById(requestInventoryById);
        requestRespondInventory.setStatusCode(200);
        requestRespondInventory.setMessage("Inventory successfully deleted");

        return requestRespondInventory;
    }

    public RequestRespondInventory getAllInventory() {
        RequestRespondInventory requestRespondInventory = new RequestRespondInventory();
        List<InventoryDTO> listOfInventoryDTO = inventoryRepo.findAllBy();
        if(listOfInventoryDTO.isEmpty()) {
           throw new ResourceNotFoundException("getAllInventory failed: No inventory records is found in the system.");
        }
        requestRespondInventory.setListOfInventoryDTO(listOfInventoryDTO);
        requestRespondInventory.setStatusCode(200);
        requestRespondInventory.setMessage("Inventory updated successfully");

        return requestRespondInventory;
    }

    private Inventory getInventoryFields(Inventory requestUpdateInventory, Inventory inventory) {
        if(Objects.nonNull(requestUpdateInventory.getCategory()) && !requestUpdateInventory.getCategory().isEmpty()) {
            inventory.setCategory(requestUpdateInventory.getCategory());
        }
        if(Objects.nonNull(requestUpdateInventory.getSku()) && !requestUpdateInventory.getSku().isEmpty()) {
            inventory.setSku(requestUpdateInventory.getSku());
        }
        if(Objects.nonNull(requestUpdateInventory.getName()) && !requestUpdateInventory.getName().isEmpty()) {
            inventory.setName(requestUpdateInventory.getName());
        }
        if(Objects.nonNull(requestUpdateInventory.getLocation()) && !requestUpdateInventory.getLocation().isEmpty()) {
            inventory.setLocation(requestUpdateInventory.getLocation());
        }
        if(Objects.nonNull(requestUpdateInventory.getQuantity())) {
            inventory.setQuantity(requestUpdateInventory.getQuantity());
        }
        if(Objects.nonNull(requestUpdateInventory.getSupplier()) && !requestUpdateInventory.getSupplier().isEmpty()) {
            inventory.setSupplier(requestUpdateInventory.getSupplier());
        }
        return inventory;
    }
}
