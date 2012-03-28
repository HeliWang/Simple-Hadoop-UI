package zpi.windows;

import java.util.ArrayList;
import java.util.Collection;

import zpi.hadoopModel.Algorithm;
import zpi.hadoopModel.Queue;
import zpi.tools.SchemaParsing;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

/**
 * Okno sluzace do definiowania wejsciowych parametrow algorytmu
 *
 */
@SuppressWarnings("serial")
public class InputForAlgorithmWindow extends Window {

	private Algorithm algorithm;

	private Queue queue;

	private Table table;

	public InputForAlgorithmWindow(Algorithm algorithm, Queue queue) {
		super("Wprowadzanie danych wej�ciowych");
		this.algorithm = algorithm;
		this.queue = queue;
		createGUI();
		setHeight(600f, UNITS_PIXELS);
		setWidth(1020f, UNITS_PIXELS);
		;
	}

	private void createGUI() {

		Label tableLabel = new Label(
				"Wprowad� wej�ciowe warto�ci parametr�w algorytmu");
		tableLabel.setStyleName("h2");

		addComponent(tableLabel);

		table = new Table();
		table.setStyleName("strong borderless");
		table.setEditable(true);
		table.addContainerProperty("Nazwa", Label.class, null);
		table.addContainerProperty("Typ", Label.class, null);
		table.addContainerProperty("Warto�� domy�lna", Label.class, null);
		table.addContainerProperty("Warto�� minimalna", Label.class, null);
		table.addContainerProperty("Warto�� maksymalna", Label.class, null);
		table.addContainerProperty("Opcjonalno��", Label.class,  true);
		table.addContainerProperty("Warto��", String.class, null);
		table.setColumnWidth("Nazwa", 120);
		table.setColumnWidth("Typ", 120);
		table.setColumnWidth("Warto�� domy�lna", 120);
		table.setColumnWidth("Warto�� minimalna", 120);
		table.setColumnWidth("Warto�� maksymalna", 120);
		table.setColumnWidth("Opcjonalno��", 120);
		table.setColumnWidth("Warto��", 120);

		fillTableWithParameters(table, algorithm);

		Button confirmButton = new Button("Potwierd� wprowadzone dane");
		confirmButton.addListener(new ConfirmInputForAlgorithmListener(this));

		Button cancelButton = new Button("Anuluj");
		cancelButton.addListener(new CancelInputForAlgorithmListener(this));

		addComponent(table);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth(800f, UNITS_PIXELS);
		horizontalLayout.setHeight(80f, UNITS_PIXELS);
		horizontalLayout.addComponent(confirmButton);
		horizontalLayout.addComponent(cancelButton);
		horizontalLayout.setComponentAlignment(confirmButton,
				Alignment.MIDDLE_CENTER);
		horizontalLayout.setComponentAlignment(cancelButton,
				Alignment.MIDDLE_CENTER);

		addComponent(horizontalLayout);
	}

	private void fillTableWithParameters(Table table, Algorithm algorithm) {

		if (algorithm.getParameters() != null) {
			ArrayList<ArrayList<String>> fromSchema = SchemaParsing
					.fromSchema(algorithm.getParameters());
			table.setPageLength(fromSchema.size());
			Label name = new Label("");
			Label type = new Label("");
			Label defaultValue = new Label("");
			Label min = new Label("");
			Label max = new Label("");
			Label optional = null;
			String parameters = queue.getParameters();
			String[] split = new String[]{};
			if (parameters != null){
				split = parameters.split(" ");
			}
			int counter = 0;
			for (ArrayList<String> list : fromSchema) {
				String value = "";
				if (counter < split.length){
					value = split[counter];
					counter++;
				}
				try {
					name = new Label(list.get(0));
					type = new Label(list.get(1));
					defaultValue = new Label(list.get(2));
					min = new Label(list.get(3));
					max = new Label(list.get(4));
					boolean b = Boolean.parseBoolean(list.get(5));
					if (!b){
						optional = new Label("Obowi�zkowy");
					}
					else{
						optional = new Label("Opcjonalny");
					}
						
				} catch (Exception e) {
					e.printStackTrace();
				}
				table.addItem(new Object[] { name, type, defaultValue, min,
						max, optional , value }, name);
			}
		}

	}

	private String getParametersFromTable() {
		StringBuffer buffer = new StringBuffer("");
		Collection<Label> itemIds = (Collection<Label>) table.getItemIds();
		for (Label itemId : itemIds) {
			String value = (String) table.getContainerProperty(itemId,
					"Warto��").getValue();
			if (value == null || value.equals("")) {
				value = ((Label) table.getContainerProperty(itemId,
						"Warto�� domy�lna").getValue()).toString();
			}
			buffer.append(value);
			buffer.append(" ");
		}
		return buffer.toString();
	}

	private class ConfirmInputForAlgorithmListener implements
			Button.ClickListener {

		Window _window = null;

		public ConfirmInputForAlgorithmListener(Window window) {
			_window = window;
		}

		@Override
		public void buttonClick(ClickEvent event) {
			if (!validateTable(table)){
				return;
			}
			queue.setParameters(getParametersFromTable());
			((Window) _window.getParent()).removeWindow(_window);
		}

	}

