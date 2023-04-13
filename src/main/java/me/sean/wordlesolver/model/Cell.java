package me.sean.wordlesolver.model;

/**
 * Stores a value and the status of that value
 *
 * @param <T> The type of value to store
 *
 * @author Sean Droll
 */
public class Cell<T> {
    private T value;
    private Model.Colors status;

    /**
     * Creates a new instance of Cell
     *
     * @param value the value to store
     * @param status the status of that value
     */
    public Cell(T value, Model.Colors status) {
        this.value = value;
        this.status = status;
    }

    /**
     * Gets the stored value
     * @return the value stored
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value stored
     * @param value the new value
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Gets the status of that value
     * @return status of that value as represented as a color
     */
    public Model.Colors getStatus() {
        return status;
    }

    /**
     * Sets the status of the value as a color
     * @param status the new status of the value
     */
    public void setStatus(Model.Colors status) {
        this.status = status;
    }

    /**
     * Simply prints the value of the cell as a string
     * @return the value as a string
     */
    @Override
    public String toString() {
        if(value == null) return "";
        return value.toString();
    }
}
