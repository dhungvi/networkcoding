package ch.epfl.lsr.adhoc.runtime;

public class ParamDoesNotExistException extends FrancException {

  public ParamDoesNotExistException(String message) {
    super(message);
  }
  
  public ParamDoesNotExistException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public ParamDoesNotExistException(Throwable cause) {
    super(cause);
  }

}
