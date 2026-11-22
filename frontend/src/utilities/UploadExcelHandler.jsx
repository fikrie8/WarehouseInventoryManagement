import { validateExcelHeader } from './ExcelValidation';

/**
 * Validates the Excel file structure and bundles it into a ready-to-send FormData package.
 * @param {File} file - The raw file object from the HTML input element.
 * @param {Array<string>} expectedHeaders - The column profile layout required for this tab.
 * @returns {Promise<{formData: FormData, headerRowIndex: number}>} - Resolves with the payload on success, throws an error on failure.
 */
export const prepareExcelUploadPayload = async (file, expectedHeaders) => {
    // 1. Run the dynamic header row locator utility
    const { isValid, headerRowIndex } = await validateExcelHeader(file, expectedHeaders);

    if (!isValid) {
        throw new Error(`Template layout mismatch. Columns must include: ${expectedHeaders.join(', ')}`);
    }

    // 2. Package the validated file along with its dynamic row data index offset
    const formData = new FormData();
    formData.append('file', file);
    formData.append('headerIndex', headerRowIndex); // Tells Spring Boot how many rows to skip

    // Return the clean package back to the calling component
    return { formData, headerRowIndex };
};
