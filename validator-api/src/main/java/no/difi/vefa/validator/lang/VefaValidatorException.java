package no.difi.vefa.validator.lang;

/**
 * Exception specific to validator.
 */
public class VefaValidatorException extends Exception
{
  public VefaValidatorException (final String message)
  {
    super (message);
  }

  public VefaValidatorException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
}
