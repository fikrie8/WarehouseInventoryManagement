/**
 * This file should be used to determine the order of every excel file so that every excel uploaded is following a standard layout.
 */

export const EXCEL_TEMPLATES = {
    // Your new layout columns
    INVENTORY: ["No", "SKU Code", "Category", "Inventory Name", "Location", "Quantity", "Supplier"], 
    USER: ["Username", "Email", "Role", "Department", "Status"],
    INBOUND: ["InboundID", "SKU", "Quantity", "ReceivedDate", "Carrier"],
    OUTBOUND: ["OutboundID", "SKU", "Quantity", "ShipDate", "Destination"]
};