	private class CancelInputForAlgorithmListener implements
			Button.ClickListener {

		Window _window = null;

		public CancelInputForAlgorithmListener(Window window) {
			_window = window;
		}

		@Override
		public void buttonClick(ClickEvent event) {
			((Window) _window.getParent()).removeWindow(_window);
		}

	}

	private boolean validateTable(Table table) {
		boolean isValid = true;
		Collection<Label> itemIds = (Collection<Label>)  table
				.getItemIds();
		try {
			for (Label row : itemIds) {
				String name = (String) ((Label) table.getContainerProperty(row, "Nazwa").getValue()).getValue();
				String defValue = ((Label) table.getContainerProperty(row,
						"Warto�� domy�lna").getValue()).toString();
				String min = ((Label) table.getContainerProperty(row,
						"Warto�� minimalna").getValue()).toString();
				String max = ((Label) table.getContainerProperty(row,
						"Warto�� maksymalna").getValue()).toString();
				String setValue = (String) table.getContainerProperty(row,
						"Warto��").getValue();
				String type = ((Label) table.getContainerProperty(row,
						"Typ").getValue()).toString();
				String optional = ((Label) table.getContainerProperty(row,
						"Opcjonalno��").getValue()).toString();
				if (type.equals("Integer") || type.equals("Long")) {
					int defVal = 0;
					int setVal = 0;
					int minVal = Integer.MIN_VALUE;
					if (!min.isEmpty()) {
						minVal = Integer.parseInt(min);
					}
					if (!defValue.isEmpty()) {
						defVal = Integer.parseInt(defValue);
						if (setValue.isEmpty()) {
							setVal = defVal;
						}
						else{
							setVal = Integer.parseInt(setValue);
						}
					} else {
						if (!setValue.isEmpty()) {
							setVal = Integer.parseInt(setValue);
							defVal = setVal;
						}
						else if (optional.equals("Obowi�zkowy")){
							throw new Exception("Brak ustawionej warto�ci dla parametru "
								+ name);
						}
						else{
							defVal = minVal;
							setVal = minVal;
						}
					}
					int maxVal = Integer.MAX_VALUE;
					if (!max.isEmpty()) {
						maxVal = Integer.parseInt(max);
					}
					if (minVal > maxVal || setVal < minVal || setVal > maxVal
							|| defVal < minVal || defVal > maxVal) {
						throw new Exception(
								"Warto�� przekracza dozwolony zakres dla parametru "
										+ name);
					}
				} else if (type.equals("Float") || type.equals("Double")) {
					Float defVal = 0.0f;
					Float setVal = 0.0f;
					Float minVal = Float.MIN_VALUE;
					if (!min.isEmpty()) {
						minVal = Float.parseFloat(min);
					}
					if (!defValue.isEmpty()) {
						defVal = Float.parseFloat(defValue);
						if (setValue.isEmpty()) {
							setVal = defVal;
						}
						else{
							setVal = Float.parseFloat(setValue);
						}
					} else {
						if (!setValue.isEmpty()) {
							setVal = Float.parseFloat(setValue);
							defVal = setVal;
						}
						else if (optional.equals("Obowi�zkowy")){
							throw new Exception("Brak ustawionej warto�ci dla parametru "
								+ name);
						}
						else{
							defVal = minVal;
							setVal = minVal;
						}
					}
					Float maxVal = Float.MAX_VALUE;
					if (!max.isEmpty()) {
						maxVal = Float.parseFloat(max);
					}
					if (minVal > maxVal || setVal < minVal || setVal > maxVal
							|| defVal < minVal || defVal > maxVal) {
						throw new Exception("Warto�� przekracza dozwolony zakres dla parametru "
								+ name);
					}
				} else if (type.equals("Boolean")) {
					if (!min.isEmpty() || !max.isEmpty()) {
						throw new Exception(
								"Typ Boolean nie mo�e mie� warto�ci maksymalnej");
					}
					if (defValue.isEmpty() && setValue.isEmpty() && optional.equals("Obowi�zkowy")) {
						throw new Exception("Brak warto�ci dla " + name);
					}
					else if(!defValue.isEmpty() && (!defValue.equalsIgnoreCase("true") && !defValue.equalsIgnoreCase("false"))){
						throw new Exception("Nieprawid�owa warto�� w kolumnie \"Warto�� domy�lna\"");
					}
					else if(!setValue.isEmpty() && (!setValue.equalsIgnoreCase("true") && !setValue.equalsIgnoreCase("false"))){
						throw new Exception("Nieprawid�owa warto�� w kolumnie \"Warto��\"");
					}
				} else if (type.equals("String")) {
					if (!min.isEmpty() || !max.isEmpty()) {
						throw new Exception(
								"String nie mo�e mie� warto�ci maksymalnej ani minimalnej dla "
										+ name);
					}
				}
			}
		} catch (Exception e) {
			String message = e.getMessage();
			if (e instanceof NumberFormatException) {
				message = "B��d konwersji typu!";
			}
			Notification notification = new Notification(
					"Tabela zawiera niepoprawne warto�ci! <BR/>" + "Szczeg�y b��du: "
							+ message, Notification.TYPE_ERROR_MESSAGE);
			notification.setDelayMsec(3000);
			this.getParent().showNotification(notification);
			isValid = false;
		}
		return isValid;
	}
}
