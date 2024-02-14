package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

@Controller
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Patient requests form to fill prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_fill";
	}

	// process data from prescription_fill form
	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model) {
		try (Connection conn = getConnection()) {


			/*
			 * valid pharmacy name and address, get pharmacy id and phone
			 */
			// TODO
			int pharmId;
			String phone;
			PreparedStatement checkPh = conn.prepareStatement("select * from pharmacy where name = ? and address = ?");

			checkPh.setString(1, p.getPharmacyName());
			checkPh.setString(2, p.getPharmacyAddress());
			ResultSet rsPharm = checkPh.executeQuery();
			if (!rsPharm.next()){
				model.addAttribute("message", "Cannot locate such pharmacy");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}else{
				pharmId = rsPharm.getInt("pharmacy_id");
				phone = rsPharm.getNString("phone");
			}
			// TODO find the patient information
			String patientLastName = p.getPatientLastName();
			String patientFirstName = "";
			int patId = -1;
			if (patientLastName.isEmpty()){
				model.addAttribute("message", "A patient with this name was not found");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}else{
				PreparedStatement getPatient = conn.prepareStatement("select * from patient join prescription on patient.id = prescription.patient_id where patient.last_name = ? and prescription.RXID = ?");
				getPatient.setString(1, patientLastName);
				getPatient.setInt(2, p.getRxid());
				ResultSet patientRs = getPatient.executeQuery();
				if (patientRs.next()){
					patId = patientRs.getInt("id");
					patientFirstName = patientRs.getString("first_name");
				}
			}

			// TODO find the prescription
			int RXID = p.getRxid();
			String drugName = "";
			PreparedStatement drugLookup= conn.prepareStatement("select * from drug join prescription on drug.drug_id = prescription.drug_id where prescription.RXID = ?");
			drugLookup.setInt(1, RXID);
			ResultSet drugRs = drugLookup.executeQuery();
			if (drugRs.next()){
				drugName = drugRs.getString("name");
			}else{
				model.addAttribute("message", "No such prescription found");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}


			/*
			 * have we exceeded the number of allowed refills
			 * the first fill is not considered a refill.
			 */
			PreparedStatement checkRef = conn.prepareStatement("select number_refills from prescription where RXID = ?");
			checkRef.setInt(1, RXID);
			ResultSet refills = checkRef.executeQuery();
			int numRef = 0;
			if (refills.next()) {
				numRef = refills.getInt("number_refills");
				PreparedStatement checkRequests = conn.prepareStatement("select * from prescription_fill where RXID = ?");
				checkRequests.setInt(1, RXID);
				ResultSet requestRs = checkRequests.executeQuery();
				if(requestRs.next()){
					if (numRef == 0) {
						model.addAttribute("message", "Exceeded the number of refills, see the doctor to renew prescription");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}else{
						PreparedStatement updateRefills = conn.prepareStatement("update prescription set number_refills = ? where RXID = ?");
						updateRefills.setInt(1, numRef-1);
						updateRefills.setInt(2, RXID);
						updateRefills.executeUpdate();
						numRef -= 1;
					}
				}

			}
			// TODO

			/*
			 * get doctor information
			 */
			PreparedStatement getDoc = conn.prepareStatement(
					"select * from doctor join prescription on " +
							"doctor.id = prescription.doctor_id where prescription.RXID = ?");
			// TODO
			getDoc.setInt(1, RXID);
			ResultSet checkDocRes = getDoc.executeQuery();
			String docFirstName = "";
			String docLastName = "";
			int docId = -1;
			if (!checkDocRes.next()){
				model.addAttribute("message", "No doctor found");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}else{
				docFirstName =checkDocRes.getString("first_name");
				docLastName = checkDocRes.getString("last_name");
				docId = checkDocRes.getInt("id");
			}

			/*
			 * calculate cost of prescription
			 */
			// TODO
			double cost = 0.0;
			double priceTotal = 0.0;
			int quantityPrescribed = 0;
			int amountDistributed;
			PreparedStatement getAmount = conn.prepareStatement("select * from cost join prescription on cost.drug_id = prescription.drug_id where prescription.RXID = ?");
			getAmount.setInt(1, RXID);
			ResultSet rsAmount = getAmount.executeQuery();
			if(rsAmount.next()){
				amountDistributed = rsAmount.getInt("amount");
				cost = rsAmount.getDouble("cost");
				quantityPrescribed = rsAmount.getInt("quantity");
				priceTotal = cost*quantityPrescribed/amountDistributed;
			}else{
				model.addAttribute("message", "Error calculating the total cost");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}
			// TODO save updated prescription
			PreparedStatement saveRefill = conn.prepareStatement("insert into prescription_fill (date, price, pharmacy_id, RXID) values (?, ?, ?, ?)");
			saveRefill.setString(1, LocalDate.now().toString());
			saveRefill.setDouble(2, priceTotal);
			saveRefill.setInt(3, pharmId);
			saveRefill.setInt(4, RXID);
			saveRefill.executeUpdate();
			//setting up things for the prescription model
			p.setPharmacyID(pharmId);
			p.setDateFilled(LocalDate.now().toString());
			p.setPharmacyPhone(phone);
			p.setCost(""+priceTotal);
			p.setQuantity(quantityPrescribed);
			p.setPatientFirstName(patientFirstName);
			p.setDrugName(drugName);
			p.setDoctorLastName(docLastName);
			p.setDoctorFirstName(docFirstName);
			p.setPatient_id(patId);
			p.setDoctor_id(docId);
			p.setRefills(numRef);
			// show the updated prescription with the most recent fill information
			model.addAttribute("message", "Prescription filled.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error. " + e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}
	}
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}