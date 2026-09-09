package eshop.com.eshopproductservice.service;

import eshop.com.eshopproductservice.model.Product;
import eshop.com.eshopproductservice.repository.ProductRepository;
import eshop.com.eshopproductservice.web.dto.ProductCreateRequest;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(ProductCreateRequest  productCreateRequest) {
        Product product = new Product();
        product.setName(productCreateRequest.getName());
        product.setPrice(productCreateRequest.getPrice());
        product.setQuantity(productCreateRequest.getQuantity());

        Product saved = productRepository.save(product);
        Sentry.captureMessage("Product created: " + saved.getId(), SentryLevel.INFO);
        return saved;
    }
}