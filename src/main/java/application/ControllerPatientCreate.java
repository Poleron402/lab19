package application;

import java.sql.*;
import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientCreate {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String getNewPatientForm(Model model) {
		// return blank form for new patient registration
		model.addAttribute("patient", new PatientView());
		return "patient_register";
	}

	/*
	 * Process data from the patient_register form
	 */
	@PostMapping("/patient/new")
	public String createPatient(PatientView p, Model model) {

		/*
		 * insert to patient table
		 */

		try (Connection con = getConnection()) {
			PreparedStatement docQ = con.prepareStatement("SELECT id from doctor where last_name=?");
			docQ.setString(1, p.getPrimaryName());
			ResultSet doc = docQ.executeQuery();
			int docId = 0;
			if (doc.next()){
				docId = doc.getInt(1);
			}



			PreparedStatement ps = con.prepareStatement("insert into patient(last_name, first_name, birthdate, ssn, street, city, state, zipcode, doctor_id ) values(?, ?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, p.getLast_name());
			ps.setString(2, p.getFirst_name());
			ps.setDate(3, Date.valueOf(p.getBirthdate()));
			ps.setString(4, p.getSsn());
			ps.setString(5, p.getStreet());
			ps.setString(6, p.getCity());
			ps.setString(7, p.getState());
			ps.setString(8, p.getZipcode());
			ps.setInt(9, docId);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) p.setId(rs.getInt(1));
			// Check if Primary Name matches Doctor name before execute update.
			// Note: the lab document gives the impression that primaryName is the doctor's last name.

			/*
			 * validate doctor last name and find the doctor id
			 */



//			ResultSet rs = ps.getGeneratedKeys();
//			if (rs.next()) p.setId(rs.getInt(1));

			// display message and patient information
			model.addAttribute("message", "Registration successful.");
			model.addAttribute("patient", p);


			return "patient_show";

		} catch (SQLException e) {

			/*
			 * on error
			 * model.addAttribute("message", some error message);
			 * model.addAttribute("patient", p);
			 * return "patient_register";
			 */

			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_register";
		}

	}

	/*
	 * Request blank form to search for patient by id and name
	 */
	@GetMapping("/patient/edit")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String showPatient(PatientView p, Model model) {

		// TODO   search for patient by id and name
		// if found, return "patient_show", else return error message and "patient_get"

		try (Connection con = getConnection()) {

			PreparedStatement ps = con.prepareStatement("select * from patient where id=? and last_name=?"); // last_name, first_name, primaryName
			ps.setInt(1, p.getId());
			ps.setString(2, p.getLast_name());

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
//				p.setId(rs.getInt(1));
//				p.setLast_name(rs.getString(2));
//				p.setPrimaryName(rs.getString(4));

				p.setId(rs.getInt(1));
				p.setFirst_name(rs.getString(3));
				p.setLast_name(rs.getString(4));
				p.setBirthdate(rs.getString(5));
				p.setStreet(rs.getString(6));
				p.setCity(rs.getString(7));
				p.setState(rs.getString(8));
				p.setZipcode(rs.getString(9));

				PreparedStatement docQ = con.prepareStatement("SELECT last_name from doctor where id=?");
				docQ.setString(1, rs.getString(10));
				ResultSet doc = docQ.executeQuery();

				if (doc.next()) {
					p.setPrimaryName(doc.getString(1));
				}

				model.addAttribute("patient", p);
				return "patient_show";

			} else {
				model.addAttribute("message", "patient not found.");
				model.addAttribute("patient", p);
				return "patient_get";
			}

		} catch (SQLException e) {
			System.out.println("SQL error in getPatient "+e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
		}
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}
}
