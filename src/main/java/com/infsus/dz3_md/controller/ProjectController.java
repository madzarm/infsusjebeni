package com.infsus.dz3_md.controller;

import com.infsus.dz3_md.domain.Project;
import com.infsus.dz3_md.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * List all projects with optional search query.
     */
    @GetMapping
    public String listProjects(
            @RequestParam Optional<String> q,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        String query = q.filter(s -> !s.isBlank()).orElse(null);
        model.addAttribute("page", projectService.search(query, pageable));
        model.addAttribute("q", query);
        return "project-list";
    }

    /**
     * Show form to create a new project.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        return "project-form";
    }

    /**
     * Handle submission of a new project.
     */
    @PostMapping
    public String createProject(
            @Valid @ModelAttribute Project project,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("error", "Please correct the errors below.");
            return "project-form";
        }
        try {
            projectService.save(project);
            redirectAttrs.addFlashAttribute("msg", "Project created successfully!");
            return "redirect:/projects";
        } catch (Exception ex) {
            model.addAttribute("project", project);
            model.addAttribute("error", ex.getMessage());
            return "project-form";
        }
    }

    /**
     * Show form to edit an existing project.
     */
    @GetMapping("/{id}")
    public String showEditForm(
            @PathVariable UUID id,
            Model model,
            RedirectAttributes redirectAttrs
    ) {
        try {
            Project project = projectService.findById(id);
            model.addAttribute("project", project);
            return "project-form";
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Project not found.");
            return "redirect:/projects";
        }
    }

    /**
     * Handle update of an existing project.
     */
    @PostMapping("/{id}")
    public String updateProject(
            @PathVariable UUID id,
            @Valid @ModelAttribute Project project,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs
    ) {
        System.out.println("Updating project with ID: " + id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("error", "Please correct the errors below.");
            return "project-form";
        }
        try {
            project.setProjectId(id);
            projectService.save(project);
            redirectAttrs.addFlashAttribute("msg", "Project updated successfully!");
            return "redirect:/projects";
        } catch (Exception ex) {
            model.addAttribute("project", project);
            model.addAttribute("error", ex.getMessage());
            return "project-form";
        }
    }

    /**
     * Delete a project by ID.
     */
    @PostMapping("/{id}/delete")
    public String deleteProject(
            @PathVariable UUID id,
            RedirectAttributes redirectAttrs
    ) {
        try {
            projectService.delete(id);
            redirectAttrs.addFlashAttribute("msg", "Project deleted successfully!");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/projects";
    }
}
