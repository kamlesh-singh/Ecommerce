package org.ttn.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.ttn.ecommerce.dto.product.ProductVariationDto;
import org.ttn.ecommerce.dto.product.ProductResponseDto;
import org.ttn.ecommerce.dto.responseDto.ProductVariationResponseDto;
import org.ttn.ecommerce.entities.product.Product;
import org.ttn.ecommerce.services.SellerServiceImpl;
import org.ttn.ecommerce.services.product.ProductService;
import org.ttn.ecommerce.services.product.ProductVariationService;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {


    @Autowired
    ProductService productService;

    @Autowired
    SellerServiceImpl sellerDaoService;

    @Autowired
    ProductVariationService productVariationService;


    /**
     * Add Product
     */
    @PostMapping("add/product/{categoryId}")
    public ResponseEntity<?> addProduct(@RequestBody Product product, @PathVariable("categoryId") Long categoryId, Authentication authentication) {

        String email = authentication.getName();
        return productService.addProduct(product, categoryId, email);

    }


    /**
     *              View Product By Product ID
     */
    @GetMapping("view/product/{id}")
    public ResponseEntity<?> viewProductById(@PathVariable("id") Long id,Authentication authentication) throws Exception {
        String email = authentication.getName();
        return productService.viewProductById(id, email);
    }

    /**
     * Add Product Variation
     */
    @PostMapping("add/product-variation")
    public ResponseEntity<?> createProductVariation(@RequestBody ProductVariationDto productVariationDto) {

        return productVariationService.createProductVariation(productVariationDto);
    }


    /**
     * @Probelem    : View Product Variation By Id
     * @OutPut      : Product Variation with Parent Product Details
     */
    @GetMapping("view/product-variation/{variationId}")
    public ProductVariationResponseDto viewProductVariation(@PathVariable("variationId") Long productVariationId, Authentication authentication) {
        String email = authentication.getName();
        return productVariationService.getProductVariation(productVariationId, email);
    }


    /**
     *     @Problem : View All Products Created By Seller
     *     @Output  : All non-deleted product with Category details
    */
    @GetMapping("view/all-products")
    public List<ProductResponseDto> viewAllProduct(Authentication authentication) {

        String email = authentication.getName();
        return productVariationService.viewAllProductsOfSeller(email);

    }


    /**
     *      @Problem : View All Product Variations For A Product
     *      @Outptut : Product Variations OF The Product
     */
    @GetMapping("seller/view/product-variation/{productId}")
    public ResponseEntity<?> viewProductVariationOfProduct(@PathVariable("productId") Long productId){

        return productVariationService.viewProductVariationByProduct(productId);
    }


    /**
     * Delete Product
     */
    @DeleteMapping("delete/product/{id}")
    public String deleteProduct(@Param("id") Long id, Authentication authentication) {
        String email = authentication.getName();
        return productService.deleteProduct(email, id);

    }

    /**
     *      @Problem :  (1) Update Product
     *                  (2) Check If Updatable Product Name IS UNIQUE
     *                  WITH Respect TO  {BRAND , CATEGORY, SELLER} Combination
     *      @Output  :  Update Product Or Return Error If Any .
     */
    @PutMapping("/update/product/{productId}")
    public String updateProduct(@PathVariable("productId") Long productId, @RequestBody Product product, Authentication authentication) {

        String email = authentication.getName();
        return productService.updateProduct(email, productId, product);
    }


}
