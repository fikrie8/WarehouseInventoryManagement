package com.example.fikrie.integration.DataModification;

import com.example.fikrie.DataTransferObject.RequestRespondInventory;
import com.example.fikrie.Model.Inventory;
import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.InboundRepo;
import com.example.fikrie.Repository.InventoryRepo;
import com.example.fikrie.Repository.OutboundRepo;
import com.example.fikrie.Repository.UserRepo;
import com.example.fikrie.Service.JwtService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.jwt.secret-key=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLXRlc3RpbmctYXV0aGVudGljYXRpb24taW50ZWdyYXRpb24=",
                "ADMIN_EMAIL=admin@example.com",
                "ADMIN_PASSWORD=adminPassword12345"
        }
)
@AutoConfigureTestRestTemplate
public class InventoryBulkRegistrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InventoryRepo inventoryRepo;

    @Autowired
    private InboundRepo inboundRepo;

    @Autowired
    private OutboundRepo outboundRepo;

    /* Need to delete inbound & outbound before able to delete inventory*/
    @BeforeEach
    void cleanUpDatabase() {
        userRepo.deleteAll();
        inboundRepo.deleteAll();
        outboundRepo.deleteAll();
        inventoryRepo.deleteAll();
    }

    private byte[] createTestExcelFile(String scenario) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Sheet sheet = workbook.createSheet("Inventory");
        // Row 0: Title row (skipped when headerIndex = 1)
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Warehouse Inventory Report");
        // Row 1: Column Headers (skipped when headerIndex = 1)
        Row headerRow = sheet.createRow(1);
        headerRow.createCell(0).setCellValue("No");
        headerRow.createCell(1).setCellValue("SKU");
        headerRow.createCell(2).setCellValue("Category");
        headerRow.createCell(3).setCellValue("Name");
        headerRow.createCell(4).setCellValue("Location");
        headerRow.createCell(5).setCellValue("Quantity");
        headerRow.createCell(6).setCellValue("Supplier");
        // Row 2: Valid Item 1
        Row row1 = sheet.createRow(2);
        row1.createCell(0).setCellValue(1);
        row1.createCell(1).setCellValue("SKU-BULK-001");
        row1.createCell(2).setCellValue("Electronics");
        row1.createCell(3).setCellValue("Wireless Mouse");
        row1.createCell(4).setCellValue("Store A");
        row1.createCell(5).setCellValue(50);
        row1.createCell(6).setCellValue("Logitech");
        // Row 3: Valid Item 2 (or invalid quantity if testing rollback)
        org.apache.poi.ss.usermodel.Row row2 = sheet.createRow(3);
        row2.createCell(0).setCellValue(2);
        row2.createCell(1).setCellValue("SKU-BULK-002");
        row2.createCell(2).setCellValue("Electronics");
        row2.createCell(3).setCellValue("Mechanical Keyboard");
        row2.createCell(4).setCellValue("Store B");
        if (scenario.equals("INVALID_TEXT")) {
            row2.createCell(5).setCellValue("NOT_A_NUMBER");
        } else if (scenario.equals("MISSING_QUANTITY")) {
            row2.createCell(5).setBlank();
        } else {
            row2.createCell(5).setCellValue(30);
        }
        row2.createCell(6).setCellValue("Keychron");
        workbook.write(out);

        return out.toByteArray();
        }
    }

    @Test
    @DisplayName("POST Bulk Upload: Should parse Excel file, skip header rows, and save all items to DB")
    void validExcelFileSavesAllItemsToDb() throws Exception {
        Users manager = new Users();
        manager.setUsername("BulkManager");
        manager.setEmail("bulkmanager@example.com");
        manager.setRole("MANAGER");
        userRepo.save(manager);
        String token = jwtService.generateToken("BulkManager");
        byte[] excelBytes = createTestExcelFile("VALID");
        ByteArrayResource fileResource = new ByteArrayResource(excelBytes) {
            @Override
            public String getFilename() {
                return "inventory_batch.xlsx";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("headerIndex", "1"); // Matches @RequestParam("headerIndex")
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<RequestRespondInventory> response = restTemplate.exchange(
                "/inventory/register-inventory/bulk-upload",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                RequestRespondInventory.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatusCode()).isEqualTo(200);
        // Verify both items were saved in the database
        assertThat(inventoryRepo.findBySku("SKU-BULK-001")).isPresent();
        assertThat(inventoryRepo.findBySku("SKU-BULK-002")).isPresent();
    }

    @Test
    @DisplayName("POST Bulk Upload (Negative Case): Should rollback entire batch if any row has invalid data")
    void corruptedRowRollsBackEntireTransaction() throws Exception {
        Users manager = new Users();
        manager.setUsername("BulkManager");
        manager.setEmail("bulkmanager@example.com");
        manager.setRole("MANAGER");
        userRepo.save(manager);
        String token = jwtService.generateToken("BulkManager");
        byte[] invalidExcelBytes = createTestExcelFile("INVALID_TEXT");
        ByteArrayResource fileResource = new ByteArrayResource(invalidExcelBytes) {
            @Override
            public String getFilename() {
                return "corrupted_batch.xlsx";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("headerIndex", "1");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<String> response = restTemplate.exchange(
                "/inventory/register-inventory/bulk-upload",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // GlobalExceptionHandler maps DataIntegrityViolationException -> 409 Conflict
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        // Verify Row 2 was NOT saved because @Transactional rolled back the entire batch
        assertThat(inventoryRepo.findBySku("SKU-BULK-001")).isEmpty();
        assertThat(inventoryRepo.findBySku("SKU-BULK-002")).isEmpty();
    }

    @Test
    @DisplayName("POST Bulk Upload (Negative Case): Should rollback entire batch when user deletes content of Quantity column")
    void missingColumnValueRollsBackEntireTransaction() throws Exception {
        Users manager = new Users();
        manager.setUsername("BulkManager");
        manager.setEmail("bulkmanager@example.com");
        manager.setRole("MANAGER");
        userRepo.save(manager);
        String token = jwtService.generateToken("BulkManager");
        // Generate Excel where Row 1 is valid, but Row 2 has a deleted/blank Quantity cell
        byte[] missingColumnExcelBytes = createTestExcelFile("MISSING_QUANTITY");
        ByteArrayResource fileResource = new ByteArrayResource(missingColumnExcelBytes) {
            @Override
            public String getFilename() {
                return "missing_column_batch.xlsx";
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("headerIndex", "1");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<String> response = restTemplate.exchange(
                "/inventory/register-inventory/bulk-upload",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class
        );

        // 1. Verify GlobalExceptionHandler catches the parsing failure and returns 409 Conflict
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        // 2. Verify @Transactional rolled back the ENTIRE batch (neither Row 1 nor Row 2 saved in DB)
        assertThat(inventoryRepo.findBySku("SKU-BULK-001")).isEmpty();
        assertThat(inventoryRepo.findBySku("SKU-BULK-002")).isEmpty();
        assertThat(inventoryRepo.count()).isZero();
    }
}
