package no.difi.vefa.validator.api;

import java.io.IOException;
import java.nio.file.Path;

public interface IPreparer {
  enum EType {
    FILE,
    STYLESHEET,
    INCLUDE
  }

  void prepare(Path source, Path target, EType type) throws IOException;
}
