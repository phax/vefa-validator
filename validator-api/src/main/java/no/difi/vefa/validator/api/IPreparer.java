package no.difi.vefa.validator.api;

import java.io.IOException;
import java.nio.file.Path;

public interface IPreparer
{
  enum EPreparerType
  {
    FILE,
    STYLESHEET,
    INCLUDE
  }

  void prepare (Path source, Path target, EPreparerType type) throws IOException;
}
