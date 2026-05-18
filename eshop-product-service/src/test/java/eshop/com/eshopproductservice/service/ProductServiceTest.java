package eshop.com.eshopproductservice.service;

import eshop.com.eshopproductservice.model.Product;
import eshop.com.eshopproductservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProducts_happyPath_returnsAllProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(999.99);

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.getProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
    }
}
