/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minor_project_ii;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author vaibh
 */
public class DataSource {

    //<editor-fold defaultstate="collapsed" desc=" Database Connectivity ">
    public final Connection connect() {
        Connection conn = null;
        try {
            //?allowMultiQueries=true will allow us to execute more than one query at once, seprated by ';'
            String connectionString = "jdbc:postgresql://localhost:5432/mydb?allowMultiQueries=true";
            conn = DriverManager.getConnection(connectionString, "vaibh", "Vaibhav@121");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return conn;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" User verfication related database function ">
    boolean checkCredential(String userName, char[] password, String user) {
        try {
            Connection conn = connect();
            PreparedStatement pst;
            if (user.equals("Admin")) {
                String createInitialDatabaseTables = "CREATE TABLE IF NOT EXISTS admin (id TEXT PRIMARY KEY, password TEXT NOT NULL);"
                        + "CREATE TABLE IF NOT EXISTS staff (id TEXT PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, password TEXT NOT NULL, address TEXT NOT NULL, email_id TEXT NOT NULL, phone_no TEXT NOT NULL, department TEXT NOT NULL, room_alloted TEXT NOT NULL);"
                        + "CREATE TABLE IF NOT EXISTS student (id TEXT PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, course TEXT NOT NULL, password TEXT NOT NULL, address TEXT NOT NULL, email_id TEXT NOT NULL, phone_no TEXT NOT NULL, parent_email_id TEXT NOT NULL, parent_phone_no TEXT NOT NULL, room_alloted TEXT NOT NULL);"
                        + "CREATE TABLE IF NOT EXISTS requests (sr_no SERIAL PRIMARY KEY, user_id TEXT NOT NULL, subject TEXT NOT NULL, request TEXT NOT NULL, user_type TEXT NOT NULL);"
                        + "CREATE TABLE IF NOT EXISTS logbook (first_name TEXT NOT NULL, last_name TEXT NOT NULL, number_of_visitor INTEGER NOT NULL, person_to_meet TEXT NOT NULL, address TEXT NOT NULL, phone_no_of_visitor TEXT NOT NULL, date_and_time TIMESTAMP(0) NOT NULL, purpose_of_visit TEXT NOT NULL);";
                pst = conn.prepareStatement(createInitialDatabaseTables);
                if (pst.executeUpdate() > 0) {
                    pst = conn.prepareStatement("INSERT INTO admin VALUE('Admin', 'Admin')");
                    pst.executeUpdate();
                }
            }
            pst = conn.prepareStatement("SELECT * FROM " + user);
            pst.executeQuery();
            ResultSet rs = pst.getResultSet();
            while (rs.next()) {
                if (userName.equals(rs.getString("id"))) {
                    if (String.valueOf(password).equals(rs.getString("password"))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Admin related database queries ">
    // Code realted to Manage Student
    void addStudent(Object record[], JTextField txtAddStudentId) {
        try {
            Connection conn = connect();

            PreparedStatement pst = conn.prepareStatement("SELECT * FROM student WHERE id = ?");
            pst.setString(1, txtAddStudentId.getText());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                if (txtAddStudentId.getText().equals(rs.getString("id"))) {
                    JOptionPane.showMessageDialog(null, "Student id already exist!");
                    txtAddStudentId.setText("");
                    txtAddStudentId.grabFocus();
                    return;
                }
            }

            pst = conn.prepareStatement("INSERT INTO student(id, first_name, last_name, course, password, address, email_id, phone_no, parent_email_id, parent_phone_no, room_alloted) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
            for (Object record1 : record) {
                if (record1.toString().trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "Every field is mandatory!");
                    return;
                }
            }

            for (int i = 0; i < record.length; i++) {
                pst.setObject(i + 1, record[i]);
            }
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Student added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void viewStudent(JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM student");
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("course"),
                    rs.getString("room_alloted"), rs.getString("password"), rs.getString("phone_no"), rs.getString("email_id"),
                    rs.getString("parent_phone_no"), rs.getString("parent_email_id"), rs.getString("address")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    Object[] deleteStudentCheckDetails(JTextField txtDeleteStudentId) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM student WHERE id = ?");
            pst.setString(1, txtDeleteStudentId.getText());
            pst.executeQuery();
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getString("course"), rs.getString("room_alloted"),
                    rs.getString("phone_no"), rs.getString("email_id"), rs.getString("parent_phone_no"),
                    rs.getString("parent_email_id"), rs.getString("password"), rs.getString("address")
                };
                return record;
            }

            // if control does not jump back to Admin than record not found
            JOptionPane.showMessageDialog(null, "No record found!");
            txtDeleteStudentId.setText("");
            txtDeleteStudentId.grabFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void deleteStudent(JTextField txtDeleteStudentId) {
        try {
            Connection conn = connect();
            if (JOptionPane.showConfirmDialog(null, "Do you want to delete ID: " + txtDeleteStudentId.getText()) == JOptionPane.YES_OPTION) {
                PreparedStatement pst = conn.prepareStatement("DELETE FROM student WHERE id = ?");
                pst.setString(1, txtDeleteStudentId.getText());
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Record deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "No record found!");
                    txtDeleteStudentId.setText("");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    String studentId = null;

    Object[] modifyStudentCheckDetails(JTextField txtModifyStudentId) {
        try {
            studentId = txtModifyStudentId.getText();
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM student WHERE id = ?");
            pst.setString(1, txtModifyStudentId.getText());
            pst.executeQuery();
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getString("course"), rs.getString("room_alloted"),
                    rs.getString("phone_no"), rs.getString("email_id"), rs.getString("parent_phone_no"), rs.getString("parent_email_id"),
                    rs.getString("password"), rs.getString("address")};
                return record;
            }
            JOptionPane.showMessageDialog(null, "No record found!");
            txtModifyStudentId.setText("");
            txtModifyStudentId.grabFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void modifyStudent(Object record[]) {
        try {
            if (JOptionPane.showConfirmDialog(null, "Do you want to modify ID: " + studentId) == JOptionPane.YES_OPTION) {
                Connection conn = connect();
                PreparedStatement pst = conn.prepareStatement("UPDATE student SET first_name = ?, "
                        + "last_name = ?, id = ?, password = ?, address = ?, email_id =?, "
                        + "phone_no = ?, parent_email_id = ?, course = ?, parent_phone_no = ?, "
                        + "room_alloted = ? WHERE id = ?");
                for (int i = 0; i < record.length; i++) {
                    pst.setObject(i + 1, record[i]);
                }
                pst.setString(12, studentId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(null, "Record updated successfully!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void searchStudent(String id, String fname, String lname, JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM student WHERE id::text LIKE ? OR first_name::text LIKE ? OR last_name::text LIKE ?");
            pst.setString(1, id);
            pst.setString(2, fname);
            pst.setString(3, lname);
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("course"),
                    rs.getString("room_alloted"), rs.getString("password"), rs.getString("phone_no"), rs.getString("email_id"),
                    rs.getString("parent_phone_no"), rs.getString("parent_email_id"), rs.getString("address")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    // Code related to Manage Staff
    void addStaff(Object record[], JTextField txtAddStaffId) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff WHERE id = ?");
            pst.setString(1, txtAddStaffId.getText());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                if (txtAddStaffId.getText().equals(rs.getString("id"))) {
                    JOptionPane.showMessageDialog(null, "Staff id already exist!");
                    txtAddStaffId.setText("");
                    txtAddStaffId.grabFocus();
                    return;
                }
            }

            for (Object record1 : record) {
                if (record1.toString().trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "Every field is mandatory!");
                    return;
                }
            }

            pst = conn.prepareStatement("INSERT INTO staff VALUES(?,?,?,?,?,?,?,?,?)");

            for (int i = 0; i < record.length; i++) {
                pst.setObject(i + 1, record[i]);
            }
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Staff added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void viewStaff(JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff");
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"),
                    rs.getString("password"), rs.getString("department"), rs.getString("phone_no"),
                    rs.getString("email_id"), rs.getString("address"), rs.getString("room_alloted")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    Object[] deleteStaffCheckDetails(JTextField txtDeleteStaffId) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff WHERE id = ?");
            pst.setString(1, txtDeleteStaffId.getText());
            pst.executeQuery();
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getString("room_alloted"), rs.getString("phone_no"),
                    rs.getString("email_id"), rs.getString("department"), rs.getString("password"), rs.getString("address")};
                return record;
            }
            JOptionPane.showMessageDialog(null, "No record found!");
            txtDeleteStaffId.setText("");
            txtDeleteStaffId.grabFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void deleteStaff(JTextField txtDeleteStaffId) {
        try {
            if (JOptionPane.showConfirmDialog(null, "Do you want to delete ID: " + txtDeleteStaffId.getText()) == JOptionPane.YES_OPTION) {
                Connection conn = connect();
                PreparedStatement pst = conn.prepareStatement("DELETE FROM staff WHERE id = ?");
                pst.setString(1, txtDeleteStaffId.getText());
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Record deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Record not found!");
                    txtDeleteStaffId.setText("");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    String staffId = null;

    Object[] modifyStaffCheckDetails(JTextField txtModifyStaffId) {
        staffId = txtModifyStaffId.getText();
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff WHERE id = ?");
            pst.setString(1, txtModifyStaffId.getText());
            pst.executeQuery();
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getString("room_alloted"), rs.getString("phone_no"),
                    rs.getString("email_id"), rs.getString("department"), rs.getString("password"), rs.getString("address")};
                return record;
            }
            JOptionPane.showMessageDialog(null, "No record found!");
            txtModifyStaffId.setText("");
            txtModifyStaffId.grabFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void modifyStaff(Object record[]) {
        try {
            if (JOptionPane.showConfirmDialog(null, "Do you want to modify ID: " + staffId) == JOptionPane.YES_OPTION) {
                Connection conn = connect();
                PreparedStatement pst = conn.prepareStatement("UPDATE staff SET first_name = ?, "
                        + "last_name = ?, id = ?, password = ?, address = ?, email_id =?, "
                        + "phone_no = ?, department = ?, room_alloted = ? WHERE id = ?");
                for (int i = 0; i < record.length; i++) {
                    pst.setObject(i + 1, record[i]);
                }
                pst.setString(10, staffId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(null, "Record Updated!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void searchStaff(String id, String fname, String lname, JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff WHERE id::text LIKE ? OR first_name::text LIKE ? OR last_name::text LIKE ?");
            pst.setString(1, id);
            pst.setString(2, fname);
            pst.setString(3, lname);
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"),
                    rs.getString("department"), rs.getString("room_alloted"), rs.getString("password"),
                    rs.getString("phone_no"), rs.getString("email_id"), rs.getString("address")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void viewUpdateRequest(JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM requests");
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            int i = 1;
            while (rs.next()) {
                Object record[] = {
                    rs.getString("sr_no"), rs.getString("user_id"), rs.getString("user_type"),
                    rs.getString("subject"), rs.getString("request")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void deleteUpdateRequest(String id, JTable tbl) {
        try {
            if (JOptionPane.showConfirmDialog(null, "Do you want to delete ID: " + id) == JOptionPane.YES_OPTION) {
                Connection conn = connect();
                PreparedStatement pst = conn.prepareStatement("DELETE FROM requests WHERE sr_no = ?");
                pst.setInt(1, Integer.parseInt(id));
                pst.executeUpdate();
                pst = conn.prepareStatement("update requests set sr_no = 10000 + nextval('requests_sr_no_seq')");
                pst.executeUpdate();
                pst = conn.prepareStatement("alter sequence requests_sr_no_seq restart with 1");
                pst.executeUpdate();
                pst = conn.prepareStatement("update requests set sr_no = nextval('requests_sr_no_seq')");
                pst.executeUpdate();
                JOptionPane.showMessageDialog(null, "Record Deleted!");
                viewUpdateRequest(tbl);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    //Code related to Manage Admins
    void addAdmin(JTextField txtAddAdminUserId, JTextField txtAddAdminPassword) {
        try {
            if (txtAddAdminUserId.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null, "User ID is empty!");
                txtAddAdminUserId.grabFocus();
                return;
            } else if (txtAddAdminPassword.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(null, "Password is empty!");
                txtAddAdminPassword.grabFocus();
                return;
            }
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM admin WHERE id = ?");
            pst.setString(1, txtAddAdminUserId.getText());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Admin id already exists!");
                txtAddAdminUserId.setText("");
                txtAddAdminUserId.grabFocus();
                return;
            }

            pst = conn.prepareStatement("INSERT INTO admin (id, password) VALUES (?,?)");
            pst.setString(1, txtAddAdminUserId.getText());
            pst.setString(2, txtAddAdminPassword.getText());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Admin added successfully!");
            txtAddAdminUserId.setText("");
            txtAddAdminPassword.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void viewAdmin(JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM admin");
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            int i = 0;
            while (rs.next()) {
                i++;
                Object record[] = {
                    i, rs.getString("id"), rs.getString("password")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    String adminId = null;

    String adminCheckDetails(JTextField txtAdminUserId) {
        adminId = txtAdminUserId.getText();
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM admin WHERE id = ?");
            pst.setString(1, txtAdminUserId.getText());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
            JOptionPane.showMessageDialog(null, "No record found!");
            txtAdminUserId.setText("");
            txtAdminUserId.grabFocus();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void deleteAdmin(JTextField txtDeleteAdminUserId) {
        try {
            if (JOptionPane.showConfirmDialog(null, "Do you want to delete ID: " + txtDeleteAdminUserId.getText()) == JOptionPane.YES_OPTION) {
                Connection conn = connect();
                PreparedStatement pst = conn.prepareStatement("DELETE FROM admin WHERE id = ?");
                pst.setString(1, txtDeleteAdminUserId.getText());
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Record deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Record not found!");
                    txtDeleteAdminUserId.setText("");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void modifyAdmin(JTextField txtModifyAdminUserId, JTextField txtModifyAdminPassword) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("UPDATE admin SET id = ?, password = ? where id = ?");
            pst.setString(1, txtModifyAdminUserId.getText());
            pst.setString(2, txtModifyAdminPassword.getText());
            pst.setString(3, adminId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Record updated successfully");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc=" Staff related database queries ">
    Object viewProfileStaff(String id) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff WHERE id = ?");
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object profile[] = {
                    rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("room_alloted"),
                    rs.getString("phone_no"), rs.getString("email_id"), rs.getString("department"), rs.getString("password"),
                    rs.getString("address")
                };
                return profile;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void addLog(Object object[]) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("INSERT INTO logbook VALUES (?,?,?,?,?,?,?,?)");
            for (Object object1 : object) {
                if (object1.toString().trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "Every field is mandatory!");
                    System.out.println(object.length);
                    return;
                }
            }

            pst.setString(1, object[0].toString());
            pst.setString(2, object[1].toString());
            pst.setInt(3, Integer.parseInt(object[2].toString()));
            pst.setString(4, object[3].toString());
            pst.setString(5, object[4].toString());
            pst.setString(6, object[5].toString());
            pst.setTimestamp(7, getCurrentTimeStamp());
            pst.setString(8, object[6].toString());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Log added successfully");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    //returns current timestamp
    private java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }

    void viewLogbook(JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM logbook");
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getInt("number_of_visitor"),
                    rs.getDate("date_and_time"), rs.getTime("date_and_time"), rs.getString("person_to_meet"), rs.getString("purpose_of_visit"),
                    rs.getString("address"), rs.getString("phone_no_of_visitor")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void searchLogbook(JTable tbl, String fname, String lname, java.util.Date date) {
        try {
            Connection conn = connect();
            PreparedStatement pst;
            /* Without this if else condition,if we enter only first name or last name,
               the code will throw a null pointer exception in the date column*/
            if (date != null) {
                pst = conn.prepareStatement("SELECT * FROM logbook WHERE first_name LIKE ? OR last_name LIKE ? OR date_and_time::date = ?");
                pst.setString(1, fname);
                pst.setString(2, lname);
//            date = new java.sql.Date(date.getTime());
                pst.setDate(3, new java.sql.Date(date.getTime()));
            } else {
                pst = conn.prepareStatement("SELECT * FROM logbook WHERE first_name LIKE ? OR last_name LIKE ?");
                pst.setString(1, fname);
                pst.setString(2, lname);
            }
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getInt("number_of_visitor"),
                    rs.getDate("date_and_time"), rs.getTime("date_and_time"), rs.getString("person_to_meet"),
                    rs.getString("purpose_of_visit"), rs.getString("address"), rs.getString("phone_no_of_visitor")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }

    void searchStaffContact(String fname, String lname, String id, JTable tbl) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM staff WHERE id::text LIKE ? OR first_name::text LIKE ? OR last_name::text LIKE ?");
            pst.setString(1, id);
            pst.setString(2, fname);
            pst.setString(3, lname);
            ResultSet rs = pst.executeQuery();
            DefaultTableModel tm = (DefaultTableModel) tbl.getModel();
            tm.setRowCount(0);
            while (rs.next()) {
                Object record[] = {
                    rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("phone_no"),
                    rs.getString("email_id"), rs.getString("room_alloted"), rs.getString("department")
                };
                tm.addRow(record);
            }
            if (tbl.getRowCount() <= 0) {
                JOptionPane.showMessageDialog(null, "No record found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Student related database queries">
    Object viewProfileStudent(String id) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM student WHERE id = ?");
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object record[] = {
                    rs.getString("first_name"), rs.getString("last_name"), rs.getString("id"), rs.getString("password"),
                    rs.getString("address"), rs.getString("email_id"), rs.getString("phone_no"), rs.getString("parent_email_id"),
                    rs.getString("course"), rs.getString("parent_phone_no"), rs.getString("room_alloted")
                };
                return record;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
        return null;
    }

    void updateRequest(JTextField txtRequestUpdateSubject, javax.swing.JTextArea txtRequestUpdateRequest, String studentID, String user_type) {
        try {
            Connection conn = connect();
            PreparedStatement pst = conn.prepareStatement("INSERT INTO requests (user_id, subject, request, user_type) VALUES(?,?,?,?)");
            if (txtRequestUpdateSubject.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Subject is empty!");
                txtRequestUpdateSubject.grabFocus();
                return;
            } else if (txtRequestUpdateRequest.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Request box is empty!");
                txtRequestUpdateRequest.grabFocus();
                return;
            }
            pst.setString(1, studentID);
            pst.setString(2, txtRequestUpdateSubject.getText());
            pst.setString(3, txtRequestUpdateRequest.getText());
            pst.setString(4, user_type);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Request sent successfully!");
            txtRequestUpdateSubject.setText("");
            txtRequestUpdateRequest.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong: " + e.getMessage());
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" JTextfield verfication  ">
    // function that allow only integer in a textField and also limit the number of character
    void onlyInt(JTextField txtField, int maxLength, java.awt.event.KeyEvent evt) {
        String txt = txtField.getText();
        int length = txt.length();

        if (evt.getKeyChar() >= '0' && evt.getKeyChar() <= '9') {
            if (length < maxLength) {
                txtField.setEditable(true);
            } else {
                txtField.setEditable(false);
                txtField.setBackground(Color.WHITE);
            }
        } else {
            if (evt.getExtendedKeyCode() == KeyEvent.VK_BACK_SPACE || evt.getExtendedKeyCode() == KeyEvent.VK_DELETE) {
                txtField.setEditable(true);
            } else {
                txtField.setEditable(false);
                txtField.setBackground(Color.WHITE);
            }
        }
    }

    //function that only allow character and not integer
    void onlyChar(JTextField txtField, java.awt.event.KeyEvent evt) {
        char c = evt.getKeyChar();

        if (Character.isLetter(c) || Character.isWhitespace(c) || Character.isISOControl(c)) {
            txtField.setEditable(true);
        } else {
            txtField.setEditable(false);
            txtField.setBackground(Color.WHITE);
        }
    }
    //</editor-fold>

}
