package com.example.demo.Controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.service.ImageStorageService;
import com.example.demo.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ImageStorageService imageStorageService;

    public ProductController(ProductService productService, ImageStorageService imageStorageService) {
        this.productService = productService;
        this.imageStorageService = imageStorageService;
    }

    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("products", productService.findAll(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "products";
    }

    @GetMapping({"/create", "/add"})
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formMode", "create");
        return "product-form";
    }

    @PostMapping({"/create", "/add"})
    public String createProduct(
        @Valid @ModelAttribute("product") Product product,
        BindingResult bindingResult,
        @RequestParam("imageFile") MultipartFile imageFile,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        validateImage(imageFile, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            return "product-form";
        }

        try {
            product.setImageName(imageStorageService.saveImage(imageFile));
            productService.create(product);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công.");
            return "redirect:/products";
        } catch (IOException exception) {
            bindingResult.rejectValue("imageName", "image.upload", "Không thể tải hình ảnh lên.");
            model.addAttribute("formMode", "create");
            return "product-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            return "redirect:/products";
        }

        model.addAttribute("product", productOptional.get());
        model.addAttribute("formMode", "edit");
        return "product-form";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
        @PathVariable Long id,
        @Valid @ModelAttribute("product") Product product,
        BindingResult bindingResult,
        @RequestParam("imageFile") MultipartFile imageFile,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Optional<Product> existingOptional = productService.findById(id);
        if (existingOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm.");
            return "redirect:/products";
        }

        Product existingProduct = existingOptional.get();
        validateImage(imageFile, bindingResult);

        if (bindingResult.hasErrors()) {
            product.setId(id);
            if (product.getImageName() == null || product.getImageName().isBlank()) {
                product.setImageName(existingProduct.getImageName());
            }
            model.addAttribute("formMode", "edit");
            return "product-form";
        }

        product.setId(id);
        String oldImage = existingProduct.getImageName();

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String newImage = imageStorageService.saveImage(imageFile);
                product.setImageName(newImage);
                imageStorageService.deleteImage(oldImage);
            } else {
                product.setImageName(oldImage);
            }

            productService.update(product);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công.");
            return "redirect:/products";
        } catch (IOException exception) {
            bindingResult.rejectValue("imageName", "image.upload", "Không thể tải hình ảnh lên.");
            product.setImageName(oldImage);
            model.addAttribute("formMode", "edit");
            return "product-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.findById(id).ifPresent(product -> imageStorageService.deleteImage(product.getImageName()));
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công.");
        return "redirect:/products";
    }

    private void validateImage(MultipartFile imageFile, BindingResult bindingResult) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
        if (fileName.length() > 200) {
            bindingResult.rejectValue("imageName", "imageName.max", "Tên hình ảnh không quá 200 kí tự.");
        }

        if (!imageStorageService.isAllowedImage(imageFile)) {
            bindingResult.rejectValue("imageName", "imageName.type", "Vui lòng tải lên file hình ảnh.");
        }
    }
}
