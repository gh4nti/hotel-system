package com.hotel.controllers;

import com.hotel.dao.RoomDAO;
import com.hotel.models.Room;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ViewRoomsController {

	@FXML
	private TableView<Room> table;
	@FXML
	private TableColumn<Room, Integer> idCol;
	@FXML
	private TableColumn<Room, String> typeCol;
	@FXML
	private TableColumn<Room, Double> priceCol;
	@FXML
	private TableColumn<Room, Boolean> availCol;

	private RoomDAO dao = new RoomDAO();

	@FXML
	public void initialize() {
		try {
			idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
			typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
			priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
			availCol.setCellValueFactory(new PropertyValueFactory<>("available"));

			ObservableList<Room> list = FXCollections.observableArrayList(dao.getAllRooms());
			table.setItems(list);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}