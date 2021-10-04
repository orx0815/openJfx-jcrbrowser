package org.motorbrot.javafxjcrbrowser.csv;

import org.motorbrot.javafxjcrbrowser.JcrBrowserSceneController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 */
@Controller
public class CsvTabController {

  private static final Logger LOG = Logger.getLogger(CsvTabController.class.getName());
    
  @Autowired
  private JcrBrowserSceneController jcrBrowserSceneController;
  
  @FXML
  private Button fileButton;
  @FXML
  private Label fileLabel;
  @FXML
  private TableView<ObservableList<StringProperty>> csvTableView;
  
  @FXML
  private void fileButtonClicked(ActionEvent event) {

    FileChooser fileChooser = new FileChooser();
    File selectedFile = fileChooser.showOpenDialog(fileButton.getScene().getWindow());
    if (selectedFile != null) {

      InputStream fileInputStream;
      try {
        fileInputStream = new FileInputStream(selectedFile);
      }
      catch (FileNotFoundException ex) {
        LOG.log(Level.SEVERE, null, ex);
        jcrBrowserSceneController.getDebugTxt().appendText("ex: " + ex.getMessage() + "\n");
        return;
      }

      loadCvsTable(fileInputStream, selectedFile.getAbsolutePath());

    }

  }

  /**
   * display csv as table
   * @param is binary
   * @param dataSourceMsg Just a display text
   */
  public void loadCvsTable(InputStream is, String dataSourceMsg) {

    fileLabel.setText("Loading: " + dataSourceMsg);
    csvTableView.getItems().clear();
    csvTableView.getColumns().clear();
    Platform.runLater(() -> {

      Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
      List<CSVRecord> records;
      try {
        records = CSVFormat.EXCEL.parse(in).getRecords();
      }
      catch (IOException ex) {
        LOG.log(Level.SEVERE, null, ex);
        jcrBrowserSceneController.getDebugTxt().appendText("ex: " + ex.getMessage() + "\n");
        return;
      }
      finally {
        try {
          if (in != null) {
            in.close();
          }
        }
        catch (IOException ex) {
          LOG.log(Level.SEVERE, "Failed to load csv records.", ex);
        }
      }
      // headers in TableColumn
      if (records.size() == 0) {
        fileLabel.setText("Failed to load csv records.");
        return;
      }
      CSVRecord firstline = records.get(0);
      int col = 0;
      Iterator<String> columnIterator = firstline.iterator();
      while (columnIterator.hasNext()) {
        String header = columnIterator.next();
        TableColumn<ObservableList<StringProperty>, String> createColumn = createColumn(col, header);
        csvTableView.getColumns().add(createColumn);
        col++;
      }
      for (int i = 1; i < records.size(); i++) {
        CSVRecord line = records.get(i);
        // Add additional columns if necessary:
        for (int columnIndex = csvTableView.getColumns().size(); columnIndex < line.size(); columnIndex++) {
          csvTableView.getColumns().add(createColumn(columnIndex, ""));
        }

        // Add data to table:
        ObservableList<StringProperty> data = FXCollections.observableArrayList();
        line.forEach(cellValue -> {
          data.add(new SimpleStringProperty(cellValue));
        });
        csvTableView.getItems().add(data);
      }
      fileLabel.setText("Loaded: " + dataSourceMsg);

    });

  }

  private TableColumn<ObservableList<StringProperty>, String> createColumn(
          final int columnIndex, String columnTitle) {
    TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
    String title;
    if (columnTitle == null || columnTitle.trim().length() == 0) {
      title = "Column " + (columnIndex + 1);
    }
    else {
      title = columnTitle;
    }
    column.setText(title);
    column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
      @Override
      public ObservableValue<String> call(
              TableColumn.CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
        ObservableList<StringProperty> values = cellDataFeatures.getValue();
        if (columnIndex >= values.size()) {
          return new SimpleStringProperty("");
        }
        else {
          return cellDataFeatures.getValue().get(columnIndex);
        }
      }
    });
    return column;
  }


  
}
