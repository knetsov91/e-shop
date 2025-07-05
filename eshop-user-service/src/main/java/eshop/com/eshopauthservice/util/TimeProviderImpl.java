package eshop.com.eshopauthservice.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class TimeProviderImpl implements TimeProvider {
    @Override
    public LocalDateTime createdAt() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime updatedAt() {
        return  LocalDateTime.now();
    }
}
