package no.difi.vefa.validator.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import no.difi.xsd.vefa.validator._1.AssertionType;
import no.difi.xsd.vefa.validator._1.FlagType;

public class SectionTest {

  @Test
  public void simpleNullFlag() {
    final FlagFilterer flagFilterer = Mockito.mock(FlagFilterer.class);
    final Section section = new Section(flagFilterer);

    section.add("TEST", "Simple test", null);

    assertEquals(section.getAssertion().size(), 0);
    assertEquals(section.getFlag(), FlagType.OK);

    Mockito.verify(flagFilterer).filterFlag(ArgumentMatchers.any(AssertionType.class));
    Mockito.verifyNoMoreInteractions(flagFilterer);
  }

  @Test
  public void simpleOkFlag() {
    final FlagFilterer flagFilterer = Mockito.mock(FlagFilterer.class);
    final Section section = new Section(flagFilterer);

    section.add("TEST", "Simple test", FlagType.OK);

    assertEquals(section.getAssertion().size(), 1);
    assertEquals(section.getFlag(), FlagType.OK);

    Mockito.verify(flagFilterer).filterFlag(ArgumentMatchers.any(AssertionType.class));
    Mockito.verifyNoMoreInteractions(flagFilterer);
  }

  @Test
  public void simpleWarningFlag() {
    final FlagFilterer flagFilterer = Mockito.mock(FlagFilterer.class);
    final Section section = new Section(flagFilterer);

    section.add("TEST", "Simple test", FlagType.WARNING);

    assertEquals(section.getAssertion().size(), 1);
    assertEquals(section.getFlag(), FlagType.WARNING);

    Mockito.verify(flagFilterer).filterFlag(ArgumentMatchers.any(AssertionType.class));
    Mockito.verifyNoMoreInteractions(flagFilterer);
  }
}
