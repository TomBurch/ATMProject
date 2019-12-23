import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.*;
import java.util.*;

/** The View class creates and manages the GUI for the application. It displays
 * the current state of the Model, and handles user input.
 */
public class View
{
    /**Height of window in pixels*/
    int H = 420;
    /**Width of window in pixels*/
    int W = 500;

    //Variables for components of the user interface
    Label      message;       // Message area 
    TextField  output1;       // Result area 
    TextArea   output2;       // Result area 
    ScrollPane scrollPane;    // scrollbars around the TextArea object  
    GridPane   grid;          // main layout grid
    
    TilePane   optionPaneL;   //Left option buttons
    TilePane   optionPaneR;   //Right option buttons
    TilePane   numpadPane;    //Central numpad
    
    /**Map used for easy modification of option buttons*/
    public Map<String, Button> paneMap = new HashMap<String, Button>();   

    // The other parts of the model-view-controller setup
    public Model model;
    public Controller controller;
    

    /** View constructor, outputs a debug message
     * 
     */
    public View() {
        Debug.trace("View::<constructor>");
    }

    /** Creates the interface, assigns CSS styling and button functionality.
     * 
     */
    public void start(Stage window) {
        Debug.trace("View::start");

        // create the user interface component objects
        // The ATM is a vertical grid of four components -
        // label, two text boxes, and a tiled panel
        // of buttons

        // layout objects
        grid = new GridPane();
        grid.setId("Layout");           // assign an id to be used in css file
        grid.getColumnConstraints().add(new ColumnConstraints(110));
        grid.getColumnConstraints().add(new ColumnConstraints(250));
        grid.getColumnConstraints().add(new ColumnConstraints(110));
       
        
        numpadPane = new TilePane();
        numpadPane.setId("NumPad");    // assign an id to be used in css file
        optionPaneL = new TilePane();
        optionPaneL.setId("Options");
        optionPaneR = new TilePane();
        optionPaneR.setId("Options");
        
        message  = new Label();         // Message bar at the top
        grid.add( message, 0, 0, 3, 1);       // Add to GUI at the top       

        output1  = new TextField();     // text field for numbers      
        grid.add( output1, 0, 1, 3, 1);       // Add to GUI on second row                      

        output2  = new TextArea();      // multi-line text area
        output2.setEditable(false);     // Read only (for the user)
        scrollPane  = new ScrollPane(); // create a scrolling window
        scrollPane.setContent( output2 );  // put the text area 'inside' the scrolling window
        grid.add( scrollPane, 0, 2, 3, 1);    // add the scrolling window to GUI on third row

        // Buttons - these are layed out on a tiled pane, then
        // the whole pane is added to the main grid as the forth row

        // Button labels - empty strings are spacers
        // The number of button per row is set in the css file
        String labels[] = {
                "7",    "8",  "9",
                "4",    "5",  "6",
                "1",    "2",  "3",
                "CLR",  "0",  "ENT"};

        for ( int i=0; i<labels.length; i++ ) {
            String label = labels[i];
            if ( label.length() >= 1 ) {
                // non-empty string - make a button
                Button b = new Button( label );
                
                //Label for CSS styling
                if (label.equals("ENT")) {
                    b.setId("EnterButton");
                } else if (label.equals("CLR")) {
                    b.setId("ClearButton");
                } else {
                    b.setId("NumpadButtons");
                }
                
                b.setOnAction( this::buttonClicked ); // set the method to call when pressed
                numpadPane.getChildren().add( b );    // and add to tiled pane
            } else {
                // empty string - add an empty text element as a spacer
                numpadPane.getChildren().add( new Text() ); 

            }
        }
        
        //Create side panes
        for (int i=1; i < 5; i++) {
            Button leftButton = new Button();
            Button rightButton = new Button();
            leftButton.setId("OptionsButtons");
            rightButton.setId("OptionsButtons");
            leftButton.setOnAction(this::buttonClicked);
            rightButton.setOnAction(this::buttonClicked);
            paneMap.put("Left" + i, leftButton);
            paneMap.put("Right" + i, rightButton);
            optionPaneL.getChildren().add(leftButton);
            optionPaneR.getChildren().add(rightButton);
        }
        
        grid.add(optionPaneL,0,3);
        grid.add(numpadPane,1,3); // add the tiled pane of buttons to the grid
        grid.add(optionPaneR,2,3);
        
        // add the complete GUI to the window and display it
        Scene scene = new Scene(grid, W, H);   
        scene.getStylesheets().add("atm.css"); // tell the app to use our css file
        window.setScene(scene);
        window.show();

        // set the opening message at the top
        message.setText( "Bank" );                     // Opening message
    }

    // This is how the View talks to the Controller
    // This method is called when a button is pressed
    // It fetches the label on the button and passes it to the controller's process method
    public void buttonClicked(ActionEvent event) {
        Button b = ((Button) event.getSource());
        if ( controller != null ) {          
            String label = b.getText();   // get the button label
            Debug.trace( "View::buttonClicked:: Label = "+ label );
            // Try setting a breakpoint here
            controller.process( label );  // Pass it to the controller's process method
        }

    }
    
    /** Method for updating all 8 side options at once,<br>
     *  allows for easier customisation
     *  
     * @param args 8 strings to assign the button labels
     */
    public void updateOptions(String... args) {
        paneMap.get("Left1").setText(args[0]);
        paneMap.get("Left2").setText(args[1]);
        paneMap.get("Left3").setText(args[2]);
        paneMap.get("Left4").setText(args[3]);
        paneMap.get("Right1").setText(args[4]);
        paneMap.get("Right2").setText(args[5]);
        paneMap.get("Right3").setText(args[6]);
        paneMap.get("Right4").setText(args[7]); 
    }

    // This is how the Model talks to the View
    // This method gets called BY THE MODEL, whenever the model changes
    // It has to do whatever is required to update the GUI to show the new model status
    public void update() {        
        if (model != null) {
            Debug.trace( "View::update" );
            // Try setting a breakpoint here
            String message1 = model.display1;     // get the new message1 from the model
            output1.setText( message1 );            // add it as text of GUI control output1
            String message2 = model.display2;     // get the new message2 from the model
            output2.setText( message2 );            // add it as text of GUI control output2
        }
    }
}
