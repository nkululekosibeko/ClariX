package ell.one.clarix;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    private EditText fullNameInput, emailInput, cardNumberInput, expDateInput, cvvInput;
    private Button payButton;

    private String tutorId, date, startTime, endTime, docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Extract intent data (optional use)
        tutorId = getIntent().getStringExtra("tutorId");
        date = getIntent().getStringExtra("date");
        startTime = getIntent().getStringExtra("startTime");
        endTime = getIntent().getStringExtra("endTime");
        docId = getIntent().getStringExtra("docId");

        // Bind input fields
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        cardNumberInput = findViewById(R.id.cardNumberInput);
        expDateInput = findViewById(R.id.expDateInput);
        cvvInput = findViewById(R.id.cvvInput);
        payButton = findViewById(R.id.payButton);

        payButton.setOnClickListener(v -> {
            if (validateInputs()) {
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();

                // Pass payment result back to booking screen
                Intent resultIntent = new Intent();
                resultIntent.putExtra("paymentStatus", "success"); // ✅ This is critical
                resultIntent.putExtra("tutorId", tutorId);
                resultIntent.putExtra("date", date);
                resultIntent.putExtra("startTime", startTime);
                resultIntent.putExtra("endTime", endTime);
                resultIntent.putExtra("docId", docId);

                setResult(RESULT_OK, resultIntent); // Send result back
                finish(); // Exit this screen
            }
        });
    }

    private boolean validateInputs() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String cardNumber = cardNumberInput.getText().toString().trim();
        String expDate = expDateInput.getText().toString().trim();
        String cvv = cvvInput.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Name required");
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email required");
            return false;
        }

        if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
            cardNumberInput.setError("Card number must be 16 digits");
            return false;
        }

        if (!expDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            expDateInput.setError("Format should be MM/YY");
            return false;
        }

        if (cvv.length() != 3 || !cvv.matches("\\d{3}")) {
            cvvInput.setError("CVV must be 3 digits");
            return false;
        }

        return true;
    }
}
