package com.example.midexam;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentId;
    private RadioButton rbEnrolled, rbNotEnrolled;
    private Button btnAdd, btnView, btnEdit, btnDelete;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        db = new DatabaseHelper(this);

        // Bind UI views
        initViews();

        // Set up button click listeners
        setupListeners();
    }

    private void initViews() {
        etStudentName = findViewById(R.id.etStudentName);
        etStudentId = findViewById(R.id.etStudentId);
        rbEnrolled = findViewById(R.id.rbEnrolled);
        rbNotEnrolled = findViewById(R.id.rbNotEnrolled);

        btnAdd = findViewById(R.id.btnAdd);
        btnView = findViewById(R.id.btnView);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(v -> addStudent());
        btnView.setOnClickListener(v -> viewStudents());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());
    }

    private boolean isEnrolled() {
        return rbEnrolled.isChecked();
    }

    private void addStudent() {
        String id = etStudentId.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this,
                    "Please enter Student Name and ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = db.addStudent(id, name, isEnrolled());

        if (success) {
            Toast.makeText(this,
                    "Student added successfully",
                    Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this,
                    "Failed! Student ID may already exist",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Views all students in an interactive dialog.
     * Tapping any student record automatically loads their details into the input fields
     * so they can be easily edited or deleted.
     */
    private void viewStudents() {
        Cursor cursor = db.getAllStudents();

        if (cursor.getCount() == 0) {
            Toast.makeText(this,
                    "No student data found",
                    Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        List<String> studentDisplayList = new ArrayList<>();
        List<String> studentIds = new ArrayList<>();
        List<String> studentNames = new ArrayList<>();
        List<Boolean> studentEnrolledList = new ArrayList<>();

        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            boolean enrolled = cursor.getInt(2) == 1;

            studentIds.add(id);
            studentNames.add(name);
            studentEnrolledList.add(enrolled);

            String formattedRecord = "ID: " + id + "\nName: " + name + "\nEnrolled: " + (enrolled ? "Yes" : "No");
            studentDisplayList.add(formattedRecord);
        }

        cursor.close();

        CharSequence[] items = studentDisplayList.toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
                .setTitle("Student Records (Tap to Select)")
                .setItems(items, (dialog, which) -> {
                    // Populate fields with selected student's data for easy editing/deleting
                    String selectedId = studentIds.get(which);
                    String selectedName = studentNames.get(which);
                    boolean isEnrolledStatus = studentEnrolledList.get(which);

                    etStudentId.setText(selectedId);
                    etStudentName.setText(selectedName);
                    if (isEnrolledStatus) {
                        rbEnrolled.setChecked(true);
                    } else {
                        rbNotEnrolled.setChecked(true);
                    }

                    Toast.makeText(this,
                            "Loaded " + selectedName + " (ID: " + selectedId + ") for editing/deleting",
                            Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private void editStudent() {
        String id = etStudentId.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this,
                    "Please select or enter a Student ID and Name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = db.updateStudent(id, name, isEnrolled());

        if (success) {
            Toast.makeText(this,
                    "Student updated successfully",
                    Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this,
                    "Student ID not found in database",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStudent() {
        String id = etStudentId.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this,
                    "Please select or enter a Student ID to delete",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete student ID " + id + "?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = db.deleteStudent(id);

                    Toast.makeText(this,
                            success ? "Student deleted successfully"
                                    : "Student ID not found",
                            Toast.LENGTH_SHORT).show();

                    if (success) {
                        clearFields();
                    }
                })
                .show();
    }

    private void clearFields() {
        etStudentName.setText("");
        etStudentId.setText("");
        rbEnrolled.setChecked(true);
    }
}
