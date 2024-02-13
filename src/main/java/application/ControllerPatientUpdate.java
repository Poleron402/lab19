package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

import java.sql.*;



/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientUpdate {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {

		PatientView pv = new PatientView();

		// TODO search for patient by id
		//  if not found, return to home page using return "index"; 
		//  else create PatientView and add to model.
		// model.addAttribute("message", some message);
		// model.addAttribute("patient", pv
		// return editable form with patient data

		pv.setId(id);

		try (Connection con = getConnection()){

			PreparedStatement ps = con.prepareStatement("SELECT * from patient where id=?"); // last_name, first_name, birthdate, ssn, street, city, state, zipcode, primaryName
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			if (!rs.next()) {
				return "index";
			} else {
				pv.setId(id);
				pv.setLast_name(rs.getString(1));
				pv.setFirst_name(rs.getString(2));
				pv.setBirthdate(rs.getString(3));
				pv.setSsn(rs.getString(4));
				pv.setStreet(rs.getString(5));
				pv.setCity(rs.getString(6));
				pv.setState(rs.getString(7));
				pv.setZipcode(rs.getString(8));
				pv.setPrimaryName(rs.getString(9));


				model.addAttribute("patient", pv);
				return "patient_edit";
			}

		} catch (SQLException e) {
			System.out.println("SQL error in getPatient "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", pv);
			return "patient_get";
		}

}
	
	
	/*
	 * Process changes from patient_edit form
	 *  Primary doctor, street, city, state, zip can be changed
	 *  ssn, patient id, name, birthdate, ssn are read only in template.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView p, Model model) {

		// validate doctor last name 
		
		// TODO 
		
		// TODO update patient profile data in database

		// ---------------------------------------------------------------


		try (Connection con = getConnection()) {
			PreparedStatement ps = con.prepareStatement("update patient set street=?,  city=?, state=?, zipcode=?, primaryName=? where id=? ");
			ps.setString(1, p.getStreet());
			ps.setString(2, p.getCity());
			ps.setString(3, p.getState());
			ps.setString(4, p.getZipcode());

			// Check if Primary Name matches Doctor name before execute update.
			// Note: the lab document gives the impression that primaryName is the doctor's last name.

			PreparedStatement docQ = con.prepareStatement("SELECT id from doctor where last_name=?");
			docQ.setString(1, p.getPrimaryName());
			ResultSet doc = docQ.executeQuery();

			if (doc.next()) {
				ps.setString(5, doc.getString(1));
			}

			ps.setInt(6, p.getId());

			int rc = ps.executeUpdate();

			if (rc == 1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("patient", p);
				return "patient_show";

			} else {
				model.addAttribute("message", "Error. Update was not successful");
				model.addAttribute("patient", p);
				return "patient_edit";
			}

			// return "invalid_doctor"; // Not sure if this is what should be returned when no match with a doctor.

		} catch (SQLException e) {

			/*
			 * on error
			 * model.addAttribute("message", some error message);
			 * model.addAttribute("patient", p);
			 * return "patient_register";
			 */

			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
		}

		// ---------------------------------------------------------------

		// model.addAttribute("message", some message);
		// model.addAttribute("patient", p)
	}

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
	
	
}
