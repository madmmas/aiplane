package dev.madmmas.aimanager.prompt;

import dev.madmmas.aimanager.prompt.dto.PromptCreateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptResponse;
import dev.madmmas.aimanager.prompt.dto.PromptUpdateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptVersionCreateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptVersionResponse;
import dev.madmmas.aimanager.prompt.dto.PromptVersionStatusUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prompts")
public class PromptController {

  private final PromptService service;

  public PromptController(PromptService service) {
    this.service = service;
  }

  @GetMapping
  List<PromptResponse> list(@RequestParam("projectId") String projectId) {
    return service.list(projectId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  PromptResponse create(@Valid @RequestBody PromptCreateRequest request) {
    return service.create(request);
  }

  @GetMapping("/{id}")
  PromptResponse get(@PathVariable("id") String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  PromptResponse update(
      @PathVariable("id") String id, @RequestBody PromptUpdateRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable("id") String id) {
    service.delete(id);
  }

  @GetMapping("/{id}/versions")
  List<PromptVersionResponse> listVersions(@PathVariable("id") String id) {
    return service.listVersions(id);
  }

  @PostMapping("/{id}/versions")
  @ResponseStatus(HttpStatus.CREATED)
  PromptVersionResponse createVersion(
      @PathVariable("id") String id, @Valid @RequestBody PromptVersionCreateRequest request) {
    return service.createVersion(id, request);
  }

  @GetMapping("/{id}/versions/{vid}")
  PromptVersionResponse getVersion(
      @PathVariable("id") String id, @PathVariable("vid") String vid) {
    return service.getVersion(id, vid);
  }

  @PatchMapping("/{id}/versions/{vid}/status")
  PromptVersionResponse updateVersionStatus(
      @PathVariable("id") String id,
      @PathVariable("vid") String vid,
      @Valid @RequestBody PromptVersionStatusUpdateRequest request) {
    return service.updateVersionStatus(id, vid, request.status());
  }

  /** Advances one step: draft → testing → active. */
  @PostMapping("/{id}/versions/{vid}/promote")
  PromptVersionResponse promoteVersion(
      @PathVariable("id") String id, @PathVariable("vid") String vid) {
    return service.promoteVersion(id, vid);
  }
}
