package eshop.com.eshopauthservice.util;

import java.time.LocalDateTime;

public interface TimeProvider {

    LocalDateTime createdAt();
    LocalDateTime updatedAt();
}
