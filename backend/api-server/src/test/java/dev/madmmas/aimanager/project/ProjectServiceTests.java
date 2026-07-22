package dev.madmmas.aimanager.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTests {

  @Mock
  private ProjectRepository projectRepository;

  @InjectMocks
  private ProjectService projectService;

  @Test
  void isSlugTakenDelegatesNormalizedSlugToRepository() {
    when(projectRepository.existsBySlug("news-radar")).thenReturn(true);

    assertThat(projectService.isSlugTaken("News Radar")).isTrue();
    verify(projectRepository).existsBySlug("news-radar");
  }

  @Test
  void isSlugTakenReturnsFalseWhenSlugIsFree() {
    when(projectRepository.existsBySlug("brand-new")).thenReturn(false);

    assertThat(projectService.isSlugTaken("Brand New")).isFalse();
  }

  @Test
  void isSlugTakenRejectsBlankNames() {
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> projectService.isSlugTaken("   "));

    assertThat(error).hasMessageContaining("non-empty slug");
    verifyNoInteractions(projectRepository);
  }
}
