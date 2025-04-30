package no.difi.vefa.validator.api;

public interface IExpectation extends IFlagFilter {

    String getDescription();

    void verify(Section section);

}
