package com.joelcode.personalinvestmentportfoliotracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

// Generic API Response wrapper for consistent response format
// This class configures the controller responses to the frontend to make it consistent.

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    // Define key fields
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private Integer statusCode;
    private List<String> errors;
    private PaginationMetadata pagination;

    // Constructors

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Factory methods for custom messages for data presentation and error handling

    // Create successful controller response with data
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    // Create successful response with data and custom message
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    // Create successful response with only message (no data)
    public static <Void> ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    //Create error response with message
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null);
        response.statusCode = 400;
        return response;
    }

    // Create error response with message and status code
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null);
        response.statusCode = statusCode;
        return response;
    }

    // Create error response with multiple error messages
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null);
        response.errors = errors;
        response.statusCode = 400;
        return response;
    }

    // Create validation error response
    public static <T> ApiResponse<T> validationError(List<String> errors) {
        ApiResponse<T> response = new ApiResponse<>(false, "Validation failed", null);
        response.errors = errors;
        response.statusCode = 422;
        return response;
    }

    // Builder methods

    // Add pagination metadata
    public ApiResponse<T> withPagination(int currentPage, int totalPages, long totalElements) {
        this.pagination = new PaginationMetadata(currentPage, totalPages, totalElements);
        return this;
    }

    // Add pagination metadata with page size
    public ApiResponse<T> withPagination(int currentPage, int totalPages, long totalElements, int pageSize) {
        this.pagination = new PaginationMetadata(currentPage, totalPages, totalElements, pageSize);
        return this;
    }

    // Set status code
    public ApiResponse<T> withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    // Add error list
    public ApiResponse<T> withErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    // Pagination metadata for large amounts of data to package properly and tell frontend where everything is
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationMetadata {

        // Define key fields
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private Integer pageSize;
        private boolean hasNext;
        private boolean hasPrevious;

        // Constructors
        public PaginationMetadata(int currentPage, int totalPages, long totalElements) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.hasNext = currentPage < totalPages - 1;
            this.hasPrevious = currentPage > 0;
        }

        public PaginationMetadata(int currentPage, int totalPages, long totalElements, int pageSize) {
            this(currentPage, totalPages, totalElements);
            this.pageSize = pageSize;
        }

        // Getters
        public int getCurrentPage() { return currentPage; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
        public Integer getPageSize() { return pageSize; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
    }

    // Getters and setters

    public boolean isSuccess() {return success;}

    public void setSuccess(boolean success) {this.success = success;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public T getData() {return data;}

    public void setData(T data) {this.data = data;}

    public LocalDateTime getTimestamp() {return timestamp;}

    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}

    public Integer getStatusCode() {return statusCode;}

    public void setStatusCode(Integer statusCode) {this.statusCode = statusCode;}

    public List<String> getErrors() {return errors;}

    public void setErrors(List<String> errors) {this.errors = errors;}

    public PaginationMetadata getPagination() {return pagination;}

    public void setPagination(PaginationMetadata pagination) {this.pagination = pagination;}
}

// ==================== JSON RESPONSE EXAMPLES ====================

/**
 * SUCCESS RESPONSE:
 * {
 *   "success": true,
 *   "message": "Account retrieved successfully",
 *   "data": {
 *     "accountId": "123e4567-e89b-12d3-a456-426614174000",
 *     "accountName": "My Investment Account",
 *     "cashBalance": 10000.00,
 *     "user": { ... }
 *   },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * ERROR RESPONSE:
 * {
 *   "success": false,
 *   "message": "Account not found",
 *   "data": null,
 *   "timestamp": "2024-01-15T10:30:00",
 *   "statusCode": 404
 * }
 *
 * PAGINATED RESPONSE:
 * {
 *   "success": true,
 *   "message": "Accounts retrieved successfully",
 *   "data": [ ... ],
 *   "timestamp": "2024-01-15T10:30:00",
 *   "pagination": {
 *     "currentPage": 0,
 *     "totalPages": 5,
 *     "totalElements": 47,
 *     "pageSize": 10,
 *     "hasNext": true,
 *     "hasPrevious": false
 *   }
 * }
 */