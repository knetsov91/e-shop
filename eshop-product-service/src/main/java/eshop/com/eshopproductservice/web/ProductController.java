package eshop.com.eshopproductservice.web;

import eshop.com.eshopproductservice.model.Product;
import eshop.com.eshopproductservice.service.ProductService;
import eshop.com.eshopproductservice.web.dto.ProductCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = productService.getProducts();

        return ResponseEntity.ok(products);
    }
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateRequest productCreateRequest) {
        productService.createProduct(productCreateRequest);
        return ResponseEntity.status(201).build();
    }
}
