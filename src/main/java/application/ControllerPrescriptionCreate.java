package application;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

@Controller
public class ControllerPrescriptionCreate {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*
	 * Doctor requests blank form for new prescription.
	 */
	@GetMapping("/prescription/new")
	public String getPrescriptionForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_create";
	}

	// process data entered on prescription_create form
	@PostMapping("/prescription")
	public String createPrescription(PrescriptionView p, Model model) {

		System.out.println("createPrescription " + p);


		try(Connection conn = getConnection()){
			/*
			 * valid doctor name and id
			 */
			//TODO
//			Boolean check = false;
			PreparedStatement checkDoc = conn.prepareStatement("select * from doctor where last_name = ? and id = ?");
			checkDoc.setString(1, p.getDoctorLastName());
			checkDoc.setInt(2, p.getDoctor_id());
			ResultSet rsDoc = checkDoc.executeQuery();

			if (!rsDoc.next()){
				model.addAttribute("message", "Doctor not found.");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}
			/*
			 * valid patient name and id
			 */
			//TODO
			PreparedStatement checkPatient = conn.prepareStatement("select * from patient where first_name = ? and last_name = ? and id = ?");
			checkPatient.setString(1, p.getPatientFirstName());
			checkPatient.setString(2, p.getPatientLastName());
			checkPatient.setInt(3, p.getPatient_id());
			ResultSet rsPat = checkPatient.executeQuery();
			if (!rsPat.next()){
				model.addAttribute("message", "No patient with entered credentials found");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}

			/*
			 * valid drug name
			 */
			//TODO
			PreparedStatement checkDrug = conn.prepareStatement("select * from drug where name = ?");
			checkDrug.setString(1, p.getDrugName());
			ResultSet rsDrug = checkDrug.executeQuery();
			if (!rsDrug.next()){
				model.addAttribute("message", "No drug with this name found in the database");
				model.addAttribute("prescription", p);
				return "prescription_create";
			}
			/*
			 * insert prescription
			 */
			PreparedStatement getId = conn.prepareStatement("select drugID from drug where name = ?");
			getId.setString(1, p.getDrugName());
			ResultSet resultSet = getId.executeQuery();
			int drug_id = 0;
			// Check if the ResultSet has any rows
			if (resultSet.next()) {
				// Retrieve the value from the ResultSet and assign it to the int variable
				drug_id = resultSet.getInt("drugID");
			}
			PreparedStatement addPrescription = conn.prepareStatement(
					"insert into prescription (quantity, number_refills, doctor_id, patient_id, pharmID, drugID) values(?, ?, ?, ?, 1, ?);");
			addPrescription.setInt(1, p.getQuantity());
			addPrescription.setInt(2, p.getRefillsRemaining());
			addPrescription.setInt(3, p.getDoctor_id());
			addPrescription.setInt(4, p.getPatient_id());
			addPrescription.setInt(5, drug_id);
			//TODO
			model.addAttribute("message", "Prescription created.");
			model.addAttribute("prescription", p);
			addPrescription.executeUpdate();
			return "prescription_show";

		}catch (SQLException e) {
			e.printStackTrace();
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_create";
		}
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
