package edu.gmu.risp.fallcheck.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import edu.gmu.risp.fallcheck.FallSensorActivity;
import edu.gmu.risp.fallcheck.R;
import edu.gmu.risp.fallcheck.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private Button fallCheckStart;

    private FragmentHomeBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        fallCheckStart = root.findViewById(R.id.FallCheckStartButton);


        // attach an OnClickListener
        fallCheckStart.setOnClickListener(v -> enableDisableFallCheck());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void enableDisableFallCheck() {
        Intent intent = new Intent(getActivity(), FallSensorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("keep", true);
        startActivity(intent);

    }
}