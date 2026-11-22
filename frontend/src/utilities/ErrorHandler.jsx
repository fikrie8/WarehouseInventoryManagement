export const getErrorMessage = (err) => {
    if (err.response) {
        // 1. Extract the 'message' field from your backend ErrorResponse JSON
        if (err.response.data?.message) {
            return err.response.data.message;
        }
        // 2. Handle plain text responses (e.g., from Spring Security 401/403 filters)
        if (typeof err.response.data === 'string' && err.response.data.trim() !== '') {
            return err.response.data;
        }
        return `Server error (${err.response.status})`;
    }
    
    // 3. Handle network/connection drops where no server response arrived
    return err.message || "Network error: Unable to reach the server.";
};
