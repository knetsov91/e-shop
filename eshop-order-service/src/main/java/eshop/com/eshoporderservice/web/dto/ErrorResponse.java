package eshop.com.eshoporderservice.web.dto;

public class ErrorResponse {

    private String message;
    private String statusCode;

    public ErrorResponse(String message, String statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}