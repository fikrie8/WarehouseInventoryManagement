import * as XLSX from 'xlsx';

/**
 * Dynamically finds and validates an Excel template header structure across any arbitrary row
 * @param {File} file - The raw file object from the HTML input element
 * @param {Array<string>} expectedHeaders - The array of headers required for this tab
 * @returns {Promise<{isValid: boolean, headerRowIndex: number}>} - Returns validation status and the exact index where the header row was found
 */

export const validateExcelHeader = (file, expectedHeaders) => {
    return new Promise((resolve, reject) => {
        if (!expectedHeaders || expectedHeaders.length === 0) {
            reject(new Error("No validation layout profile provided."));
            return;
        }

        const reader = new FileReader();

        reader.onload = (event) => {
            try {
                const data = event.target.result;
                const workbook = XLSX.read(data, { type: 'binary' });
                const worksheet = workbook.Sheets[workbook.SheetNames[0]];

                // Read the file as a 2D matrix
                const fileRows = XLSX.utils.sheet_to_json(worksheet, { header: 1 });

                // SAFETY CHECK 1: Immediately exit if the file contains no rows at all
                if (!fileRows || fileRows.length === 0) {
                    resolve({ isValid: false, headerRowIndex: -1 });
                    return;
                }

                let headerRowIndex = -1;
                let isValidStructure = false;

                const normalizedExpected = expectedHeaders.map(h => h.trim().toLowerCase());

                // SAFETY CHECK 2: Limit scanner depth. 
                // Realistically, headers will always appear in the first 50 rows.
                // This prevents endless loops if a user uploads an huge corrupted file.
                const MAX_SCAN_DEPTH = Math.min(fileRows.length, 50);

                for (let i = 0; i < MAX_SCAN_DEPTH; i++) {
                    const currentRow = fileRows[i] || [];
                    
                    // Filter out completely blank cells in this row to avoid waste
                    const normalizedCurrentRow = currentRow
                        .map(cell => cell?.toString().trim().toLowerCase() || "")
                        .filter(cell => cell !== "");

                    // Skip evaluating this row if it's completely empty
                    if (normalizedCurrentRow.length === 0) continue;

                    // Verify if ALL expected columns exist anywhere in this row array
                    const matchFound = normalizedExpected.every(expectedHeader => 
                        normalizedCurrentRow.includes(expectedHeader)
                    );

                    if (matchFound) {
                        headerRowIndex = i;
                        isValidStructure = true;
                        break; // 🛡️ SAFETY CHECK 3: Stop scanning immediately once found
                    }
                }

                resolve({ isValid: isValidStructure, headerRowIndex });
            } catch (err) {
                reject(err);
            }
        };

        reader.onerror = () => reject(new Error("File system reader failed."));
        reader.readAsBinaryString(file);
    });
};
