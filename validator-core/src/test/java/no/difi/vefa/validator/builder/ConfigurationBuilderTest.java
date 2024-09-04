package no.difi.vefa.validator.builder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import no.difi.xsd.vefa.validator._1.ConfigurationType;

public class ConfigurationBuilderTest {

    @Test
    public void simple() {
        ConfigurationType cfg = ConfigurationBuilder
                .identifier("test")
                .title("Test")
                .standardId("test#test")
                .weight(-10L)
                .trigger("test")
                .build("unit-test")
                .build();

        assertEquals(cfg.getIdentifier().getValue(), "test");
        assertEquals(cfg.getTitle(), "Test");
        assertEquals(cfg.getStandardId(), "test#test");
        assertEquals(cfg.getWeight(), -10L);
        assertEquals(cfg.getTrigger().size(), 1);
        assertEquals(cfg.getTrigger().get(0).getIdentifier(), "test");
        assertEquals(cfg.getBuild(), "unit-test");
    }

}
