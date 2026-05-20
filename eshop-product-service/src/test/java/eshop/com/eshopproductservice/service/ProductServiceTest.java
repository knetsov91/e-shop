package eshop.com.eshopproductservice.service;

import eshop.com.eshopproductservice.model.Product;
import eshop.com.eshopproductservice.repository.ProductRepository;
import eshop.com.eshopproductservice.web.dto.ProductCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProducts_whenProductsExist_thenReturnsAllProducts() {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(999.99);

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.getProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void getProducts_whenNoProducts_thenReturnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> result = productService.getProducts();

        assertThat(result).isEmpty();
    }

    @Test
    void createProduct_whenValidRequest_thenSavesAndReturnsProduct() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(999.99);
        request.setQuantity(10);

        Product saved = new Product();
        saved.setName("Laptop");
        saved.setPrice(999.99);
        saved.setQuantity(10);

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.createProduct(request);

        verify(productRepository).save(any(Product.class));
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getPrice()).isEqualTo(999.99);
        assertThat(result.getQuantity()).isEqualTo(10);
    }
}
