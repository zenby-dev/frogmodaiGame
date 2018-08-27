package frogmodaiGame.old;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class CurseMe3 {

	public static void main(String[] args) throws IOException {
		DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
		Screen screen = null;
		try {
			screen = terminalFactory.createScreen();
			screen.startScreen();
			final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
			final Window window = new BasicWindow("My Root Window");
			//window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));
			
			Panel contentPanel = new Panel(new GridLayout(2));
			GridLayout gridLayout = (GridLayout)contentPanel.getLayoutManager();
			gridLayout.setHorizontalSpacing(3);
			Label title = new Label("This is a label that spans two columns");
			title.setLayoutData(GridLayout.createLayoutData(
					GridLayout.Alignment.BEGINNING, //Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
					GridLayout.Alignment.BEGINNING, //Vertical alignment ''
					true,  //Give the component extra horizontal space if available
					false, //Give the component extra vertical space if available
					2, //Horizontal span
					1)); //Vertical span
			contentPanel.addComponent(title);
			
			contentPanel.addComponent(new Label("Text Box (aligned)"));
			contentPanel.addComponent(
					new TextBox()
						.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.CENTER)));
			
			contentPanel.addComponent(new Label("Password box (right aligned)"));
			contentPanel.addComponent(
					new TextBox()
						.setMask('*')
						.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER)));
			
			contentPanel.addComponent(new Label("Read-only Combo Box (forced size)"));
			List<String> timezonesAsStrings = new ArrayList<String>();
			for (String id : TimeZone.getAvailableIDs()) {
				timezonesAsStrings.add(id);
			}
			ComboBox<String> readOnlyComboBox = new ComboBox<String>(timezonesAsStrings);
			readOnlyComboBox.setReadOnly(true);
			readOnlyComboBox.setPreferredSize(new TerminalSize(20, 1));
			contentPanel.addComponent(readOnlyComboBox);
			
			contentPanel.addComponent(new Label("Editable Combo Box (filled)"));
			contentPanel.addComponent(
					new ComboBox<String>("Item #1", "Item #2", "Item #3", "Item #4")
						.setReadOnly(false)
						.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)));
			
			contentPanel.addComponent(new Label("Button (centered)"));
			contentPanel.addComponent(new Button("Button", new Runnable() {
				@Override
				public void run() {
					File returnedFile = new FileDialogBuilder()
							.setTitle("Open File")
							.setDescription("Choose a file")
							.setActionLabel("Open")
							.build()
							.showDialog(textGUI);
					if (returnedFile != null) {
						System.out.println(returnedFile.getName());
					}
					//MessageDialog.showMessageDialog(textGUI, "Message Box", "This is a message box", MessageDialogButton.OK);
				}
			}).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER)));
			
			contentPanel.addComponent(
					new EmptySpace()
						.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));
			contentPanel.addComponent(
					new Separator(Direction.HORIZONTAL)
						.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));
			contentPanel.addComponent(
					new Button("Close", new Runnable() {
						@Override
						public void run() {
							window.close();
						}
					}).setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2)));
			
			window.setComponent(contentPanel);
			textGUI.addWindowAndWait(window);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (screen != null) {
				try {
					screen.stopScreen();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
