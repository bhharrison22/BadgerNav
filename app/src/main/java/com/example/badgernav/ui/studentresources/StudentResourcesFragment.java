package com.example.badgernav.ui.studentresources;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.badgernav.R;

public class StudentResourcesFragment extends Fragment {

    private StudentResourcesViewModel mViewModel;

    public static StudentResourcesFragment newInstance() {
        return new StudentResourcesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.student_resources_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hyperlink(getView().findViewById(R.id.canvas), "https://canvas.wisc.edu", "Canvas");
        hyperlink(getView().findViewById(R.id.myuw), "https://my.wisc.edu", "My UW");
        hyperlink(getView().findViewById(R.id.recwell), "https://recwell.wisc.edu", "RecWell");
        hyperlink(getView().findViewById(R.id.libraries), "https://library.wisc.edu", "Libraries");
        hyperlink(getView().findViewById(R.id.guts), "https://guts.wisc.edu", "GUTS Tutoring");
        hyperlink(getView().findViewById(R.id.doit), "https://kb.wisc.edu/helpdesk/", "DoIT Help Desk");
        mViewModel = new ViewModelProvider(this).get(StudentResourcesViewModel.class);
    }

    private void hyperlink(TextView view, String link, String label){
        view.setClickable(true);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='" + link + "'> " + label + " </a>";
        view.setText(Html.fromHtml(text));
    }

}