package me.sean.wordlesolver.view;

/**
 * Should be implemented for the view module in your MVC
 * that monitors a model and is updated to reflect changes
 *
 * @param <Observed> The model to observe
 * @param <Data> The data to be transmitted
 *
 * @author Sean Droll
 */
public interface Observer<Observed, Data> {

    /**
     * Updates the current view when something changes in the model
     * @param model the model the view is observing
     * @param message a message that contains the change in the model
     */
    void update(Observed model, Data message);
}
