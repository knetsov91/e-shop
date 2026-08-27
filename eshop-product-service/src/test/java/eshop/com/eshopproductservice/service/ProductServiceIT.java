package eshop.com.eshopproductservice.service;

import eshop.com.eshopproductservice.TestcontainersConfiguration;
import eshop.com.eshopproductservice.model.Product;
import eshop.com.eshopproductservice.web.dto.ProductCreateRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false"
})
class ProductServiceIT {

    @Autowired
    private ProductService productService;

    @Test
    void createProduct_thenPersistsToMongoAndIsRetrievableViaGetProducts() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(999.99);
        request.setQuantity(10);

        Product saved = productService.createProduct(request);

        assertThat(saved.getId()).isNotBlank();

        List<Product> products = productService.getProducts();
        assertThat(products)
                .filteredOn(p -> p.getId().equals(saved.getId()))
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getName()).isEqualTo("Laptop");
                    assertThat(p.getPrice()).isEqualTo(999.99);
                    assertThat(p.getQuantity()).isEqualTo(10);
                });
    }
}
