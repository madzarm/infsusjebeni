package com.infsus.dz3_md.controller;

import com.infsus.dz3_md.domain.Product;
import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.service.ProductService;
import com.infsus.dz3_md.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProjectService projectService;

    /**
     * Populate all projects for dropdowns
     */
    @ModelAttribute("allProjects")
    public List<Project> populateProjects() {
        return projectService.search(null, Pageable.unpaged()).getContent();
    }

    /**
     * List or filter products by project or name
     */
    @GetMapping
    public String listProducts(
            @RequestParam Optional<UUID> projectId,
            @RequestParam Optional<String> q,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        UUID pid = projectId.orElse(null);
        String query = q.filter(s -> !s.isBlank()).orElse(null);
        model.addAttribute("page", productService.search(pid, query, pageable));
        model.addAttribute("projectId", pid);
        model.addAttribute("q", query);
        return "product-list";
    }

    /**
     * Show form to create a new product, optionally pre-selecting project
     */
    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam Optional<UUID> projectId,
            Model model
    ) {
        Product product = new Product();
        projectId.ifPresent(id -> product.setProject(projectService.findById(id)));
        model.addAttribute("product", product);
        return "product-form";
    }

    /**
     * Show form to edit an existing product
     */
    @GetMapping("/{id}")
    public String showEditForm(
            @PathVariable UUID id,
            Model model,
            RedirectAttributes redirectAttrs
    ) {
        try {
            Product product = productService.findById(id);
            model.addAttribute("product", product);
            return "product-form";
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/products";
        }
    }

    /**
     * Handle create or update of a product
     */
    @PostMapping
    public String saveProduct(
            @Valid @ModelAttribute Product product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs
    ) {
        if (bindingResult.hasErrors()) {
            return "product-form";
        }
        try {
            Product saved = productService.save(product);
            redirectAttrs.addFlashAttribute("msg", "Product saved successfully!");
            return "redirect:/projects/" + saved.getProject().getProjectId();
        } catch (Exception ex) {
            bindingResult.rejectValue(
                    ex instanceof IllegalStateException ? "name" : "project",
                    "error",
                    ex.getMessage()
            );
            return "product-form";
        }
    }

    /**
     * Delete a product and return to its project view
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(
            @PathVariable UUID id,
            RedirectAttributes redirectAttrs
    ) {
        try {
            Product product = productService.findById(id);
            productService.delete(id);
            redirectAttrs.addFlashAttribute("msg", "Product deleted successfully!");
            return "redirect:/projects/" + product.getProject().getProjectId();
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/products";
        }
    }
}