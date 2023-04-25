package edu.gmu.risp.fallcheck.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import edu.gmu.risp.fallcheck.R;
import edu.gmu.risp.fallcheck.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private Button emergencyContactUpdateBtn;
    private SharedPreferences sharedpreferences;

    private TextView emergencyContactPersonName;
    private TextView emergencyContactPhoneNumber;

    private TextView updateContactSuccessErrorMsg;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        emergencyContactUpdateBtn = root.findViewById(R.id.updateContactButton);

        emergencyContactPersonName = root.findViewById(R.id.textPersonName);
        emergencyContactPhoneNumber = root.findViewById(R.id.textPhoneNumber);
        updateContactSuccessErrorMsg =  root.findViewById(R.id.updateContactSuccessErrorMsg);
        updateContactSuccessErrorMsg.setVisibility(View.INVISIBLE);

        sharedpreferences = this.getActivity().getSharedPreferences("FallCheckPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("EmergencyContactName",sharedpreferences.getString("EmergencyContactName","911"));
        editor.putString("EmergencyContactNumber",sharedpreferences.getString("EmergencyContactNumber","911"));
        editor.apply();

        emergencyContactPersonName.setText(sharedpreferences.getString("EmergencyContactName", "911"));
        emergencyContactPhoneNumber.setText(sharedpreferences.getString("EmergencyContactNumber", "911"));


        emergencyContactPersonName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                updateContactSuccessErrorMsg.setVisibility(View.INVISIBLE);
            }
        });

        emergencyContactPhoneNumber.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                updateContactSuccessErrorMsg.setVisibility(View.INVISIBLE);
            }
        });


        emergencyContactUpdateBtn.setOnClickListener(v -> updateEmergencyContactDetails(root));

    //    final TextView textView = binding.textDashboard;
    //    settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateEmergencyContactDetails(View root) {
        emergencyContactPersonName = root.findViewById(R.id.textPersonName);
        emergencyContactPhoneNumber = root.findViewById(R.id.textPhoneNumber);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("EmergencyContactName",emergencyContactPersonName.getText().toString());
        editor.putString("EmergencyContactNumber",emergencyContactPhoneNumber.getText().toString());
        editor.apply();
        updateContactSuccessErrorMsg.setVisibility(View.VISIBLE);


    }
}