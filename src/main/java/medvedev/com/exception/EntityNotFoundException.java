package medvedev.com.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String tableName, Number id) {
        super(String.format("%s not found with id = %d", tableName, id));
    }

    public EntityNotFoundException(String tableName, String name) {
        super(String.format("%s not found with name = %s", tableName, name));
    }
}
