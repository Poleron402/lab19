package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
				pharmId = rsPharm.getInt("pharmID");
				phone = rsPharm.getNString("phone");
			}
			// TODO find the patient information
			String patientLastName = p.getPatientLastName();
			if (patientLastName.isEmpty()){
				model.addAttribute("message", "A patient with this name was not found");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}
			// TODO find the prescription
			Integer RXID = p.getRxid();

			/*
			 * have we exceeded the number of allowed refills
			 * the first fill is not considered a refill.
			 */
			int numberOfRefills = p.getRefills();
			PreparedStatement checkRef = conn.prepareStatement("select number_refills from prescription where RXID = ?");
			checkRef.setInt(1, RXID);
			ResultSet refills = checkRef.executeQuery();
			int numRef = refills.getInt("number_refills");
			if (numberOfRefills > numRef+1){
				model.addAttribute("message", "Exceeded the number of refills, see the doctor to renew prescription");
				model.addAttribute("prescription", p);
				return "prescription_fill";
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
			if (!checkDocRes.next()){
				model.addAttribute("message", "Exceeded the number of refills, see the doctor to renew prescription");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			/*
			 * calculate cost of prescription
			 */
			// TODO

			// TODO save updated prescription

			// show the updated prescription with the most recent fill information
			model.addAttribute("message", "Prescription filled.");
			model.addAttribute("prescription", p);
			return "prescription_show";
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error." + e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}
	}
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}