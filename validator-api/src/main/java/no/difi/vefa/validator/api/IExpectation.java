package no.difi.vefa.validator.api;

public interface IExpectation extends IFlagFilterer {

    String getDescription();

    void verify(Section section);

}
