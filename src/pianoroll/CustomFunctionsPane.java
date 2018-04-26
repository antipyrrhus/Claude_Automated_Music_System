package pianoroll;

import customapi.MyClassLoader;
import customapi.SuperCustomFunctions;

import java.lang.reflect.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomFunctionsPane extends BorderPane{
	
	private final int PREF_WIDTH = 500;
	private final int PREF_HEIGHT = 100;

	private Stage stage;
	private PianoRollGUI pianoRollGUI;
	private SuperCustomFunctions cf;
	private TextField methodField, paramField;
	private MyClassLoader classLoader;
	private ListView<String> lv;
	

	public CustomFunctionsPane(PianoRollGUI gui, Stage stage) {
		this.pianoRollGUI = gui;
		this.stage = stage;
//		this.cf = new CustomFunctions(pianoRollGUI.getScorePane(), pianoRollGUI.getInstrumentInt(), this);
		try {
			this.reload();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
				
		
	}
	
	/**
	 * Dynamically re-loads the CustomFunctions class. This means the user can edit this class
	 * while this program is running, and the next time the user opens this pane, the edited class
	 * will be dynamically re-loaded without having to re-start the entire program.
	 * @throws Exception
	 */
	private void reload() throws Exception {
		this.classLoader = new MyClassLoader(MyClassLoader.class.getClassLoader());
		
		try {
			//Use MyClassLoader's override method loadClass() to manually re-load the class
			Class<?> myObjectClass = this.classLoader.loadClass("customapi.CustomFunctions");
			
			//Must cast the new instance of this class to a superclass (or alternatively to an interface)
			//Why is this needed? See http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
			SuperCustomFunctions cfNew = (SuperCustomFunctions)myObjectClass.getDeclaredConstructor(
							ScorePane.class, CustomFunctionsPane.class
								).newInstance(pianoRollGUI.getScorePane(), this);
			this.cf = cfNew;
			
			this.setPadding(new Insets(10,10,10,10));
			
			this.lv = new ListView<>();
			ObservableList<String> items =FXCollections.observableArrayList ();
			lv.setItems(items);
			int index = 0;
			while (this.cf.getCommandsStr(index) != null) {
				lv.getItems().add(this.cf.getCommandsStr(index++));
			}
			
			
			lv.setOnMouseClicked(new EventHandler<MouseEvent>() {
			    @Override
			    public void handle(MouseEvent event) {
			    		String selectedItem = lv.getSelectionModel().getSelectedItem(); 
			    		methodField.setText(selectedItem.substring(0, selectedItem.indexOf('(')));
			    	}    
			});
			
			VBox box = new VBox(5);
			box.setAlignment(Pos.CENTER);
			this.methodField = new TextField();
			this.paramField = new TextField();
			box.getChildren().addAll(methodField, paramField);
			
			
			lv.getSelectionModel().select(0);
			String selectedItem = lv.getSelectionModel().getSelectedItem();
			methodField.setText(selectedItem.substring(0, selectedItem.indexOf('(')));
			
			//OK, Cancel buttons in HBox
			HBox buttonsHB = new HBox(10);  //Will leave gap between the buttons
			buttonsHB.setAlignment(Pos.CENTER);
			Button ok = new Button("OK");
			ok.setOnAction(e -> {
				try {
					String methodName = methodField.getText();
					this.cf.runCommand(methodName);
					
				} catch (NumberFormatException nfe) {
					System.out.println("Invalid number. Closing...");
					this.stage.close();
				}
			});
			Button close = new Button("Close");
			close.setOnAction(e -> {
				this.stage.close();
			});
			buttonsHB.getChildren().addAll(ok, close);

			//Set this Pane's size and add nodes
			this.setPrefWidth(PREF_WIDTH);
			this.setPrefHeight(PREF_HEIGHT);
			this.setTop(lv);
			this.setCenter(box);
			this.setBottom(buttonsHB);

			BorderPane.setMargin(lv, new Insets(10));
			BorderPane.setAlignment(lv,  Pos.CENTER);
			BorderPane.setMargin(box, new Insets(10));
			BorderPane.setAlignment(box,  Pos.CENTER);
			BorderPane.setMargin(buttonsHB, new Insets(10));
			BorderPane.setAlignment(buttonsHB,  Pos.CENTER);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} 
		catch (ClassCastException cce) {
			cce.printStackTrace();
		}
	}
	
	/**
	 * Reads the param TextField in this Pane, and then splits them into int[] array
	 * (assumes that " " space bar delimiter is used, and that all the params are ints) 
	 * @return int[]
	 */
	public int[] getParams() {
		String[] paramStr = this.paramField.getText().split(" ");
		int[] ret = new int[paramStr.length];
		for (int i = 0; i < ret.length; ++i) ret[i] = Integer.parseInt(paramStr[i]);
		return ret;
	}
}