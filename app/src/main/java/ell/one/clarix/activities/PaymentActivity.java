package ell.one.clarix.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.Serializable;

import ell.one.clarix.R;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PayHere";
    private Button payButton;
    private TextView paymentStatusText;

    // Passed via intent
    private String tutorId, date, startTime, endTime, docId;
    private String studentName, studentEmail, studentPhone;
    private double sessionPrice;

    private final ActivityResultLauncher<Intent> paymentResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (serializable instanceof PHResponse) {
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            String msg = response.isSuccess()
                                    ? "✅ Payment Success\nRef: " + response.getData()
                                    : "❌ Payment Failed\n" + response.toString();
                            Log.d(TAG, msg);
                            paymentStatusText.setText(msg);
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    paymentStatusText.setText("❌ User canceled the payment.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind UI
        payButton = findViewById(R.id.payButton);
        paymentStatusText = findViewById(R.id.paymentStatusText);

        // Retrieve intent data
        tutorId = getIntent().getStringExtra("tutorId");
        date = getIntent().getStringExtra("date");
        startTime = getIntent().getStringExtra("startTime");
        endTime = getIntent().getStringExtra("endTime");
        docId = getIntent().getStringExtra("docId");
        studentName = getIntent().getStringExtra("studentName");
        studentEmail = getIntent().getStringExtra("studentEmail");
        studentPhone = getIntent().getStringExtra("studentPhone");
        sessionPrice = getIntent().getDoubleExtra("price", 0.0);

        payButton.setOnClickListener(v -> initiatePayment());
    }

    private void initiatePayment() {
        try {
            InitRequest req = new InitRequest();
            req.setMerchantId("122XXXX"); // Replace with your sandbox Merchant ID
            req.setCurrency("ZAR");
            req.setAmount(sessionPrice);
            req.setOrderId("ORDER_" + System.currentTimeMillis());
            req.setItemsDescription("Tutor Booking for " + date + " at " + startTime);

            // Optional: Custom fields
            req.setCustom1(tutorId);
            req.setCustom2(docId);

            // Set customer info
            String[] nameParts = studentName.split(" ", 2);
            req.getCustomer().setFirstName(nameParts[0]);
            req.getCustomer().setLastName(nameParts.length > 1 ? nameParts[1] : "Student");
            req.getCustomer().setEmail(studentEmail);
            req.getCustomer().setPhone(studentPhone);
            req.getCustomer().getAddress().setAddress("123 School Lane");
            req.getCustomer().getAddress().setCity("Durban");
            req.getCustomer().getAddress().setCountry("South Africa");

            // Set delivery address same as above
            req.getCustomer().getDeliveryAddress().setAddress("123 School Lane");
            req.getCustomer().getDeliveryAddress().setCity("Durban");
            req.getCustomer().getDeliveryAddress().setCountry("South Africa");

            // Add item breakdown (optional)
            req.getItems().add(new Item(null, "Tutoring Session", 1, sessionPrice));

            req.setNotifyUrl("https://example.com/payment_notify"); // This must be valid even for sandbox

            Intent intent = new Intent(this, PHMainActivity.class);
            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

            // Force sandbox mode
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

            paymentResultLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Payment launch error", e);
            paymentStatusText.setText("❌ Error launching payment: " + e.getMessage());
        }
    }
}
