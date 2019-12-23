
// The ATM controller is very simple - it simply passes on the action string
// to the Model. It coulld do more such as mapping action labels onto logical 
// functions in the model (as the breakout game does), or providing more than
// one way on the interface to do the same task in the model
public class Controller
{
    public Model model;
    public View  view;

    // we don't really need a constructor method, but include one to print a 
    // debugging message if required
    public Controller()
    {
        Debug.trace("Controller::<constructor>");
    }

    // This is how the View talks to the Controller
    // AND how the Controller talks to the Model
    // This method is called by the View to respond to some user interface event
    // The controller's job is to decide what to do. In this case it just passes
    // the action (which is the label on the button that was pressed) to the 
    // process method of the model
    public void process( String action )
    {
        Debug.trace("Controller::process: action = " + action);
        // Try setting a breakpoint here
        model.process( action );
    }

}
