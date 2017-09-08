package com.goodsamaritan.drawer.help_and_feedback;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goodsamaritan.R;
import com.goodsamaritan.drawer.help_and_feedback.Help_and_feedback_Adapter.Help_and_Feedback_Adapter;


import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnHelpAndFeedbackInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HelpAndFeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class HelpAndFeedbackFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<Help_and_feedback> help_and_feedbacks_List = new ArrayList<>();
    private RecyclerView recyclerView;
    private Help_and_Feedback_Adapter mAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnHelpAndFeedbackInteractionListener mListener;

    public HelpAndFeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HelpAndFeedbackFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HelpAndFeedbackFragment newInstance(String param1, String param2) {
        HelpAndFeedbackFragment fragment = new HelpAndFeedbackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_and_feedback, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mAdapter = new Help_and_Feedback_Adapter(help_and_feedbacks_List);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        prepareHelpandFeedbackData();


        // Inflate the layout for this fragment
        return view;
    }

    private void prepareHelpandFeedbackData() {
        Help_and_feedback h1 = new Help_and_feedback("About");
        help_and_feedbacks_List.add(h1);
        Help_and_feedback h2 = new Help_and_feedback("FAQ's");
        help_and_feedbacks_List.add(h2);
        Help_and_feedback h3 = new Help_and_feedback("Terms and Privacy Policy");
        help_and_feedbacks_List.add(h3);
        Help_and_feedback h4 = new Help_and_feedback("Contact us");
        help_and_feedbacks_List.add(h4);

        mAdapter.notifyDataSetChanged();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onHelpAndFeedbackInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHelpAndFeedbackInteractionListener) {
            mListener = (OnHelpAndFeedbackInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHelpAndFeedbackInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHelpAndFeedbackInteractionListener {
        // TODO: Update argument type and name
        void onHelpAndFeedbackInteraction(Uri uri);
    }
}
