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
import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final ProjectService projectService;

    /** make all projects available for the FK‐dropdown */
    @ModelAttribute("allProjects")
    public List<Project> allProjects() {
        return projectService.search(null, Pageable.unpaged())
                .getContent();
    }


    /** list or filter by projectId/q */
    @GetMapping
    public String list(@RequestParam(required = false) UUID projectId,
                       @RequestParam(required = false) String q,
                       @PageableDefault(size = 10) Pageable pg,
                       Model m) {
        m.addAttribute("page", service.search(projectId, q, pg));
        m.addAttribute("projectId", projectId);
        m.addAttribute("q", q);
        return "product-list";
    }

    /** new product (optional projectId pre‐selected) */
    @GetMapping("/new")
    public String create(@RequestParam(required = false) UUID projectId, Model m) {
        Product p = new Product();
        if (projectId != null) {
            // pre‐set the FK so the dropdown will default
            p.setProject(projectService.findOne(projectId));
        }
        m.addAttribute("product", p);
        return "product-form";
    }

    /** edit existing */
    @GetMapping("/{id}")
    public String edit(@PathVariable UUID id, Model m) {
        m.addAttribute("product", service.findOne(id));
        return "product-form";
    }

    /** save (create or update) */
    @PostMapping
    public String save(@Valid @ModelAttribute Product product,
                       BindingResult br,
                       Model m,
                       RedirectAttributes ra) {

        // re-populate the dropdown if we need to re-render
        m.addAttribute("allProjects",
                projectService.search(null, Pageable.unpaged()).getContent());

        if (br.hasErrors()) {
            return "product-form";
        }

        try {
            service.save(product);
        } catch (IllegalStateException ex) {
            // uniqueness or missing-project error → bind to the 'name' field
            br.rejectValue("name", "duplicate", ex.getMessage());
            return "product-form";
        }

        ra.addFlashAttribute("msg", "Saved!");
        return "redirect:/projects/" + product.getProject().getProjectId();
    }

    /** delete and return to the project view */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id,
                         RedirectAttributes ra) {
        Product p = service.findOne(id);
        service.delete(id);
        ra.addFlashAttribute("msg", "Deleted!");
        return "redirect:/projects/" + p.getProject().getProjectId();
    }
}
